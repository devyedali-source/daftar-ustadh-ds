package com.elyedali.daftarustadh.auth

import com.elyedali.daftarustadh.data.AppMetaEntity
import com.elyedali.daftarustadh.data.ClassActivation
import com.elyedali.daftarustadh.data.DaftarDatabase
import com.elyedali.daftarustadh.data.InstitutionEntity
import com.elyedali.daftarustadh.data.TeacherActivation
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/**
 * الاشتراكات على الخادم: مربوطة بالحساب والمؤسسة (لكل مؤسسة اشتراكها) والسنة الدراسية.
 *  - ds_activation_requests: طلب التفعيل (معلّق) يُنشئه المستخدم، ثم يرسل رمزه على واتساب.
 *  - ds_subscriptions: الاشتراك المفعّل، لا يكتبه إلا صاحب التطبيق من لوحة التحكم.
 * تُحفظ نسخة منها على الهاتف، وتُحسب صلاحيتها بالساعة الموثوقة فيعمل التطبيق بلا إنترنت.
 */
object DsSubscriptions {
    const val CACHE_KEY = "ds_subs_cache"
    const val APP_ADMIN = "admin"
    const val APP_TEACHER = "teacher"

    private fun norm(t: String?) = t.orEmpty().trim().replace(Regex("\\s+"), " ").lowercase()

    /** مفتاح المؤسسة: رمزها في لائحة المدارس، وإلا اسمها وولايتها. */
    fun institutionKey(inst: InstitutionEntity?): String =
        inst?.schoolListId?.takeIf { it.isNotBlank() }?.let { "L$it" } ?: "N${norm(inst?.schoolName)}|${norm(inst?.wilaya)}"

    /** نهاية السنة الدراسية «2026-2027»: آخر يوم من أغسطس 2027. */
    fun yearEnd(schoolYear: String): Long {
        val end = schoolYear.substringAfter('-').trim().toIntOrNull() ?: (Calendar.getInstance().get(Calendar.YEAR) + 1)
        return Calendar.getInstance().apply { set(end, Calendar.AUGUST, 31, 23, 59, 59); set(Calendar.MILLISECOND, 0) }.timeInMillis
    }

    /** يجلب اشتراكات الحساب من الخادم ويحفظها على الهاتف ثم يطبّقها. */
    suspend fun refresh(db: DaftarDatabase): Boolean {
        val u = DsAuth.user ?: return false
        return try {
            val snap = FirebaseFirestore.getInstance().collection("ds_subscriptions").whereEqualTo("uid", u.uid).get().await()
            val arr = JSONArray()
            snap.documents.forEach { d ->
                arr.put(JSONObject()
                    .put("app", d.getString("app") ?: "")
                    .put("inst", d.getString("institutionKey") ?: "")
                    .put("year", d.getString("schoolYear") ?: "")
                    .put("classIds", JSONArray((d.get("classIds") as? List<*>)?.map { it.toString() } ?: emptyList<String>()))
                    .put("expiresAt", d.getLong("expiresAt") ?: 0L))
            }
            db.appMetaDao().put(AppMetaEntity(CACHE_KEY, arr.toString()))
            apply(db)
            true
        } catch (e: Exception) { false }
    }

    /** يطبّق الاشتراكات المحفوظة (السارية بالساعة الموثوقة) على قفل الأقسام وسنوات الأستاذ. */
    suspend fun apply(db: DaftarDatabase) {
        val text = db.appMetaDao().get(CACHE_KEY) ?: return   // لم يتصل بالخادم بعد: لا نغيّر شيئًا
        val now = TrustedClock.now()
        val arr = try { JSONArray(text) } catch (e: Exception) { return }
        val classes = HashSet<String>(); val years = HashSet<String>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optLong("expiresAt") <= now) continue
            when (o.optString("app")) {
                APP_ADMIN -> o.optJSONArray("classIds")?.let { a -> for (k in 0 until a.length()) classes += a.getString(k) }
                APP_TEACHER -> years += o.optString("year")
            }
        }
        if (com.elyedali.daftarustadh.AppRole.isAdmin)
            db.appMetaDao().put(AppMetaEntity(ClassActivation.KEY, ClassActivation.encode(classes)))
        else db.appMetaDao().put(AppMetaEntity(TeacherActivation.KEY, TeacherActivation.encode(years)))
    }

    /** رابط التفعيل الذي يُرسل على واتساب: يفتح الطلب مباشرة في لوحة التحكم (كما في دفتر المعلم). */
    fun activationLink(requestId: String) = "https://$LINK_HOST$LINK_PATH?r=$requestId"
    const val LINK_HOST = "moctar-gratuit.web.app"
    const val LINK_PATH = "/ds-activate"
    fun shortCode(requestId: String) = requestId.take(6).uppercase()

    /** ينشئ طلب تفعيل على الخادم ويعيد معرّفه (رمزه القصير = أول 6 أحرف)، أو null بلا إنترنت. */
    suspend fun request(
        app: String, inst: InstitutionEntity?, schoolYear: String,
        classIds: List<String>, classNames: List<String>, amount: Int, teacherName: String = ""
    ): String? {
        val u = DsAuth.user ?: return null
        return try {
            val ref = FirebaseFirestore.getInstance().collection("ds_activation_requests").document()
            ref.set(hashMapOf(
                "uid" to u.uid, "email" to (u.email ?: ""), "name" to (u.displayName ?: teacherName),
                "app" to app, "status" to "pending",
                "institutionKey" to if (app == APP_ADMIN) institutionKey(inst) else "",
                "institutionName" to (inst?.schoolName ?: ""), "wilaya" to (inst?.wilaya ?: ""),
                "schoolYear" to schoolYear, "classIds" to classIds, "classNames" to classNames,
                "amount" to amount, "code" to ref.id.take(6).uppercase(),
                "createdAt" to FieldValue.serverTimestamp()
            )).await()
            ref.id
        } catch (e: Exception) { null }
    }
}
