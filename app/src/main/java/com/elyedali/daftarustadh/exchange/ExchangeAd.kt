package com.elyedali.daftarustadh.exchange

import org.json.JSONArray
import org.json.JSONObject

/** صفة صاحب الإعلان في مؤسسته. */
object ExchangeRole {
    const val TEACHER = "teacher"            // أستاذ
    const val STUDIES_DIRECTOR = "director"  // مدير دروس
    const val GENERAL_SUPERVISOR = "supervisor" // مراقب عام
    val ALL = listOf(TEACHER, STUDIES_DIRECTOR, GENERAL_SUPERVISOR)
}

/**
 * إعلان تبادل. لا يحتوي اسمًا ولا أي معلومة شخصية.
 * رقم الواتساب محفوظ مع الإعلان لكنه لا يُعرض أبدًا؛ يُستعمل فقط لفتح المحادثة عند الضغط على «تواصل».
 */
data class ExchangeAd(
    val id: String,
    val ownerId: String,
    val role: String,
    val wilaya: String,
    val moughataa: String,
    val specialties: List<String>,      // رموز المواد: MATS, PC, AR ...
    val isMoughataaLevel: Boolean,      // مقاطعي أم لا
    val desiredWilayas: List<String>,
    val whatsapp: String,               // أرقام فقط مع رمز البلد، مثل 22236123456
    val updatedAt: Long
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id).put("owner", ownerId).put("role", role)
        .put("wilaya", wilaya).put("moughataa", moughataa)
        .put("specialties", JSONArray(specialties))
        .put("moughataa_level", isMoughataaLevel)
        .put("desired", JSONArray(desiredWilayas))
        .put("whatsapp", whatsapp).put("updated", updatedAt)

    /** متوافق مع إعلاني: ولايته من ولاياتي المطلوبة، وولايتي من ولاياته المطلوبة، ونفس الصفة، وتخصص مشترك. */
    fun matches(mine: ExchangeAd): Boolean =
        ownerId != mine.ownerId &&
            role == mine.role &&
            wilaya in mine.desiredWilayas &&
            mine.wilaya in desiredWilayas &&
            (role != ExchangeRole.TEACHER || specialties.any { it in mine.specialties })

    companion object {
        fun fromJson(o: JSONObject): ExchangeAd = ExchangeAd(
            id = o.getString("id"),
            ownerId = o.optString("owner"),
            role = o.optString("role", ExchangeRole.TEACHER),
            wilaya = o.optString("wilaya"),
            moughataa = o.optString("moughataa"),
            specialties = o.optJSONArray("specialties").toStrings(),
            isMoughataaLevel = o.optBoolean("moughataa_level"),
            desiredWilayas = o.optJSONArray("desired").toStrings(),
            whatsapp = o.optString("whatsapp"),
            updatedAt = o.optLong("updated")
        )

        private fun JSONArray?.toStrings(): List<String> =
            if (this == null) emptyList() else (0 until length()).map { getString(it) }

        /**
         * يوحّد رقم الواتساب: يحذف المسافات والرموز، ويضيف 222 (موريتانيا) للأرقام ذات 8 خانات.
         * يرجع null إن لم يكن رقمًا صالحًا.
         */
        fun normalizePhone(input: String): String? {
            val digits = input.map { c ->
                when (c) { in '٠'..'٩' -> '0' + (c - '٠'); else -> c }
            }.filter { it.isDigit() }.joinToString("").removePrefix("00")
            return when {
                digits.length == 8 && digits[0] in "234" -> "222$digits"
                digits.length == 11 && digits.startsWith("222") && digits[3] in "234" -> digits
                digits.length in 10..15 && !digits.startsWith("222") -> digits // رقم من بلد آخر
                else -> null
            }
        }
    }
}

/** مكان حفظ الإعلانات ومشاركتها بين الأساتذة (خادم التطبيق). */
interface ExchangeRepository {
    /** هل الخادم مربوط؟ */
    val isConnected: Boolean
    suspend fun publish(ad: ExchangeAd)
    suspend fun delete(adId: String)
    suspend fun list(): List<ExchangeAd>
}

/**
 * الخادم الخاص بدفتر الأستاذ DS لم يُنشأ بعد (منفصل تمامًا عن دفتر المعلم).
 * إلى أن يُربط: يُحفظ إعلان الأستاذ على هاتفه، ولا تظهر إعلانات الآخرين.
 */
object NotConnectedExchangeRepository : ExchangeRepository {
    override val isConnected = false
    override suspend fun publish(ad: ExchangeAd) {}
    override suspend fun delete(adId: String) {}
    override suspend fun list(): List<ExchangeAd> = emptyList()
}
