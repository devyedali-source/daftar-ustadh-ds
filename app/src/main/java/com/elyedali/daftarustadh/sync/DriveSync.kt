package com.elyedali.daftarustadh.sync

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import androidx.room.InvalidationTracker
import com.elyedali.daftarustadh.AppRole
import com.elyedali.daftarustadh.auth.DsAuth
import com.elyedali.daftarustadh.auth.DsSubscriptions
import com.elyedali.daftarustadh.auth.TrustedClock
import com.elyedali.daftarustadh.data.DaftarDatabase
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * المزامنة مع Google Drive (حساب المستخدم نفسه) — صامتة وصادقة:
 *  - نسخة واحدة مضغوطة لكل حساب ولكل تطبيق (الإدارة / الأستاذ) في Drive.
 *  - تُزامَن عند فتح التطبيق وعند الخروج منه إذا تغيّر شيء، وبزر «مزامنة الآن».
 *  - هاتفان على نفس الحساب: إن لم يتغيّر شيء على هذا الهاتف تؤخذ نسخة Drive كما هي،
 *    وإن تغيّر على الهاتفين يُدمَج الاثنان فلا يضيع شيء (عند تعارض نفس السجل يُؤخذ الأحدث).
 *  - «آخر مزامنة» لا تُكتب إلا بعد التأكد من وصول الملف إلى Drive.
 */
object DriveSync {
    private const val PREFS = "ds_drive_sync"
    private const val SCOPE = "oauth2:https://www.googleapis.com/auth/drive.file"
    private const val FORMAT = 1
    /** جداول داخلية لا تُنسخ. */
    private val SKIP_TABLES = setOf("android_metadata", "room_master_table", "sqlite_sequence", "room_table_modification_log")
    /** مفاتيح خاصة بهذا الهاتف أو تأتي من الخادم، لا تُنسخ. */
    private val SKIP_META = setOf(DsSubscriptions.CACHE_KEY, "activated_classes", "teacher_activated_years", "expired_classes",
        com.elyedali.daftarustadh.data.TeacherActivation.PACKS_KEY)

    sealed class State {
        object Idle : State()
        object Running : State()
        data class Done(val at: Long) : State()
        data class Failed(val reason: String) : State()
        /** يحتاج موافقة المستخدم على الوصول إلى Drive (مرة واحدة). */
        data class NeedsConsent(val intent: Intent) : State()
        object Offline : State()
    }

    data class Device(val id: String, val name: String)

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state
    private val _lastSync = MutableStateFlow(0L)
    val lastSync: StateFlow<Long> = _lastSync
    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    @Volatile private var ctx: Context? = null
    @Volatile private var suppressUntil = 0L
    private var observedDb: DaftarDatabase? = null

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private fun emailKey() = (DsAuth.user?.email ?: "none").lowercase()
    /** المؤسسة المفتوحة (تطبيق الإدارة: لكل مؤسسة نسختها في Drive). */
    /** لاحقة مفاتيح الحفظ: لا شيء للمؤسسة الأولى (كما كانت)، ومفتاح المؤسسة لغيرها. */
    private fun sfx(db: DaftarDatabase): String = emailKey() + db.instKey.let { if (it == com.elyedali.daftarustadh.data.Institutions.MAIN) "" else "__$it" }
    private fun appName() = if (AppRole.isAdmin) "admin" else "teacher"

    /** يبدأ متابعة التغييرات على الهاتف (لمعرفة هل تغيّر شيء منذ آخر مزامنة). */
    fun init(context: Context, db: DaftarDatabase) {
        ctx = context.applicationContext
        val p = prefs(context)
        _lastSync.value = p.getLong("last_${sfx(db)}", 0L)
        _devices.value = decodeDevices(p.getString("devices_${sfx(db)}", null))
        if (observedDb === db) return
        observedDb = db
        scope.launch {
            try {
                val tables = userTables(db).toTypedArray()
                if (tables.isEmpty()) return@launch
                val key = sfx(db)   // تغييرات هذه القاعدة تُحسب لمؤسستها هي
                db.invalidationTracker.addObserver(object : InvalidationTracker.Observer(tables) {
                    override fun onInvalidated(tables: Set<String>) {
                        if (System.currentTimeMillis() < suppressUntil) return
                        val c = ctx ?: return
                        prefs(c).edit().putLong("changed_$key", TrustedClock.now()).apply()
                    }
                })
            } catch (e: Exception) { }
        }
    }

