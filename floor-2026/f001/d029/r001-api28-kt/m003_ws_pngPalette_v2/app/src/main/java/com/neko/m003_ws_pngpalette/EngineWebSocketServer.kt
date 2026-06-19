package com.neko.m003_ws_pngpalette

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.nio.ByteBuffer

/**
 * EngineWebSocketServer.kt
 *
 * UI(JavaScript, browser) からの接続を受け付ける WebSocket サーバー。
 * Room "r001" の単一ルームのみ扱う簡易実装。
 *
 * 受信: JSONテキストフレーム (Protocol.IncomingCommand)
 * 送信: JSONテキストフレーム + (PNG応答時のみ) 直後にBinaryフレーム
 *
 * ログはコールバック(onLog)経由でMainActivity側のテキストボックスに渡す。
 */
class EngineWebSocketServer(
    port: Int,
    private val onLog: (String) -> Unit
) : WebSocketServer(InetSocketAddress(port)) {

    // ボタンB(パレット加工)用に保持する、現在のUI側設定値 (uiID=D,E,F,G の最新値)
    @Volatile
    private var currentPaletteIndex: Int = 0
    @Volatile
    private var currentR: Int = 255
    @Volatile
    private var currentG: Int = 0
    @Volatile
    private var currentB: Int = 0

    override fun onStart() {
        onLog("[SERVER] started on port ${address.port}, room=${Protocol.ROOM_ID}")
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        onLog("[OPEN] client connected: ${conn.remoteSocketAddress}")
        conn.send(Protocol.buildAck("connected to room ${Protocol.ROOM_ID} (mdID=0x${Protocol.MD_ID.toString(16)})"))
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        onLog("[CLOSE] client disconnected: code=$code reason=$reason")
    }

    override fun onMessage(conn: WebSocket, message: String) {
        onLog("[RECV][JSON] $message")

        val command = try {
            Protocol.parseIncoming(message)
        } catch (e: Exception) {
            onLog("[ERROR] failed to parse JSON: ${e.message}")
            conn.send(Protocol.buildError("invalid JSON: ${e.message}"))
            return
        }

        when (command) {
            is Protocol.IncomingCommand.RawRequest -> handleRawRequest(conn, command)
            is Protocol.IncomingCommand.PaletteApply -> handlePaletteApply(conn, command)
            is Protocol.IncomingCommand.ClearNotify -> handleClearNotify(command)
            is Protocol.IncomingCommand.Unknown -> {
                onLog("[WARN] unknown command: ${command.raw}")
                conn.send(Protocol.buildError("unknown command"))
            }
        }
    }

    // バイナリフレームを受け取るケースは現仕様(UI->Engine方向)では使わないが、
    // 拡張時のために空実装で残しておく。
    override fun onMessage(conn: WebSocket, message: ByteBuffer) {
        onLog("[RECV][BIN] unexpected binary frame from UI, ${message.remaining()} bytes (ignored)")
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        onLog("[ERROR] ${ex.message}")
    }

    /** ボタンA: 無加工PNGデータをそのままUIへ送る */
    private fun handleRawRequest(conn: WebSocket, cmd: Protocol.IncomingCommand.RawRequest) {
        if (cmd.mdId != Protocol.MD_ID) {
            onLog("[WARN] mdID mismatch: received=${cmd.mdId}, expected=${Protocol.MD_ID}")
        }
        val bytes = PngAsset.rawBytes()
        onLog("[A_RAW_REQUEST] sending raw PNG: ${PngAsset.FILE_NAME} (${bytes.size} bytes)")

        val header = Protocol.buildPngDataHeader(
            fileName = PngAsset.FILE_NAME,
            byteLength = bytes.size,
            processed = false
        )
        conn.send(header)
        conn.send(bytes)
        onLog("[SEND] JSON header + binary(${bytes.size} bytes) sent")
    }

    /**
     * ボタンB: あらかじめ設定済みのパレット値 (uiID=D,E,F,G) で
     * PLTEチャンクを書き換えたPNGデータをUIへ送る。
     */
    private fun handlePaletteApply(conn: WebSocket, cmd: Protocol.IncomingCommand.PaletteApply) {
        if (cmd.mdId != Protocol.MD_ID) {
            onLog("[WARN] mdID mismatch: received=${cmd.mdId}, expected=${Protocol.MD_ID}")
        }

        currentPaletteIndex = cmd.paletteIndex
        currentR = cmd.r
        currentG = cmd.g
        currentB = cmd.b

        onLog(
            "[B_PALETTE_APPLY] index=$currentPaletteIndex " +
                "RGB=($currentR,$currentG,$currentB)"
        )

        try {
            val raw = PngAsset.rawBytes()
            val before = Png8PaletteEditor.readPaletteColor(raw, currentPaletteIndex)
            val processed = Png8PaletteEditor.applyPaletteChange(
                raw,
                Png8PaletteEditor.PaletteChange(
                    index = currentPaletteIndex,
                    r = currentR,
                    g = currentG,
                    b = currentB
                )
            )
            onLog(
                "[PALETTE] index=$currentPaletteIndex changed " +
                    "${before} -> ($currentR,$currentG,$currentB)"
            )

            val header = Protocol.buildPngDataHeader(
                fileName = PngAsset.FILE_NAME,
                byteLength = processed.size,
                processed = true
            )
            conn.send(header)
            conn.send(processed)
            onLog("[SEND] JSON header + binary(${processed.size} bytes) sent (processed)")
        } catch (e: Png8PaletteEditor.PngFormatException) {
            onLog("[ERROR] palette apply failed: ${e.message}")
            conn.send(Protocol.buildError("palette apply failed: ${e.message}"))
        }
    }

    /** ボタンC: UI側でバッファをクリアしたことの通知(エンジン側は状態を持たないのでログのみ) */
    private fun handleClearNotify(cmd: Protocol.IncomingCommand.ClearNotify) {
        onLog("[C_CLEAR_NOTIFY] UI buffer (uiID=H) cleared by client")
    }
}
