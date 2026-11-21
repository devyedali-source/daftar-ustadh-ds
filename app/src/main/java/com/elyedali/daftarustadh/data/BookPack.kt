package com.elyedali.daftarustadh.data

import android.content.Context
import com.elyedali.daftarustadh.AppRole
import com.google.android.play.core.assetpacks.AssetPackManager
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackState
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.io.File

/**
 * كتب تطبيق الأستاذ: حزمة مستقلة لكل (مادة، مستوى) على المتجر (bookpacks/، tools/books/packs.py).
 * لا يُنزَّل إلا ما اختاره الأستاذ من موادّه ومستوياته، أو كتاب درس فتحه وطلب تنزيله.
 * التنزيل يتولاه متجر Google (يستأنف وحده إن انقطع الإنترنت)، والكتب بعده تعمل بلا إنترنت.
 * نسخة التجربة (APK من GitHub) بلا كتب.
 */
object BookPack {
    /** حزمة كتاب: مادة ومستوى، وملفاتها (التاريخ والجغرافيا ملفان في حزمة واحدة). */
    /** level: رمز الحزمة؛ levels: المستويات (الشعب) التي يخدمها الكتاب — كتاب الثانوية قد يشترك فيه أكثر من شعبة. */
    data class Pack(val name: String, val subject: String, val level: String, val files: List<String>, val bytes: Long,
                    val levels: List<String> = listOf(level)) {
        fun serves(chosen: Collection<String>) = levels.any { it in chosen }
        val levelsLabel: String get() = levels.joinToString(" · ")
    }

    sealed class Status {
        object Ready : Status()
        object NotDownloaded : Status()
        /** طُلب تنزيلها والمتجر يجهّز. */
        object Pending : Status()
        data class Downloading(val percent: Int) : Status()
        /** تحتاج موافقة المستخدم (بيانات الهاتف بدل Wi-Fi). */
        object NeedsConfirmation : Status()
        /** انقطع التنزيل: يُستأنف وحده بعد دقيقة. */
        object Failed : Status()
        /** نسخة التجربة من GitHub: لا كتب فيها (تأتي مع نسخة المتجر). */
        object NotInTestBuild : Status()
    }

    private const val PREFS = "book_packs"
    private const val KEY_SELECTED = "selected"
    private const val KEY_PICKED = "picked_once"
    private const val KEY_SUBJECTS = "subjects"
    private const val KEY_LEVELS = "levels"
    /** يختار الأستاذ حتى 3 مواد (كما في التفعيل). */
    const val MAX_SUBJECTS = 3

    private val _states = MutableStateFlow<Map<String, Status>>(emptyMap())
    /** حالة كل حزمة باسمها. */
    val states: StateFlow<Map<String, Status>> = _states