    /**
     * هاتف يدخل إليه حساب آخر غير صاحب البيانات الموجودة: تُفرَّغ بيانات الحساب السابق من الهاتف
     * (هي محفوظة في Drive حسابه)، ثم تأتي بيانات الحساب الجديد من Drive حسابه. لا تختلط بيانات حسابين أبدًا.
     * يرجع true إذا وجب إعادة فتح الشاشة (كانت مفتوحة على مؤسسة أخرى للحساب السابق). */
    suspend fun prepareAccount(context: Context, db: DaftarDatabase): Boolean = withContext(Dispatchers.IO) {
        val email = DsAuth.user?.email?.lowercase() ?: return@withContext false
        val p = prefs(context)
        val owner = p.getString("db_owner", null)
        if (owner == null) { p.edit().putString("db_owner", email).apply(); return@withContext false }
        if (owner == email) return@withContext false
        // مؤسسات الحساب السابق (الإضافية) تُحذف من الهاتف، ويُرجع إلى المؤسسة الأولى
        val restart = DaftarDatabase.openKey != com.elyedali.daftarustadh.data.Institutions.MAIN
        com.elyedali.daftarustadh.data.Institutions.resetAll(context)
        val main = if (!restart) db else {
            DaftarDatabase.switchTo(context, com.elyedali.daftarustadh.data.Institutions.MAIN)
            DaftarDatabase.get(context)
        }
        suppressUntil = System.currentTimeMillis() + 5_000
        main.runInTransaction(Runnable {
            val w = main.openHelper.writableDatabase
            userTables(main).forEach { t -> w.execSQL("DELETE FROM `$t`") }
        })
        p.edit().putString("db_owner", email).apply()
        _lastSync.value = 0L
        p.edit().remove("last_${sfx(main)}").remove("changed_${sfx(main)}").apply()
        _devices.value = emptyList()
        restart
    }

    /**
     * كتابات تأتي من الخادم (حالة الاشتراكات والطلبات) لا تُحسب «تغييرًا من المستخدم»:
     * وإلا صار الهاتف يدمج بدل أن يأخذ آخر نسخة من الهاتف الآخر، فيعود ما حُذف هناك.
     */
    fun quiet() { suppressUntil = maxOf(suppressUntil, System.currentTimeMillis() + 3_000) }

    private fun changedAt(c: Context, db: DaftarDatabase) = prefs(c).getLong("changed_${sfx(db)}", 0L)
    fun hasLocalChanges(c: Context, db: DaftarDatabase) = changedAt(c, db) > prefs(c).getLong("last_${sfx(db)}", 0L)

    /** مزامنة في الخلفية (لا تُظهر شيئًا إن لم يكن هناك إنترنت). */
    fun syncInBackground(context: Context, db: DaftarDatabase, onlyIfChanged: Boolean = false) {
        if (DsAuth.user == null) return
        if (onlyIfChanged && !hasLocalChanges(context, db)) return
        scope.launch { sync(context, db) }
    }

