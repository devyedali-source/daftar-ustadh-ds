package com.elyedali.daftarustadh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elyedali.daftarustadh.R
import com.elyedali.daftarustadh.data.ClassEntity
import com.elyedali.daftarustadh.data.ClassGenderCount
import com.elyedali.daftarustadh.data.Gender

private data class Counts(val total: Int, val boys: Int, val girls: Int) {
    val unknown get() = total - boys - girls
}

/** إحصائيات المؤسسة: عدد الأقسام والطلاب والأولاد والبنات، ثم لكل مستوى ولكل قسم. */
@Composable
fun StatsScreen(
    schoolName: String,
    schoolYear: String,
    classes: List<ClassEntity>,
    genderCounts: List<ClassGenderCount>,
    onBack: () -> Unit
) {
    fun countsFor(ids: Set<String>): Counts {
        val rows = genderCounts.filter { it.classId in ids }
        return Counts(
            total = rows.sumOf { it.count },
            boys = rows.filter { it.gender == Gender.BOY }.sumOf { it.count },
            girls = rows.filter { it.gender == Gender.GIRL }.sumOf { it.count }
        )
    }
    val all = countsFor(classes.map { it.id }.toSet())
    val byLevel = classes.groupBy { it.levelCode }.toSortedMap()
    val boyColor = MaterialTheme.colorScheme.primary
    val girlColor = MaterialTheme.colorScheme.tertiary

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { ScreenHeader(stringResource(R.string.statistics), onBack, listOf(schoolName, schoolYear)) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(classes.size.toString(), stringResource(R.string.stat_classes), MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                    StatTile(all.total.toString(), stringResource(R.string.stat_students), MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile(all.boys.toString(), stringResource(R.string.stat_boys), boyColor, Modifier.weight(1f))
                    StatTile(all.girls.toString(), stringResource(R.string.stat_girls), girlColor, Modifier.weight(1f))
                }
            }
            if (all.unknown > 0) item {
                Text(stringResource(R.string.stat_unknown_gender, all.unknown), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (all.total > 0) item { GenderBar(all, boyColor, girlColor) }

            item { SectionTitle(stringResource(R.string.stat_by_level)) }
            items(byLevel.entries.toList(), key = { "lvl-" + it.key }) { (level, list) ->
                val c = countsFor(list.map { it.id }.toSet())
                CountsRow(level, levelDescription(level), list.size, c)
            }

            item { SectionTitle(stringResource(R.string.stat_by_class)) }
            items(classes, key = { it.id }) { cl ->
                val c = countsFor(setOf(cl.id))
                CountsRow(cl.name, null, null, c)
            }
        }
    }
}

@Composable
private fun CountsRow(title: String, subtitle: String?, classCount: Int?, c: Counts) {
    val boyColor = MaterialTheme.colorScheme.primary
    val girlColor = MaterialTheme.colorScheme.tertiary
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    val sub = listOfNotNull(subtitle, classCount?.let { stringResource(R.string.stat_n_classes, it) }).joinToString(" — ")
                    if (sub.isNotEmpty()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Num(c.total.toString(), stringResource(R.string.stat_students), MaterialTheme.colorScheme.onSurface)
                Num(c.boys.toString(), stringResource(R.string.stat_boys), boyColor)
                Num(c.girls.toString(), stringResource(R.string.stat_girls), girlColor)
            }
            if (c.total > 0) GenderBar(c, boyColor, girlColor)
        }
    }
}

@Composable
private fun Num(v: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(Modifier.width(58.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(v, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

/** شريط نسبة الأولاد والبنات. */
@Composable
private fun GenderBar(c: Counts, boyColor: androidx.compose.ui.graphics.Color, girlColor: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (c.boys > 0) Box(Modifier.weight(c.boys.toFloat()).fillMaxHeight().background(boyColor))
        if (c.girls > 0) Box(Modifier.weight(c.girls.toFloat()).fillMaxHeight().background(girlColor))
        if (c.unknown > 0) Box(Modifier.weight(c.unknown.toFloat()).fillMaxHeight())
    }
}
