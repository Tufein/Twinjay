package com.tsutsen.platformplayer.importer

import org.junit.Assert.assertEquals
import org.junit.Test

class TakeoutCsvTest {
    @Test
    fun parsesTakeoutExportWithHeader() {
        val csv = "Channel Id,Channel Url,Channel Title\n" +
            "UCaaaaaaaaaaaaaaaaaaaaaa,http://www.youtube.com/channel/UCaaaaaaaaaaaaaaaaaaaaaa,\"Foo, Bar\"\n" +
            "UCbbbbbbbbbbbbbbbbbbbbbb,http://www.youtube.com/channel/UCbbbbbbbbbbbbbbbbbbbbbb,Baz\n"
        val entries = TakeoutCsv.parse(csv)
        assertEquals(2, entries.size)
        assertEquals("https://www.youtube.com/channel/UCaaaaaaaaaaaaaaaaaaaaaa", entries[0].url)
        assertEquals("Foo, Bar", entries[0].title)
        assertEquals("Baz", entries[1].title)
    }

    @Test
    fun fallsBackToChannelIdWhenUrlIsMissing() {
        val csv = "Channel Id,Channel Url,Channel Title\nUCccccccccccccccccccccc1,,Nope\n"
        assertEquals(
            listOf("https://www.youtube.com/channel/UCccccccccccccccccccccc1"),
            TakeoutCsv.parse(csv).map { it.url },
        )
    }

    @Test
    fun parsesPlainListsAndDeduplicates() {
        val text = "https://www.youtube.com/@one\nhttps://www.youtube.com/@one\nUCddddddddddddddddddddd1\n\n"
        assertEquals(
            listOf("https://www.youtube.com/@one", "https://www.youtube.com/channel/UCddddddddddddddddddddd1"),
            TakeoutCsv.parse(text).map { it.url },
        )
    }

    @Test
    fun emptyInputGivesNothing() {
        assertEquals(0, TakeoutCsv.parse("").size)
        assertEquals(0, TakeoutCsv.parse("﻿Channel Id,Channel Url,Channel Title\n").size)
    }
}
