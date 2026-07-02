package com.neko.m003_api22_pngPalette

import android.util.Base64
import org.json.JSONObject
import java.util.zip.CRC32

/**
 * Core logic side ("Engine", mdID=0x18).
 * Owns the embedded PNG-8 image (see EmbeddedPng.kt) and handles JSON
 * commands from the UI side. PNG palette (PLTE) editing is done directly
 * on the binary: rewrite 3 bytes of the target palette entry and
 * recompute the chunk CRC. No re-encode, so it works on API 22.
 *
 * Commands:
 *   {"cmd":"raw"}                                  -> unmodified PNG   (button A)
 *   {"cmd":"edit","index":n,"r":n,"g":n,"b":n}     -> palette-edited PNG (button B)
 *   (button C = buffer clear is handled entirely on the UI side)
 */
class Engine {

    data class Result(val png: ByteArray? = null, val log: String? = null)

    private val original: ByteArray by lazy {
        Base64.decode(EmbeddedPng.BASE64, Base64.DEFAULT)
    }

    fun handleCommand(cmd: JSONObject): Result {
        return when (val name = cmd.optString("cmd")) {
            "raw" -> Result(
                png = original,
                log = "cmd=raw : sent original PNG (${original.size} bytes)"
            )
            "edit" -> {
                val index = cmd.getInt("index").coerceIn(0, 15)
                val r = cmd.getInt("r").coerceIn(0, 255)
                val g = cmd.getInt("g").coerceIn(0, 255)
                val b = cmd.getInt("b").coerceIn(0, 255)
                val edited = editPalette(original, index, r, g, b)
                Result(
                    png = edited,
                    log = "cmd=edit : palette[$index] = ($r,$g,$b), sent ${edited.size} bytes"
                )
            }
            else -> Result(log = "unknown cmd: $name")
        }
    }

    /**
     * Rewrites one entry of the PLTE chunk and fixes its CRC32.
     * PNG layout: 8-byte signature, then chunks of
     * [4B length][4B type][data...][4B CRC]. CRC covers type+data.
     */
    private fun editPalette(src: ByteArray, index: Int, r: Int, g: Int, b: Int): ByteArray {
        val out = src.copyOf()
        var pos = 8 // skip PNG signature
        while (pos + 8 <= out.size) {
            val len = readU32(out, pos)
            val type = String(out, pos + 4, 4, Charsets.US_ASCII)
            if (type == "PLTE") {
                val dataStart = pos + 8
                val entry = dataStart + index * 3
                require(entry + 3 <= dataStart + len) { "palette index out of range" }
                out[entry] = r.toByte()
                out[entry + 1] = g.toByte()
                out[entry + 2] = b.toByte()
                // recompute CRC over type + data
                val crc = CRC32()
                crc.update(out, pos + 4, 4 + len)
                writeU32(out, dataStart + len, crc.value)
                return out
            }
            pos += 12 + len
        }
        throw IllegalStateException("PLTE chunk not found")
    }

    private fun readU32(a: ByteArray, p: Int): Int =
        ((a[p].toInt() and 0xFF) shl 24) or
        ((a[p + 1].toInt() and 0xFF) shl 16) or
        ((a[p + 2].toInt() and 0xFF) shl 8) or
        (a[p + 3].toInt() and 0xFF)

    private fun writeU32(a: ByteArray, p: Int, v: Long) {
        a[p] = ((v shr 24) and 0xFF).toByte()
        a[p + 1] = ((v shr 16) and 0xFF).toByte()
        a[p + 2] = ((v shr 8) and 0xFF).toByte()
        a[p + 3] = (v and 0xFF).toByte()
    }
}
