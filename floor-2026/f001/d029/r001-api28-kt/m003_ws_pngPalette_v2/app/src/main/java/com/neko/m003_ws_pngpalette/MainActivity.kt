package com.neko.m003_ws_pngpalette

import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MainActivity.kt
 *
 * エンジン側(Kotlin / Android API 28+)の唯一の画面。
 * - WebSocketサーバーをポート起動し、UI(JavaScript)からの接続を待ち受ける
 * - 受信コマンド・送信内容・エラーをログとしてテキストボックスに表示する
 * - ログクリアボタンでテキストボックスの内容を消去する
 *
 * mdID=0x17 / Room=r001 / Project=m003_ws_pngPalette
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val WS_PORT = 9000 // f001 共通ポート (README.md記載のとおり)
        private const val LOG_MAX_LINES = 500
    }

    private lateinit var textLog: TextView
    private lateinit var textStatus: TextView
    private lateinit var scrollLog: ScrollView
    private lateinit var buttonClearLog: Button

    private var server: EngineWebSocketServer? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textLog = findViewById(R.id.textLog)
        textStatus = findViewById(R.id.textStatus)
        scrollLog = findViewById(R.id.scrollLog)
        buttonClearLog = findViewById(R.id.buttonClearLog)

        buttonClearLog.setOnClickListener {
            textLog.text = ""
            appendLog("[UI] log cleared")
        }

        startServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
    }

    private fun startServer() {
        appendLog("[BOOT] PNG asset = ${PngAsset.FILE_NAME}")
        // 起動時に埋め込みPNGのサイズを検証してログに出す
        val bytes = PngAsset.rawBytes()
        val sizeOk = bytes.size == PngAsset.EXPECTED_SIZE_BYTES
        appendLog(
            "[BOOT] asset size = ${bytes.size} bytes " +
                "(expected ${PngAsset.EXPECTED_SIZE_BYTES}) -> ${if (sizeOk) "OK" else "MISMATCH"}"
        )

        server = EngineWebSocketServer(WS_PORT) { line ->
            runOnUiThread { appendLog(line) }
        }
        server?.start()
        textStatus.text = "server: listening on ws://<device-ip>:$WS_PORT  room=${Protocol.ROOM_ID}"
        appendLog("[BOOT] WebSocket server starting on port $WS_PORT ...")
    }

    private fun appendLog(line: String) {
        val time = timeFormat.format(Date())
        textLog.append("$time  $line\n")

        // 行数が増えすぎないよう古い行を間引く
        val lines = textLog.text.lines()
        if (lines.size > LOG_MAX_LINES) {
            textLog.text = lines.takeLast(LOG_MAX_LINES).joinToString("\n")
        }

        scrollLog.post { scrollLog.fullScroll(android.view.View.FOCUS_DOWN) }
    }
}