    private var packs: List<Pack>? = null
    private var manager: AssetPackManager? = null
    private var bundled: Boolean? = null
    private var listening = false
    private var appContext: Context? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    /** فهرس الكتب (app/src/teacher/assets/book_packs.json). فارغ في تطبيق الإدارة. */
    fun packs(context: Context): List<Pack> = packs ?: try {
        val root = JSONObject(context.assets.open("book_packs.json").bufferedReader(Charsets.UTF_8).use { it.readText() })
        val arr = root.getJSONArray("packs")
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val f = o.getJSONArray("files")
            val lv = o.optJSONArray("levels")
            Pack(o.getString("name"), o.getString("subject"), o.getString("level"),
                (0 until f.length()).map { f.getString(it) }, o.getLong("bytes"),
                lv?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: listOf(o.getString("level")))
        }
    } catch (e: Exception) { emptyList() }.also { packs = it }

    /** اسم الحزمة من مسار الكتاب: books/HG_1AS_GEO.pdf ← b_hg_1as */
    fun packOf(assetPath: String): String =
        "b_" + assetPath.substringAfterLast('/').removeSuffix(".pdf").split('_').take(2).joinToString("_").lowercase()

    fun packFor(context: Context, assetPath: String): Pack? = packOf(assetPath).let { n -> packs(context).firstOrNull { it.name == n } }

    fun statusOf(assetPath: String): Status = _states.value[packOf(assetPath)] ?: Status.NotDownloaded

    // ---------- اختيار الأستاذ ----------
    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    fun selected(c: Context): Set<String> = prefs(c).getStringSet(KEY_SELECTED, emptySet()).orEmpty().toSet()
    fun pickedSubjects(c: Context): Set<String> = prefs(c).getStringSet(KEY_SUBJECTS, emptySet()).orEmpty().toSet()
    fun pickedLevels(c: Context): Set<String> = prefs(c).getStringSet(KEY_LEVELS, emptySet()).orEmpty().toSet()

    /** تُعرض صفحة الاختيار عند أول دخول للأستاذ (مرة واحدة، ويبقى تعديلها من القائمة). */
    fun needsFirstPick(c: Context): Boolean =
        !AppRole.isAdmin && packs(c).isNotEmpty() && !prefs(c).getBoolean(KEY_PICKED, false)

    fun markPicked(c: Context) { prefs(c).edit().putBoolean(KEY_PICKED, true).apply() }

    /**
     * يحفظ مواد الأستاذ ومستوياته وينزّل كتبها. الكتب التي كانت مختارة وخرجت من الاختيار تبقى على الهاتف
     * (يحذفها الأستاذ بنفسه إن أراد) حتى لا يضيع ما نزل بإنترنت ضعيف.
     */
    fun choose(c: Context, subjects: Set<String>, levels: Set<String>) {
        val wanted = packs(c).filter { it.subject in subjects && it.serves(levels) }.map { it.name }.toSet()
        prefs(c).edit().putStringSet(KEY_SUBJECTS, subjects).putStringSet(KEY_LEVELS, levels)
            .putStringSet(KEY_SELECTED, selected(c) + wanted).putBoolean(KEY_PICKED, true).apply()
        fetch(c, wanted)
    }

    /** تنزيل كتاب واحد (مثلًا درس من مادة أخرى فتحه الأستاذ). */
    fun download(c: Context, name: String) {
        prefs(c).edit().putStringSet(KEY_SELECTED, selected(c) + name).apply()
        fetch(c, setOf(name))
    }

    /** حذف كتاب من الهاتف لتوفير المساحة. */
    fun remove(c: Context, name: String) {
        prefs(c).edit().putStringSet(KEY_SELECTED, selected(c) - name).apply()
        set(name, Status.NotDownloaded)
        try { manager(c).removePack(name) } catch (e: Exception) { }
    }

    // ---------- المتجر ----------
    private fun manager(c: Context): AssetPackManager =
        manager ?: AssetPackManagerFactory.getInstance(c.applicationContext).also { manager = it }

    private fun isBundled(context: Context): Boolean = bundled ?: try {
        !context.assets.list("books").isNullOrEmpty()
    } catch (e: Exception) { false }.also { bundled = it }

    /** نسخة التجربة تُنزّل الكتب فقط في الاختبار المحلي للمتجر (bundletool --local-testing). */
    private fun isTestBuild(c: Context): Boolean = com.elyedali.daftarustadh.BuildConfig.DEBUG &&
        File(c.getExternalFilesDir(null), "local_testing").let { !it.exists() }

    private fun set(name: String, st: Status) { _states.value = _states.value + (name to st) }

    private val listener = AssetPackStateUpdateListener { s: AssetPackState ->
        val st = toStatus(s)
        set(s.name(), st)
        if (st is Status.Failed) retryLater()
    }

    private fun toStatus(s: AssetPackState): Status = when (s.status()) {
        AssetPackStatus.COMPLETED -> Status.Ready
        AssetPackStatus.DOWNLOADING, AssetPackStatus.TRANSFERRING -> {
            val total = s.totalBytesToDownload()
            Status.Downloading(if (total > 0) (s.bytesDownloaded() * 100 / total).toInt().coerceIn(0, 100) else 0)
        }
        AssetPackStatus.WAITING_FOR_WIFI, AssetPackStatus.REQUIRES_USER_CONFIRMATION -> Status.NeedsConfirmation
        AssetPackStatus.FAILED -> Status.Failed
        AssetPackStatus.CANCELED, AssetPackStatus.NOT_INSTALLED -> Status.NotDownloaded
        else -> Status.Pending
    }

    /** إعادة المحاولة وحدها بعد دقيقة لكل كتاب مختار لم يكتمل (انقطاع الإنترنت مثلًا). */
    private fun retryLater() {
        val c = appContext ?: return
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            val missing = selected(c).filter { _states.value[it] !is Status.Ready }.toSet()
            if (missing.isNotEmpty()) fetch(c, missing)
        }, 60_000L)
    }

    private fun fetch(c: Context, names: Set<String>) {
        if (names.isEmpty() || AppRole.isAdmin) return
        if (isBundled(c) || isTestBuild(c)) return
        try {
            val m = manager(c)
            if (!listening) { m.registerListener(listener); listening = true }
            val todo = names.filter { m.getPackLocation(it) == null }
            names.filter { it !in todo }.forEach { set(it, Status.Ready) }
            if (todo.isEmpty()) return
            todo.forEach { if (_states.value[it] !is Status.Downloading) set(it, Status.Pending) }
            m.fetch(todo).addOnFailureListener { todo.forEach { n -> set(n, Status.Failed) }; retryLater() }
        } catch (e: Exception) {
            names.forEach { set(it, Status.Failed) }
            retryLater()
        }
    }

    /** يُستدعى عند فتح تطبيق الأستاذ: حالة كل كتاب، ومتابعة تنزيل ما اختاره ولم يكتمل. */
    fun start(context: Context) {
        if (AppRole.isAdmin) return
        val app = context.applicationContext
        appContext = app
        val all = packs(app).map { it.name }
        when {
            isBundled(app) -> { _states.value = all.associateWith { Status.Ready }; return }
            isTestBuild(app) -> { _states.value = all.associateWith { Status.NotInTestBuild }; return }
        }
        try {
            val m = manager(app)
            _states.value = all.associateWith { if (m.getPackLocation(it) != null) Status.Ready else Status.NotDownloaded }
        } catch (e: Exception) {
            _states.value = all.associateWith { Status.NotDownloaded }
        }
        fetch(app, selected(app).filter { _states.value[it] !is Status.Ready }.toSet())
    }

    /**
     * موافقة المستخدم على التنزيل ببيانات الهاتف (يطلبها المتجر أحيانًا للملفات الكبيرة).
     * تُرجع false إن لم يكن هناك ما يحتاج الموافقة.
     */
    fun confirm(launcher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>): Boolean = try {
        manager?.showConfirmationDialog(launcher) ?: false
    } catch (e: Exception) { false }

    /** ملف الكتاب الجاهز للقراءة، أو null إن لم يُنزَّل بعد. */
    fun file(context: Context, assetPath: String): File? {
        if (isBundled(context)) return null   // يُقرأ من داخل التطبيق كما كان
        val loc = try { manager(context).getPackLocation(packOf(assetPath)) } catch (e: Exception) { null } ?: return null
        return loc.assetsPath()?.let { File(it, assetPath) }?.takeIf { it.exists() }
    }

    fun bundledIn(context: Context) = isBundled(context)
}
