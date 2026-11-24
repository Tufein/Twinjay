package com.tsutsen.platformplayer.twinjay

import com.tsutsen.platformplayer.core.data.util.MiniJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MiniJsonTest {
    @Test fun parsesNestedValues() {
        val v = MiniJson.asObject(MiniJson.parse("""{"a":1,"b":[true,null,"x\nA"],"c":{"d":-2.5}}"""))!!
        assertEquals(1L, v["a"])
        assertEquals(listOf(true, null, "x\nA"), MiniJson.asArray(v["b"]))
        assertEquals(-2.5, MiniJson.asObject(v["c"])!!["d"])
    }

    @Test fun rejectsMalformedInput() {
        assertNull(MiniJson.parse(""))
        assertNull(MiniJson.parse("{"))
        assertNull(MiniJson.parse("""{"a":}"""))
        assertNull(MiniJson.parse("""{"a":1} trailing"""))
        assertNull(MiniJson.parse("\"unterminated"))
    }

    @Test fun refusesAbsurdNesting() {
        assertNull(MiniJson.parse("[".repeat(500) + "]".repeat(500)))
    }

    @Test fun handlesEmptyContainers() {
        assertEquals(emptyMap<String, Any?>(), MiniJson.parse("{}"))
        assertEquals(emptyList<Any?>(), MiniJson.parse(" [ ] "))
    }
}