    /** المزامنة: تحميل نسخة Drive ودمجها إن لزم، ثم رفع النسخة الكاملة والتأكد من وصولها. */
    suspend fun sync(context: Context, db: DaftarDatabase): State = mutex.withLock {
        withContext(Dispatchers.IO) {
            val c = context.applicationContext
            if (!online(c)) return@withContext State.Offline.also { _state.value = it }
            _state.value = State.Running
            val result = try {
                TrustedClock.syncWithServer()
                val token = token(c)
                val name = fileName(db)
                val p = prefs(c)
                val ek = sfx(db)
                val last = p.getLong("last_$ek", 0L)
                var devices = decodeDevices(p.getString("devices_$ek", null))
                var attempt = 0
                var id = ""
                var size = -1L
                var payload = ByteArray(0)
                var now = 0L
                while (true) {
                    val fileId = findFile(token, name)
                    val rev0 = fileId?.let { revision(token, it) }
                    if (fileId != null) {
                        val remote = JSONObject(gunzip(download(token, fileId)))
                        if (remote.optInt("dbVersion") > db.openHelper.readableDatabase.version)
                            throw SyncException("update")   // نسخة Drive من إصدار أحدث للتطبيق
                        devices = mergeDevices(decodeDevices(remote.optJSONArray("devices")?.toString()), devices)
                        val remoteAt = remote.optLong("savedAt")
                        val remoteDevice = remote.optString("deviceId")
                        if (remoteAt > last && remoteDevice != deviceId(c)) {
                            val localChanged = changedAt(c, db)
                            when {
                                // أول مزامنة لهاتف فيه بيانات: دمج بلا حذف (لا يضيع شيء مما على الهاتف)
                                last == 0L && hasData(db) -> restore(db, remote, replace = false, remoteWins = true)
                                // لم يتغيّر شيء هنا منذ آخر مزامنة (أو هاتف جديد فارغ): نأخذ نسخة Drive كما هي
                                localChanged <= last && attempt == 0 -> restore(db, remote, replace = true, remoteWins = true)
                                // تغيّر على الهاتفين: دمج بلا حذف، والأحدث يغلب عند التعارض
                                else -> restore(db, remote, replace = false, remoteWins = remoteAt > localChanged)
                            }
                        }
                    }
                    devices = mergeDevices(listOf(Device(deviceId(c), deviceName())), devices)
                    now = TrustedClock.now()
                    payload = gzip(snapshot(db, now, deviceId(c), devices).toString())
                    // هاتف آخر رفع نسخته أثناء ذلك: نعيد الدمج معها قبل الرفع، فلا تمحو نسخةٌ الأخرى
                    if (fileId != null && attempt < 3 && revision(token, fileId) != rev0) { attempt++; continue }
                    id = fileId ?: createFile(token, name)
                    size = upload(token, id, payload)
                    break
                }
                if (size != payload.size.toLong()) throw SyncException("verify")
                if (AppRole.isAdmin) {
                    try { describe(token, id, db); discover(c, token) } catch (e: Exception) { }
                }
                p.edit().putLong("last_$ek", now).putLong("changed_$ek", minOf(changedAt(c, db), now))
                    .putString("devices_$ek", encodeDevices(devices)).apply()
                _lastSync.value = now
                _devices.value = devices
                DsSubscriptions.apply(db)
                State.Done(now)
            } catch (e: UserRecoverableAuthException) {
                e.intent?.let { State.NeedsConsent(it) } ?: State.Failed("auth")
            } catch (e: SyncException) {
                State.Failed(e.message ?: "")
            } catch (e: Exception) {
                State.Failed(e.message ?: e.javaClass.simpleName)
            }
            _state.value = result
            result
        }
    }

    private class SyncException(msg: String) : Exception(msg)

    // ———— قاعدة البيانات ————

    private fun userTables(db: DaftarDatabase): List<String> {
        val out = ArrayList<String>()
        db.openHelper.readableDatabase.query("SELECT name FROM sqlite_master WHERE type='table'").use { cur ->
            while (cur.moveToNext()) cur.getString(0)?.let { if (it !in SKIP_TABLES && !it.startsWith("sqlite_")) out += it }
        }
        return out
    }

    private fun hasData(db: DaftarDatabase): Boolean = userTables(db).filter { it != "app_meta" }.any { t ->
        db.openHelper.readableDatabase.query("SELECT 1 FROM `$t` LIMIT 1").use { it.moveToFirst() }
    }

    private fun snapshot(db: DaftarDatabase, now: Long, device: String, devices: List<Device>): JSONObject {
        val r = db.openHelper.readableDatabase
        val tables = JSONObject()
        for (t in userTables(db)) {
            val rows = JSONArray()
            r.query("SELECT * FROM `$t`").use { cur ->
                val cols = cur.columnNames
                while (cur.moveToNext()) {
                    val o = JSONObject()
                    for (i in cols.indices) {
                        when (cur.getType(i)) {
                            android.database.Cursor.FIELD_TYPE_NULL -> o.put(cols[i], JSONObject.NULL)
                            android.database.Cursor.FIELD_TYPE_INTEGER -> o.put(cols[i], cur.getLong(i))
                            android.database.Cursor.FIELD_TYPE_FLOAT -> o.put(cols[i], cur.getDouble(i))
                            android.database.Cursor.FIELD_TYPE_BLOB -> o.put(cols[i], JSONObject().put("b64", Base64.encodeToString(cur.getBlob(i), Base64.NO_WRAP)))
                            else -> o.put(cols[i], cur.getString(i))
                        }
                    }
                    if (t == "app_meta" && o.optString("key") in SKIP_META) continue
                    rows.put(o)
                }
            }
            tables.put(t, rows)
        }
        return JSONObject().put("format", FORMAT).put("app", appName()).put("dbVersion", r.version)
            .put("savedAt", now).put("deviceId", device).put("devices", JSONArray(encodeDevices(devices)))
            .put("tables", tables)
    }

