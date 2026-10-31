package com.elyedali.daftarustadh.exchange

import org.json.JSONArray
import org.json.JSONObject

/** صفة صاحب الإعلان في مؤسسته. */
object ExchangeRole {
    const val TEACHER = "teacher"               // أستاذ
    const val STUDIES_DIRECTOR = "director"     // مدير دروس
    const val GENERAL_SUPERVISOR = "supervisor" // مراقب عام
    val ALL = listOf(TEACHER, STUDIES_DIRECTOR, GENERAL_SUPERVISOR)
}

/** نوع العرض. */
object OfferType {
    const val FRIENDLY = "friendly" // تبادل ودي دون دفع
    const val SELL = "sell"         // بيع مقعد مكان عمله
    const val BUY = "buy"           // بحث عن شراء مقعد
    val ALL = listOf(FRIENDLY, SELL, BUY)
}

/**
 * منشور تبادل. لا يحتوي اسمًا ولا أي معلومة شخصية تُعرض.
 * رقم الواتساب محفوظ مع المنشور لكنه لا يُعرض أبدًا؛ يُستعمل فقط لفتح المحادثة عند الضغط على «تواصل».
 */
data class ExchangeAd(
    val id: String,
    val ownerId: String,
    val offerType: String,
    val priceAmount: String,            // المبلغ بالأوقية، أو فارغ
    val isNegotiable: Boolean,          // المقابل المالي للنقاش في الخاص
    val role: String,
    val specialties: List<String>,      // رموز المواد: MATS, PC, AR ...
    val isMoughataaLevel: Boolean,      // مقاطعي
    val wilaya: String,
    val moughataa: String,
    val whatsapp: String,               // أرقام فقط مع رمز البلد، مثل 22236123456
    val desiredWilayas: List<String>,
    val createdAt: Long,
    val isSynced: Boolean = false       // وصل إلى الخادم أم لا
) {
    fun toJson(): JSONObject = JSONObject()
        .put("id", id).put("owner", ownerId)
        .put("offer_type", offerType).put("price", priceAmount).put("negotiable", isNegotiable)
        .put("role", role)
        .put("specialties", JSONArray(specialties))
        .put("moughataa_level", isMoughataaLevel)
        .put("wilaya", wilaya).put("moughataa", moughataa)
        .put("whatsapp", whatsapp)
        .put("desired", JSONArray(desiredWilayas))
        .put("created", createdAt)
        .put("synced", isSynced)

    companion object {
        fun fromJson(o: JSONObject): ExchangeAd = ExchangeAd(
            id = o.getString("id"),
            ownerId = o.optString("owner"),
            offerType = o.optString("offer_type", OfferType.FRIENDLY),
            priceAmount = o.optString("price"),
            isNegotiable = o.optBoolean("negotiable"),
            role = o.optString("role", ExchangeRole.TEACHER),
            specialties = o.optJSONArray("specialties").toStrings(),
            isMoughataaLevel = o.optBoolean("moughataa_level"),
            wilaya = o.optString("wilaya"),
            moughataa = o.optString("moughataa"),
            whatsapp = o.optString("whatsapp"),
            desiredWilayas = o.optJSONArray("desired").toStrings(),
            createdAt = o.optLong("created"),
            isSynced = o.optBoolean("synced")
        )

        fun listToJson(list: List<ExchangeAd>): String = JSONArray(list.map { it.toJson() }).toString()

        fun listFromJson(text: String?): List<ExchangeAd> {
            if (text.isNullOrBlank()) return emptyList()
            return runCatching {
                val arr = JSONArray(text)
                (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
            }.getOrDefault(emptyList())
        }

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
                else -> null
            }
        }

        /** الرقم المحلي (8 خانات) لعرضه لصاحبه فقط عند التعديل. */
        fun localPhone(stored: String): String = stored.removePrefix("222")
    }
}

/** مكان حفظ المنشورات ومشاركتها بين الأساتذة (خادم التطبيق). */
interface ExchangeRepository {
    /** هل الخادم مربوط؟ */
    val isConnected: Boolean
    suspend fun publish(ad: ExchangeAd): Boolean
    suspend fun delete(adId: String)
    suspend fun list(): List<ExchangeAd>
}

/**
 * الخادم الخاص بدفتر الأستاذ DS لم يُنشأ بعد (منفصل تمامًا عن دفتر المعلم).
 * إلى أن يُربط: تُحفظ منشورات الأستاذ على هاتفه، ولا تظهر منشورات الآخرين.
 */
object NotConnectedExchangeRepository : ExchangeRepository {
    override val isConnected = false
    override suspend fun publish(ad: ExchangeAd) = false
    override suspend fun delete(adId: String) {}
    override suspend fun list(): List<ExchangeAd> = emptyList()
}
