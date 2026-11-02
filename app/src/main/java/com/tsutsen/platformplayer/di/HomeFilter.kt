package com.tsutsen.platformplayer.di

import com.tsutsen.platformplayer.core.model.Card
import com.tsutsen.platformplayer.core.model.VideoCard

/** Hides home feed videos whose title or channel contains one of the user's words. */
object HomeFilter {
    fun parseWords(text: String): List<String> =
        text.split(',', '\n').map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct()

    fun shouldHide(title: String, author: String?, words: List<String>): Boolean {
        if (words.isEmpty()) return false
        val t = title.lowercase()
        val a = author?.lowercase().orEmpty()
        return words.any { t.contains(it) || a.contains(it) }
    }

    fun apply(cards: List<Card>, words: List<String>): List<Card> =
        if (words.isEmpty()) cards
        else cards.filterNot { it is VideoCard && shouldHide(it.title, it.author, words) }
}
