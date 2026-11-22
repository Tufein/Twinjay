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
