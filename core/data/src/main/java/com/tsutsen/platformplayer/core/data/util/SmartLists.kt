package com.tsutsen.platformplayer.core.data.util

import android.content.Context

/** Saved filters ("smart lists") for the library lists: a tag, a time budget and a search text. */
object SmartLists {
    private const val FILE = "smart_lists"
    private const val KEY = "rules"

    data class Rule(val tag: String, val budgetMinutes: Int, val query: String) {
        val label: String
            get() =
                listOfNotNull(
                    tag.takeIf { it.isNotEmpty() }?.let { "#$it" },
                    budgetMinutes.takeIf { it > 0 }?.let { "$it min" },
                    query.takeIf { it.isNotEmpty() }?.let { "\"$it\"" },
                ).joinToString(" ")
    }

    fun encode(rules: List<Rule>): String =
        rules.joinToString("\n") { "${it.tag}|${it.budgetMinutes}|${it.query.replace('\n', ' ')}" }

    fun decode(text: String?): List<Rule> =
        (text ?: "")
            .lines()
            .mapNotNull { line ->
                val p = line.split("|", limit = 3)
                if (p.size < 3) return@mapNotNull null
                Rule(p[0], p[1].toIntOrNull() ?: 0, p[2]).takeIf { it.label.isNotEmpty() }
            }.distinct()

    fun get(context: Context): List<Rule> =
        decode(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, ""))

    fun put(context: Context, rules: List<Rule>) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, encode(rules)).apply()
    }
}