    /** يكتب نسخة Drive في قاعدة الهاتف: replace = استبدال كامل، وإلا دمج (remoteWins: من يغلب عند التعارض). */
    private fun restore(db: DaftarDatabase, remote: JSONObject, replace: Boolean, remoteWins: Boolean) {
        val tables = remote.optJSONObject("tables") ?: return
        val local = userTables(db).toSet()
        suppressUntil = System.currentTimeMillis() + 5_000
        db.runInTransaction(Runnable {
            val w = db.openHelper.writableDatabase
            for (t in local) {
                val rows = tables.optJSONArray(t) ?: continue
                val cols = HashSet<String>()
                w.query("PRAGMA table_info(`$t`)").use { cur -> val i = cur.getColumnIndex("name"); while (cur.moveToNext()) cols += cur.getString(i) }
                if (replace) {
                    if (t == "app_meta") w.execSQL("DELETE FROM app_meta WHERE `key` NOT IN (${SKIP_META.joinToString { "'$it'" }})")
                    else w.execSQL("DELETE FROM `$t`")
                }
                val verb = if (replace || remoteWins) "INSERT OR REPLACE" else "INSERT OR IGNORE"
                for (k in 0 until rows.length()) {
                    val o = rows.getJSONObject(k)
                    if (t == "app_meta" && o.optString("key") in SKIP_META) continue
                    val names = o.keys().asSequence().filter { it in cols }.toList()
                    if (names.isEmpty()) continue
                    val args = names.map { n ->
                        val v = o.opt(n)
                        when {
                            v == null || v == JSONObject.NULL -> null
                            v is JSONObject && v.has("b64") -> Base64.decode(v.getString("b64"), Base64.NO_WRAP)
                            else -> v
                        }
                    }.toTypedArray()
                    try {
                        w.execSQL("$verb INTO `$t` (${names.joinToString { "`$it`" }}) VALUES (${names.joinToString { "?" }})", args)
                    } catch (e: Exception) { }
                }
            }
        })
        suppressUntil = System.currentTimeMillis() + 3_000
    }

    // ———— Google Drive ————

    private fun token(c: Context): String {
        val account = GoogleSignIn.getLastSignedInAccount(c)?.account ?: throw SyncException("signin")
        return GoogleAuthUtil.getToken(c, account, SCOPE)
    }

    private fun cleanEmail() = emailKey().replace(Regex("[^a-z0-9_]"), "_")
    private fun fileName(db: DaftarDatabase): String {
        val k = db.instKey
        return "ds_${appName()}_backup_${cleanEmail()}" + (if (k == com.elyedali.daftarustadh.data.Institutions.MAIN) "" else "__$k") + ".json.gz"
    }

    /** اسم المؤسسة المفتوحة وولايتها، يُكتبان مع نسخة Drive لتظهر في هاتف آخر لنفس الحساب. */
    private suspend fun describe(token: String, id: String, db: DaftarDatabase) {
        val own = db.institutionDao().getOwn() ?: return
        val props = JSONObject().put("dsInst", db.instKey).put("instName", own.schoolName.take(100)).put("wilaya", own.wilaya.take(60))
        http("https://www.googleapis.com/drive/v3/files/$id?fields=id", "PATCH", token,
            JSONObject().put("appProperties", props).toString().toByteArray(), "application/json; charset=utf-8")
    }

    /** مؤسسات هذا الحساب الموجودة في Drive (تطبيق الإدارة): تُضاف إلى قائمة المؤسسات في هذا الهاتف. */
    private fun discover(c: Context, token: String) {
        val q = URLEncoder.encode("name contains 'ds_admin_backup_${cleanEmail()}' and trashed=false", "UTF-8")
        val (_, b) = http("https://www.googleapis.com/drive/v3/files?q=$q&spaces=drive&fields=files(id,name,appProperties)", "GET", token)
        val files = JSONObject(String(b)).optJSONArray("files") ?: return
        for (i in 0 until files.length()) {
            val f = files.getJSONObject(i)
            val n = f.optString("name")
            val prefix = "ds_admin_backup_${cleanEmail()}"
            if (!n.startsWith(prefix) || !n.endsWith(".json.gz")) continue
            val rest = n.removePrefix(prefix).removeSuffix(".json.gz")
            if (rest.isNotEmpty() && !rest.startsWith("__")) continue   // حساب آخر يبدأ بنفس الحروف
            val key = rest.removePrefix("__").ifBlank { com.elyedali.daftarustadh.data.Institutions.MAIN }
            val p = f.optJSONObject("appProperties")
            com.elyedali.daftarustadh.data.Institutions.addKnown(c, key, p?.optString("instName").orEmpty(), p?.optString("wilaya").orEmpty())
        }
    }

