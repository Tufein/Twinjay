package com.tsutsen.platformplayer.core.data.player

import com.tsutsen.platformplayer.core.model.ContentItem

/**
 * Placement rules for the queue's "Play next" action. Kept separate from the
 * repository so the ordering can be unit tested without an ExoPlayer.
 */
object PlaybackQueueOrder {
    /**
     * Returns [queue] with [item] directly after the current video. An item
     * that was already queued moves up instead of being duplicated. When
     * there is no current video (or it is not in the queue) the item goes
     * first, so it plays next.
     */
    fun insertNext(
        queue: List<ContentItem>,
        currentUrl: String?,
        item: ContentItem,
    ): List<ContentItem> {
        val rest = queue.filterNot { it.url == item.url }
        val currentIndex = rest.indexOfFirst { it.url == currentUrl }
        val insertAt = if (currentIndex >= 0) currentIndex + 1 else 0
        return rest.toMutableList().apply { add(insertAt, item) }
    }
}

/** Shuffling of the part of the queue that has not played yet. */
object QueueShuffle {
    /**
     * Returns [queue] with everything after the current video in random
     * order. The current video (and anything before it) keeps its place, so
     * shuffling never changes what is playing. Without a current video in
     * the queue the whole queue is shuffled.
     */
    fun shuffleUpcoming(
        queue: List<ContentItem>,
        currentUrl: String?,
        random: kotlin.random.Random = kotlin.random.Random.Default,
    ): List<ContentItem> {
        val currentIndex = queue.indexOfFirst { it.url == currentUrl }
        val head = queue.take(currentIndex + 1)
        return head + queue.drop(currentIndex + 1).shuffled(random)
    }
}

/** Arithmetic for "+15 min" on a running sleep timer. */
object SleepExtend {
    /**
     * Minutes to set the timer to when [addMinutes] is added to what is left
     * ([remainingMs]). Partial minutes round up so the timer never gets
     * shorter than asked; without a running timer ([remainingMs] <= 0) it is
     * simply [addMinutes].
     */
    fun totalMinutes(remainingMs: Long, addMinutes: Int): Int {
        if (addMinutes <= 0) return 0
        if (remainingMs <= 0L) return addMinutes
        val left = ((remainingMs + 59_999L) / 60_000L).toInt()
        return left + addMinutes
    }
}

/** Naming for "Save queue as playlist". */
object QueuePlaylistName {
    /** "Queue 2026-11-28 14:05"; the time comes in as numbers so this stays testable. */
    fun of(year: Int, month: Int, day: Int, hour: Int, minute: Int): String =
        "Queue %04d-%02d-%02d %02d:%02d".format(java.util.Locale.US, year, month, day, hour, minute)
}
