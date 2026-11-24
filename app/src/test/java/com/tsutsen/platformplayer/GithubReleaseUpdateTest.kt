package com.tsutsen.platformplayer

import com.tsutsen.platformplayer.core.data.update.GithubReleaseUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GithubReleaseUpdateTest {
    @Test fun newerTagsAreDetected() {
        assertTrue(GithubReleaseUpdate.isNewer("v0.8.0-thor.1", "0.7.3-thor.1"))
        assertTrue(GithubReleaseUpdate.isNewer("v0.7.3-thor.2", "0.7.3-thor.1"))
        assertTrue(GithubReleaseUpdate.isNewer("v1.0.0", "0.9.9-thor.9"))
    }

    @Test fun sameOrOlderTagsAreNotUpdates() {
        assertFalse(GithubReleaseUpdate.isNewer("v0.7.3-thor.1", "0.7.3-thor.1"))
        assertFalse(GithubReleaseUpdate.isNewer("v0.7.2-thor.9", "0.7.3-thor.1"))
        assertFalse(GithubReleaseUpdate.isNewer("not-a-version", "0.7.3-thor.1"))
    }

    @Test fun missingTrailingPartsCountAsZero() {
        assertFalse(GithubReleaseUpdate.isNewer("v0.8", "0.8.0"))
        assertTrue(GithubReleaseUpdate.isNewer("v0.8.1", "0.8"))
    }

    @Test fun parsesReleaseJson() {
        val json = """{"url":"x","html_url":"https://github.com/Tufein/Twinjay/releases/tag/v0.8.0-thor.1",
            "author":{"html_url":"https://github.com/someone"},"tag_name":"v0.8.0-thor.1",
            "assets":[{"browser_download_url":"https://github.com/Tufein/Twinjay/releases/download/v0.8.0-thor.1/SHA256SUMS.txt"},
            {"browser_download_url":"https://github.com/Tufein/Twinjay/releases/download/v0.8.0-thor.1/TwinJay-v0.8.0-thor.1-arm64.apk"}]}"""
        val release = GithubReleaseUpdate.parse(json)
        assertNotNull(release)
        assertEquals("v0.8.0-thor.1", release!!.tag)
        assertEquals("https://github.com/Tufein/Twinjay/releases/tag/v0.8.0-thor.1", release.pageUrl)
        assertEquals(
            "https://github.com/Tufein/Twinjay/releases/download/v0.8.0-thor.1/TwinJay-v0.8.0-thor.1-arm64.apk",
            release.apkUrl,
        )
    }

    @Test fun garbageIsRejected() {
        assertNull(GithubReleaseUpdate.parse("{}"))
    }

    @Test fun parsesNotesAndSize() {
        val json = """{"html_url":"https://github.com/x/y/releases/tag/v1","tag_name":"v1",
            "body":"## What's new\r\n- **Faster** start\r\n- See [docs](http://a.b)\r\n\r\n\r\nFull Changelog: x",
            "assets":[{"size":1234,"browser_download_url":"https://github.com/Tufein/Twinjay/releases/download/v1/TwinJay-v1-arm64.apk"}]}"""
        val release = GithubReleaseUpdate.parse(json)!!
        assertEquals("What's new\n• Faster start\n• See docs", release.notes)
        assertEquals(1234L, release.apkSizeBytes)
    }

    @Test fun missingBodyGivesEmptyNotes() {
        val release = GithubReleaseUpdate.parse("""{"tag_name":"v1"}""")!!
        assertEquals("", release.notes)
        assertNull(release.apkSizeBytes)
    }

    @Test fun untrustedApkUrlIsDropped() {
        val json = """{"tag_name":"v1","assets":[{"browser_download_url":"https://evil.example/TwinJay-arm64.apk"},
            {"browser_download_url":"http://github.com/Tufein/Twinjay/releases/download/v1/a.apk"}]}"""
        assertNull(GithubReleaseUpdate.parse(json)!!.apkUrl)
        assertFalse(GithubReleaseUpdate.isTrustedUrl("https://github.com/Tufein/Other/a.apk"))
        assertFalse(GithubReleaseUpdate.isTrustedUrl("https://github.com/Tufein/Twinjay/../Other/a.apk"))
        assertTrue(GithubReleaseUpdate.isTrustedUrl("https://github.com/Tufein/Twinjay/releases/download/v1/a.apk"))
    }

    @Test fun findsChecksumsAsset() {
        val base = "https://github.com/Tufein/Twinjay/releases/download/v1/"
        val json = """{"tag_name":"v1","assets":[{"name":"SHA256SUMS.txt","browser_download_url":"${base}SHA256SUMS.txt"},
            {"name":"a.apk","size":9,"browser_download_url":"${base}a-arm64.apk"}]}"""
        val r = GithubReleaseUpdate.parse(json)!!
        assertEquals("${base}SHA256SUMS.txt", r.checksumsUrl)
        assertEquals(9L, r.apkSizeBytes)
    }

    @Test fun readsChecksumLines() {
        val h = "a".repeat(64)
        val text = "$h  TwinJay-v1-arm64.apk\n${"b".repeat(64)}  LICENSE.md\n"
        assertEquals(h, GithubReleaseUpdate.checksumFor(text, "TwinJay-v1-arm64.apk"))
        assertNull(GithubReleaseUpdate.checksumFor(text, "other.apk"))
        assertNull(GithubReleaseUpdate.checksumFor("zz  x.apk", "x.apk"))
    }

    @Test fun hashesStreams() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            GithubReleaseUpdate.sha256Hex("abc".byteInputStream()),
        )
    }

    @Test fun malformedJsonIsRejected() {
        assertNull(GithubReleaseUpdate.parse("{\"tag_name\": \"v1\""))
        assertNull(GithubReleaseUpdate.parse("[]"))
    }
}
