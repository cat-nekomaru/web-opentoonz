package com.neko.m003_ws_pngpalette

import java.util.zip.CRC32

/**
 * Png8PaletteEditor.kt
 *
 * PNG-8 (パレットカラーPNG) のバイト列を直接パースし、
 * PLTEチャンク内の指定パレット番号(index)のRGB値を書き換えるユーティリティ。
 *
 * Bitmap/Canvas等を経由せず、PNGファイルフォーマットを直接編集するため、
 * 色数・インデックス配置・IDAT(画素データ)は変更されない。
 * = 「ピクセルの並びは無加工、パレットの色定義だけ書き換える」処理。
 *
 * PNGチャンク構造:
 *   [length:4][type:4][data:length][crc32:4]
 */
object Png8PaletteEditor {

    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    )

    data class PaletteChange(
        val index: Int, // 0..255 (本プロジェクトでは uiID=D の運用上 0..15 を想定)
        val r: Int,     // 0..255
        val g: Int,     // 0..255
        val b: Int      // 0..255
    )

    class PngFormatException(message: String) : Exception(message)

    /**
     * src の PLTE チャンクのうち、指定 index の色だけを (r,g,b) に書き換えた
     * 新しいPNGバイト列を返す。IDAT・その他チャンクは一切変更しない。
     */
    fun applyPaletteChange(src: ByteArray, change: PaletteChange): ByteArray {
        require(change.index in 0..255) { "palette index out of range: ${change.index}" }
        for (v in listOf(change.r, change.g, change.b)) {
            require(v in 0..255) { "RGB value out of range: $v" }
        }

        validateSignature(src)

        val out = src.copyOf() // 書き換え用に複製 (元データは無加工のまま保持される)
        var pos = 8 // シグネチャ分をスキップ

        var plteFound = false

        while (pos + 8 <= out.size) {
            val length = readUInt32BE(out, pos)
            val typeOffset = pos + 4
            val type = String(out, typeOffset, 4, Charsets.US_ASCII)
            val dataOffset = pos + 8
            val crcOffset = dataOffset + length

            if (crcOffset + 4 > out.size) {
                throw PngFormatException("chunk overruns buffer: type=$type")
            }

            if (type == "PLTE") {
                plteFound = true
                val numColors = length / 3
                if (change.index >= numColors) {
                    throw PngFormatException(
                        "palette index ${change.index} >= numColors($numColors) in PLTE"
                    )
                }
                val entryOffset = dataOffset + change.index * 3
                out[entryOffset] = change.r.toByte()
                out[entryOffset + 1] = change.g.toByte()
                out[entryOffset + 2] = change.b.toByte()

                // PLTEチャンクのCRC32を再計算 (type + data)
                val crc = CRC32()
                crc.update(out, typeOffset, 4 + length)
                writeUInt32BE(out, crcOffset, crc.value)
            }

            pos = crcOffset + 4
            if (type == "IEND") break
        }

        if (!plteFound) {
            throw PngFormatException("PLTE chunk not found (not a palette/PNG-8 image?)")
        }

        return out
    }

    /** 複数のパレット変更を一括適用する (将来の複数スライダー拡張用) */
    fun applyPaletteChanges(src: ByteArray, changes: List<PaletteChange>): ByteArray {
        var result = src
        for (c in changes) {
            result = applyPaletteChange(result, c)
        }
        return result
    }

    /** 現在のPLTEチャンクから index 番目の色を読み出す (ログ表示などに使用) */
    fun readPaletteColor(src: ByteArray, index: Int): Triple<Int, Int, Int> {
        validateSignature(src)
        var pos = 8
        while (pos + 8 <= src.size) {
            val length = readUInt32BE(src, pos)
            val type = String(src, pos + 4, 4, Charsets.US_ASCII)
            val dataOffset = pos + 8
            if (type == "PLTE") {
                val numColors = length / 3
                require(index < numColors) { "index out of range for PLTE" }
                val o = dataOffset + index * 3
                return Triple(
                    src[o].toInt() and 0xFF,
                    src[o + 1].toInt() and 0xFF,
                    src[o + 2].toInt() and 0xFF
                )
            }
            pos = dataOffset + length + 4
            if (type == "IEND") break
        }
        throw PngFormatException("PLTE chunk not found")
    }

    private fun validateSignature(data: ByteArray) {
        if (data.size < 8) throw PngFormatException("too small to be PNG")
        for (i in 0 until 8) {
            if (data[i] != PNG_SIGNATURE[i]) {
                throw PngFormatException("invalid PNG signature")
            }
        }
    }

    private fun readUInt32BE(buf: ByteArray, offset: Int): Int {
        return ((buf[offset].toInt() and 0xFF) shl 24) or
            ((buf[offset + 1].toInt() and 0xFF) shl 16) or
            ((buf[offset + 2].toInt() and 0xFF) shl 8) or
            (buf[offset + 3].toInt() and 0xFF)
    }

    private fun writeUInt32BE(buf: ByteArray, offset: Int, value: Long) {
        buf[offset] = ((value shr 24) and 0xFF).toByte()
        buf[offset + 1] = ((value shr 16) and 0xFF).toByte()
        buf[offset + 2] = ((value shr 8) and 0xFF).toByte()
        buf[offset + 3] = (value and 0xFF).toByte()
    }
}
