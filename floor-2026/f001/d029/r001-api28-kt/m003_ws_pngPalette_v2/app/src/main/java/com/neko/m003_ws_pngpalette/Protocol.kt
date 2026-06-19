package com.neko.m003_ws_pngpalette

import org.json.JSONObject

/**
 * Protocol.kt
 *
 * UI(JavaScript) <-> Engine(Kotlin) 間の WebSocket 通信プロトコル定義。
 *
 * 通信方式:
 *   1) コマンド・パラメータ  -> JSON テキストフレーム
 *   2) PNG画像本体          -> Binary フレーム (JSONフレームの直後に続けて送信)
 *
 * 全メッセージ共通で mdID (= 0x17) をJSON内に含める。
 * mdID はこの通信メッセージ定義(message definition)のバージョン識別子。
 */
object Protocol {

    const val MD_ID: Int = 0x17

    const val ROOM_ID: String = "r001"
    const val PROJECT_NAME: String = "m003_ws_pngPalette"

    // UI -> Engine コマンド種別
    const val CMD_RAW_REQUEST = "A_RAW_REQUEST"       // ボタンA: 無加工PNG要求
    const val CMD_PALETTE_APPLY = "B_PALETTE_APPLY"   // ボタンB: パレット加工PNG要求
    const val CMD_CLEAR_NOTIFY = "C_CLEAR_NOTIFY"     // ボタンC: UI側バッファをクリアした通知(ログ用)

    // Engine -> UI 応答種別
    const val RES_PNG_DATA = "PNG_DATA"               // 直後にBinaryフレームでPNG本体が続く
    const val RES_ERROR = "ERROR"
    const val RES_ACK = "ACK"

    /**
     * UIから受信したJSONコマンドを解析するためのデータクラス群。
     */
    sealed class IncomingCommand {
        data class RawRequest(val mdId: Int) : IncomingCommand()

        data class PaletteApply(
            val mdId: Int,
            val paletteIndex: Int, // uiID=D (0..15)
            val r: Int,            // uiID=E
            val g: Int,            // uiID=F
            val b: Int             // uiID=G
        ) : IncomingCommand()

        data class ClearNotify(val mdId: Int) : IncomingCommand()

        data class Unknown(val raw: String) : IncomingCommand()
    }

    /** 受信した生JSON文字列をパースする */
    fun parseIncoming(jsonText: String): IncomingCommand {
        val obj = JSONObject(jsonText)
        val mdId = obj.optInt("mdID", -1)
        return when (obj.optString("cmd")) {
            CMD_RAW_REQUEST -> IncomingCommand.RawRequest(mdId)
            CMD_PALETTE_APPLY -> {
                val palette = obj.getJSONObject("palette")
                IncomingCommand.PaletteApply(
                    mdId = mdId,
                    paletteIndex = palette.getInt("index"),
                    r = palette.getInt("r"),
                    g = palette.getInt("g"),
                    b = palette.getInt("b")
                )
            }
            CMD_CLEAR_NOTIFY -> IncomingCommand.ClearNotify(mdId)
            else -> IncomingCommand.Unknown(jsonText)
        }
    }

    /** ボタンA/B応答用: 「これからPNG Binaryフレームを送る」ことを知らせるJSON */
    fun buildPngDataHeader(fileName: String, byteLength: Int, processed: Boolean): String {
        val obj = JSONObject()
        obj.put("mdID", MD_ID)
        obj.put("room", ROOM_ID)
        obj.put("res", RES_PNG_DATA)
        obj.put("fileName", fileName)
        obj.put("byteLength", byteLength)
        obj.put("processed", processed) // false=ボタンA(無加工), true=ボタンB(加工済)
        return obj.toString()
    }

    fun buildAck(message: String): String {
        val obj = JSONObject()
        obj.put("mdID", MD_ID)
        obj.put("room", ROOM_ID)
        obj.put("res", RES_ACK)
        obj.put("message", message)
        return obj.toString()
    }

    fun buildError(message: String): String {
        val obj = JSONObject()
        obj.put("mdID", MD_ID)
        obj.put("room", ROOM_ID)
        obj.put("res", RES_ERROR)
        obj.put("message", message)
        return obj.toString()
    }
}
