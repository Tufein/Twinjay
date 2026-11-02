package com.tsutsen.platformplayer.core.data.groups

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Bridge between the engine's subscription groups (app module) and the
 * subscriptions feed (feature module). The app sets [provider] at startup.
 */
object ChannelGroups {
    data class Group(val id: String, val name: String, val urls: Set<String>)

    @Volatile var provider: () -> List<Group> = { emptyList() }

    /** Id of the group the feed is narrowed to, or null for all channels. */
    val selected = MutableStateFlow<String?>(null)

    fun groups(): List<Group> = provider().filter { it.urls.isNotEmpty() }.sortedBy { it.name.lowercase() }

    /** Pure helper: is a video from [authorUrl] part of [group]? A null group means everything. */
    fun matches(group: Group?, authorUrl: String?): Boolean =
        group == null || (authorUrl != null && authorUrl in group.urls)
}
