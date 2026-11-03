package com.elyedali.daftarustadh.data

import android.content.Context
import org.json.JSONObject

/**
 * التخطيط السنوي للإعدادية (من وثائق الوزارة): لكل مادة ومستوى، أسطر بالأسبوع (أو بالشهر للفرنسية).
 * k: lesson درس، eval تقويم ودعم، diag تقويم تشخيصي، start استقبال، exam اختبار فصلي، holiday عطلة،
 * activity أنشطة لا صفية، final التقويم النهائي.
 */
data class PlanRow(val label: String, val week: Int, val term: Int, val unit: String, val kind: String, val items: List<String>)

object AnnualPlans {
    private var cache: JSONObject? = null

    private fun root(context: Context): JSONObject? = cache ?: runCatching {
        JSONObject(context.assets.open("planning/planning.json").bufferedReader().use { it.readText() })
    }.getOrNull()?.also { cache = it }

    /** مواد هذا المستوى التي وصل تخطيطها. */
    fun subjectsWithPlan(context: Context, levelCode: String): Set<String> {
        val plans = root(context)?.optJSONObject("plans") ?: return emptySet()
        return plans.keys().asSequence().filter { plans.getJSONObject(it).has(levelCode) }.toSet()
    }

    fun source(context: Context, subject: String): String = root(context)?.optJSONObject("source")?.optString(subject).orEmpty()

    /**
     * أسطر تخطيط المادة للمستوى. أسبوع العطلة يأتي بعد أسبوع اختبار الفصل (الأول والثاني):
     * إن لم يكن في الوثيقة، يُضاف بعده سطر «عطلة».
     */
    fun rows(context: Context, subject: String, levelCode: String): List<PlanRow> {
        val arr = root(context)?.optJSONObject("plans")?.optJSONObject(subject)?.optJSONArray(levelCode) ?: return emptyList()
        val raw = (0 until arr.length()).map { k ->
            val o = arr.getJSONObject(k)
            val items = o.getJSONArray("i")
            PlanRow(o.optString("l"), o.optInt("w"), o.optInt("t"), o.optString("u"), o.optString("k", "lesson"),
                (0 until items.length()).map { items.getString(it) })
        }
        val out = ArrayList<PlanRow>()
        raw.forEachIndexed { i, r ->
            out += r
            val next = raw.getOrNull(i + 1)
            if (r.kind == "exam" && r.term in 1..2 && next?.kind != "holiday")
                out += PlanRow("", 0, r.term, "", "holiday", emptyList())
        }
        return out
    }
}
