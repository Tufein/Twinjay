package com.tsutsen.platformplayer.feature.settings.impl

import android.content.Context

/** A saved arrangement of the second screen: pages, video tabs and their orders. */
data class LayoutProfile(
    val pages: List<String>,
    val tabs: List<String>,
    val tabOrder: List<String>,
    val pageOrder: List<String>,
)

/** Three slots (home, couch, travel) kept in a small preference file. */
object LayoutProfiles {
    private const val FILE = "layout_profiles"
    val slots = listOf("Home", "Couch", "On the go")

    fun encode(p: LayoutProfile): String =
        listOf(p.pages, p.tabs, p.tabOrder, p.pageOrder).joinToString(";") { it.joinToString(",") }

    fun decode(text: String?): LayoutProfile? {
        if (text.isNullOrBlank()) return null
        val parts = text.split(";", limit = 4)
        if (parts.size < 4) return null
        val lists = parts.map { part -> part.split(",").filter { it.isNotBlank() } }
        return LayoutProfile(lists[0], lists[1], lists[2], lists[3])
    }

    fun load(context: Context, slot: Int): LayoutProfile? =
        decode(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString("slot$slot", null))

    fun save(context: Context, slot: Int, profile: LayoutProfile) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString("slot$slot", encode(profile)).apply()
    }
}
