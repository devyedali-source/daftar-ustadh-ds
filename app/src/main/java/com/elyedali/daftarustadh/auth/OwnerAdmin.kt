package com.elyedali.daftarustadh.auth

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/** عمليات لوحة التحكم (لحساب صاحب التطبيق فقط؛ قواعد الخادم تمنع غيره). */
object OwnerAdmin {
    private val fs get() = FirebaseFirestore.getInstance()

    data class Request(
        val id: String, val code: String, val uid: String, val email: String, val name: String,
        val app: String, val institutionKey: String, val institutionName: String, val wilaya: String,
        val schoolYear: String, val classIds: List<String>, val classNames: List<String>,
        val amount: Int, val status: String, val createdAt: Long
    )

    data class YearStat(val year: String, val admin: Int, val teacher: Int, val classes: Int, val amount: Int)

    data class Stats(
        val adminUsers: Long, val teacherUsers: Long,
        val years: List<YearStat>,
        val exchangesActive: Long, val exchangesInactive: Long,
        val pending: Int
    )

    data class Exchange(val id: String, val code: String, val email: String, val wilaya: String, val specialties: List<String>, val active: Boolean)

    private suspend fun count(q: Query): Long = q.count().get(AggregateSource.SERVER).await().count

    suspend fun stats(): Stats {
        val users = fs.collection("ds_users")
        val ex = fs.collection("ds_exchanges")
        val subs = fs.collection("ds_subscriptions").get().await().documents
        val years = subs.groupBy { it.getString("schoolYear").orEmpty() }.map { (y, l) ->
            YearStat(y,
                l.count { it.getString("app") == DsSubscriptions.APP_ADMIN },
                l.count { it.getString("app") == DsSubscriptions.APP_TEACHER },
                l.sumOf { (it.get("classIds") as? List<*>)?.size ?: 0 },
                l.sumOf { (it.getLong("amount") ?: 0L).toInt() })
        }.sortedByDescending { it.year }
        return Stats(
            adminUsers = count(users.whereEqualTo("apps.admin", true)),
            teacherUsers = count(users.whereEqualTo("apps.teacher", true)),
            years = years,
            exchangesActive = count(ex.whereEqualTo("active", true)),
            exchangesInactive = count(ex.whereEqualTo("active", false)),
            pending = count(fs.collection("ds_activation_requests").whereEqualTo("status", "pending")).toInt()
        )
    }

    suspend fun requests(status: String): List<Request> =
        fs.collection("ds_activation_requests").whereEqualTo("status", status).get().await().documents.map { d ->
            Request(d.id, d.getString("code") ?: d.id.take(6).uppercase(), d.getString("uid").orEmpty(), d.getString("email").orEmpty(),
                d.getString("name").orEmpty(), d.getString("app").orEmpty(), d.getString("institutionKey").orEmpty(),
                d.getString("institutionName").orEmpty(), d.getString("wilaya").orEmpty(), d.getString("schoolYear").orEmpty(),
                (d.get("classIds") as? List<*>)?.map { it.toString() }.orEmpty(), (d.get("classNames") as? List<*>)?.map { it.toString() }.orEmpty(),
                (d.getLong("amount") ?: 0L).toInt(), d.getString("status").orEmpty(), d.getTimestamp("createdAt")?.toDate()?.time ?: 0L)
        }.sortedByDescending { it.createdAt }

    suspend fun request(id: String): Request? = try {
        val d = fs.collection("ds_activation_requests").document(id).get().await()
        if (!d.exists()) null else requests(d.getString("status") ?: "pending").firstOrNull { it.id == id }
    } catch (e: Exception) { null }

    /** معرّف الاشتراك: الحساب + التطبيق + المؤسسة (للإدارة) + السنة الدراسية. */
    private fun subId(r: Request) = listOf(r.uid, r.app, r.institutionKey.ifBlank { "T" }, r.schoolYear)
        .joinToString("_").replace(Regex("[/\\s]"), "-").take(1400)

    /** قبول الطلب: يُنشئ الاشتراك (أو يضيف الأقسام إلى اشتراك المؤسسة نفسها) وينتهي آخر السنة الدراسية. */
    suspend fun approve(r: Request) {
        val batch = fs.batch()
        val sub = fs.collection("ds_subscriptions").document(subId(r))
        val data = hashMapOf<String, Any>(
            "uid" to r.uid, "email" to r.email, "name" to r.name, "app" to r.app,
            "institutionKey" to r.institutionKey, "institutionName" to r.institutionName, "wilaya" to r.wilaya,
            "schoolYear" to r.schoolYear, "expiresAt" to DsSubscriptions.yearEnd(r.schoolYear),
            "amount" to FieldValue.increment(r.amount.toLong()),
            "approvedAt" to FieldValue.serverTimestamp(), "lastRequest" to r.id
        )
        if (r.classIds.isNotEmpty()) data["classIds"] = FieldValue.arrayUnion(*r.classIds.toTypedArray())
        batch.set(sub, data, SetOptions.merge())
        batch.update(fs.collection("ds_activation_requests").document(r.id),
            mapOf("status" to "approved", "decidedAt" to FieldValue.serverTimestamp()))
        batch.commit().await()
    }

    suspend fun reject(r: Request) {
        fs.collection("ds_activation_requests").document(r.id)
            .update(mapOf("status" to "rejected", "decidedAt" to FieldValue.serverTimestamp())).await()
    }

    suspend fun exchanges(active: Boolean): List<Exchange> =
        fs.collection("ds_exchanges").whereEqualTo("active", active).get().await().documents.map { d ->
            Exchange(d.id, d.getString("code") ?: d.id.take(8).uppercase(), d.getString("email").orEmpty(), d.getString("wilaya").orEmpty(),
                (d.get("specialties") as? List<*>)?.map { it.toString() }.orEmpty(), active)
        }

    suspend fun setExchangeActive(id: String, active: Boolean) {
        fs.collection("ds_exchanges").document(id).update(mapOf("active" to active, "activatedAt" to FieldValue.serverTimestamp())).await()
    }
}
