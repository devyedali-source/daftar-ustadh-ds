package com.elyedali.daftarustadh.exchange

import com.elyedali.daftarustadh.auth.DsAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

/**
 * منشورات التبادل على خادم DS (المجموعة ds_exchanges). المنشور يُرسل غير مفعّل،
 * ولا يظهر للعامة إلا بعد أن يفعّله صاحب التطبيق من لوحة التحكم (بعد دفع 200 أوقية).
 */
object FirestoreExchangeRepository : ExchangeRepository {
    private val col get() = FirebaseFirestore.getInstance().collection("ds_exchanges")
    override val isConnected: Boolean get() = DsAuth.user != null

    override suspend fun publish(ad: ExchangeAd): Boolean {
        val u = DsAuth.user ?: return false
        return withTimeoutOrNull(15_000) {
            try {
                val ref = col.document(ad.id)
                val old = ref.get().await()
                val active = old.exists() && old.getBoolean("active") == true
                val data = HashMap<String, Any>()
                ad.toJson().let { j -> j.keys().forEach { k -> if (k != "synced" && k != "approved") data[k] = j.get(k).let { v -> if (v is org.json.JSONArray) (0 until v.length()).map { v.getString(it) } else v } } }
                data["uid"] = u.uid
                data["email"] = u.email ?: ""
                data["active"] = active
                data["code"] = ad.code
                data["updatedAt"] = FieldValue.serverTimestamp()
                ref.set(data).await()
                true
            } catch (e: Exception) { false }
        } ?: false
    }

    override suspend fun delete(adId: String) {
        try { withTimeoutOrNull(15_000) { col.document(adId).delete().await() } } catch (e: Exception) { }
    }

    /** المنشورات المفعّلة فقط (عروض التبادل). */
    override suspend fun list(): List<ExchangeAd> = try {
        col.whereEqualTo("active", true).get().await().documents.mapNotNull { d -> fromDoc(d.data ?: return@mapNotNull null, true) }
    } catch (e: Exception) { emptyList() }

    /** حالة منشوراتي على الخادم: معرّف المنشور → مفعّل أم لا. */
    suspend fun myStatus(): Map<String, Boolean>? {
        val u = DsAuth.user ?: return null
        return try {
            col.whereEqualTo("uid", u.uid).get().await().documents.associate { it.id to (it.getBoolean("active") == true) }
        } catch (e: Exception) { null }
    }

    fun fromDoc(m: Map<String, Any?>, approved: Boolean): ExchangeAd? = try {
        val o = JSONObject()
        m.forEach { (k, v) ->
            when (v) {
                is List<*> -> o.put(k, org.json.JSONArray(v.map { it.toString() }))
                is Number, is Boolean, is String -> o.put(k, v)
                else -> {}
            }
        }
        ExchangeAd.fromJson(o.put("synced", true).put("approved", approved))
    } catch (e: Exception) { null }
}