    private fun http(url: String, method: String, token: String, body: ByteArray? = null, type: String? = null): Pair<Int, ByteArray> {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 30_000; conn.readTimeout = 60_000
            if (method == "PATCH") { conn.requestMethod = "POST"; conn.setRequestProperty("X-HTTP-Method-Override", "PATCH") }
            else conn.requestMethod = method
            conn.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", type ?: "application/octet-stream")
                conn.setFixedLengthStreamingMode(body.size)
                conn.outputStream.use { it.write(body) }
            }
            val code = conn.responseCode
            val bytes = (if (code in 200..299) conn.inputStream else conn.errorStream)?.use { it.readBytes() } ?: ByteArray(0)
            if (code == 401) GoogleAuthUtil.clearToken(ctx ?: throw SyncException("auth"), token)
            if (code !in 200..299) throw SyncException("drive $code")
            return code to bytes
        } finally { conn.disconnect() }
    }

    private fun findFile(token: String, name: String): String? {
        val q = URLEncoder.encode("name='$name' and trashed=false", "UTF-8")
        val (_, b) = http("https://www.googleapis.com/drive/v3/files?q=$q&spaces=drive&orderBy=modifiedTime%20desc&fields=files(id)", "GET", token)
        val files = JSONObject(String(b)).optJSONArray("files") ?: return null
        return if (files.length() > 0) files.getJSONObject(0).optString("id").takeIf { it.isNotBlank() } else null
    }

    private fun createFile(token: String, name: String): String {
        val meta = JSONObject().put("name", name).put("mimeType", "application/gzip")
            .put("description", if (AppRole.isAdmin) "إدارة DS — نسخة المزامنة" else "دفتر الأستاذ DS — نسخة المزامنة")
        val (_, b) = http("https://www.googleapis.com/drive/v3/files?fields=id", "POST", token, meta.toString().toByteArray(), "application/json; charset=utf-8")
        return JSONObject(String(b)).getString("id")
    }

    private fun upload(token: String, id: String, data: ByteArray): Long {
        val (_, b) = http("https://www.googleapis.com/upload/drive/v3/files/$id?uploadType=media&fields=id,size", "PATCH", token, data, "application/gzip")
        return JSONObject(String(b)).optString("size").toLongOrNull() ?: -1L
    }

    /** رقم آخر نسخة من الملف في Drive (يتغيّر عند كل رفع). */
    private fun revision(token: String, id: String): String? =
        JSONObject(String(http("https://www.googleapis.com/drive/v3/files/$id?fields=headRevisionId", "GET", token).second))
            .optString("headRevisionId").takeIf { it.isNotBlank() }

    private fun download(token: String, id: String): ByteArray =
        http("https://www.googleapis.com/drive/v3/files/$id?alt=media", "GET", token).second

    // ———— أدوات ————

    private fun gzip(s: String): ByteArray = ByteArrayOutputStream().also { o -> GZIPOutputStream(o).use { it.write(s.toByteArray()) } }.toByteArray()
    private fun gunzip(b: ByteArray): String = GZIPInputStream(b.inputStream()).use { String(it.readBytes()) }

    private fun online(c: Context): Boolean = try {
        val cm = c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    } catch (e: Exception) { false }

    private fun deviceId(c: Context): String = try {
        android.provider.Settings.Secure.getString(c.contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "unknown"
    } catch (e: Exception) { "unknown" }

    private fun deviceName(): String = "${android.os.Build.MANUFACTURER.orEmpty()} ${android.os.Build.MODEL.orEmpty()}".trim().ifBlank { "?" }

    /** هذا الهاتف أولًا، بلا تكرار لنفس الهاتف. */
    private fun mergeDevices(first: List<Device>, rest: List<Device>): List<Device> {
        val out = LinkedHashMap<String, Device>()
        (first + rest).forEach { d -> if (d.id.isNotBlank() && d.id !in out && out.values.none { it.name == d.name }) out[d.id] = d }
        return out.values.take(10)
    }

    private fun encodeDevices(list: List<Device>): String = JSONArray(list.map { JSONObject().put("id", it.id).put("name", it.name) }).toString()
    private fun decodeDevices(text: String?): List<Device> = try {
        val a = JSONArray(text ?: "[]")
        (0 until a.length()).map { a.getJSONObject(it).let { o -> Device(o.optString("id"), o.optString("name")) } }
    } catch (e: Exception) { emptyList() }
}
