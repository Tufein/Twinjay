package com.tsutsen.platformplayer.core.data.util

/**
 * Small, dependency-free JSON reader (objects -> Map, arrays -> List, numbers -> Long/Double,
 * strings, booleans, null). Used where `org.json` is not available in plain JVM unit tests.
 * Returns null instead of throwing on malformed input.
 */
object MiniJson {
    fun parse(text: String): Any? =
        try {
            val p = Parser(text)
            p.skipWs()
            val v = p.value()
            p.skipWs()
            if (p.atEnd()) v else null
        } catch (_: IllegalArgumentException) {
            null
        }

    @Suppress("UNCHECKED_CAST")
    fun asObject(v: Any?): Map<String, Any?>? = v as? Map<String, Any?>

    @Suppress("UNCHECKED_CAST")
    fun asArray(v: Any?): List<Any?>? = v as? List<Any?>

    private class Parser(private val s: String) {
        private var i = 0
        private var depth = 0

        fun atEnd() = i >= s.length

        fun skipWs() {
            while (i < s.length && s[i].isWhitespace()) i++
        }

        private fun fail(): Nothing = throw IllegalArgumentException("bad json at $i")

        private fun expect(c: Char) {
            if (i >= s.length || s[i] != c) fail()
            i++
        }

        fun value(): Any? {
            if (i >= s.length) fail()
            return when (s[i]) {
                '{' -> obj()
                '[' -> arr()
                '"' -> str()
                't' -> lit("true", true)
                'f' -> lit("false", false)
                'n' -> lit("null", null)
                else -> num()
            }
        }

        private fun lit(word: String, v: Any?): Any? {
            if (!s.startsWith(word, i)) fail()
            i += word.length
            return v
        }

        private fun num(): Any {
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] in "+-.eE")) i++
            val t = s.substring(start, i)
            if (t.isEmpty()) fail()
            return t.toLongOrNull() ?: t.toDoubleOrNull() ?: fail()
        }

        private fun str(): String {
            expect('"')
            val sb = StringBuilder()
            while (true) {
                if (i >= s.length) fail()
                val c = s[i++]
                when (c) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        if (i >= s.length) fail()
                        when (val e = s[i++]) {
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'u' -> {
                                if (i + 4 > s.length) fail()
                                sb.append((s.substring(i, i + 4).toIntOrNull(16) ?: fail()).toChar())
                                i += 4
                            }
                            '"', '\\', '/' -> sb.append(e)
                            else -> fail()
                        }
                    }
                    else -> sb.append(c)
                }
            }
        }

        private fun arr(): List<Any?> {
            if (++depth > 64) fail()
            expect('[')
            val out = mutableListOf<Any?>()
            skipWs()
            if (i < s.length && s[i] == ']') {
                i++
                depth--
                return out
            }
            while (true) {
                skipWs()
                out.add(value())
                skipWs()
                if (i >= s.length) fail()
                if (s[i] == ',') { i++; continue }
                expect(']')
                depth--
                return out
            }
        }

        private fun obj(): Map<String, Any?> {
            if (++depth > 64) fail()
            expect('{')
            val out = LinkedHashMap<String, Any?>()
            skipWs()
            if (i < s.length && s[i] == '}') {
                i++
                depth--
                return out
            }
            while (true) {
                skipWs()
                val k = str()
                skipWs()
                expect(':')
                skipWs()
                out[k] = value()
                skipWs()
                if (i >= s.length) fail()
                if (s[i] == ',') { i++; continue }
                expect('}')
                depth--
                return out
            }
        }
    }
}
