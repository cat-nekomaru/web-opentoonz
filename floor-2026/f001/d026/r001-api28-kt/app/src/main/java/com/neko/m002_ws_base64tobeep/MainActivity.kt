package com.neko.m002_ws_base64tobeep

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.json.JSONObject
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG  = "d026"
private const val PORT = 9000

class MainActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView
    private var wsServer: WsServer? = null
    private var pcmBuffer: ShortArray = ShortArray(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvLog = findViewById(R.id.tvLog)

        wsServer = WsServer(PORT,
            onLoad    = { b64  -> handleLoad(b64) },
            onPlay    = {        handlePlay()      },
            onSilence = {        handleSilence()   },
            onLog     = { msg  -> appendLog(msg)   }
        )
        wsServer?.start()
        appendLog("WS server :$PORT  start")
    }

    override fun onDestroy() {
        super.onDestroy()
        wsServer?.stop()
    }

    // ── cmd: load ── Base64 wav → pcmBuffer ──
    private fun handleLoad(b64: String) {
        try {
            val wav  = Base64.decode(b64, Base64.DEFAULT)
            val pcm  = wav.copyOfRange(44, wav.size)          // WAVヘッダ44byte スキップ
            val buf  = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN)
            pcmBuffer = ShortArray(pcm.size / 2) { buf.short }
            appendLog("load: ${pcmBuffer.size} samples")
        } catch (e: Exception) {
            appendLog("load ERR: ${e.message}")
        }
    }

    // ── cmd: play ── AudioTrack 再生 ──
    private fun handlePlay() {
        if (pcmBuffer.isEmpty()) { appendLog("play: buffer empty"); return }
        Thread {
            try {
                val at = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(48000)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmBuffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                at.write(pcmBuffer, 0, pcmBuffer.size)
                at.play()
                appendLog("play: ${pcmBuffer.size} samples")
                Thread.sleep((pcmBuffer.size / 48000.0 * 1000 + 50).toLong())
                at.stop(); at.release()
            } catch (e: Exception) {
                appendLog("play ERR: ${e.message}")
            }
        }.start()
    }

    // ── cmd: silence ── 先頭4サンプルだけ MAX、残りゼロ ──
    private fun handleSilence() {
        val size = if (pcmBuffer.isNotEmpty()) pcmBuffer.size else 4800
        pcmBuffer = ShortArray(size) { i -> if (i < 4) Short.MAX_VALUE else 0 }
        appendLog("silence: size=${pcmBuffer.size}, peak[0..3]=${pcmBuffer.take(4)}")
    }

    private fun appendLog(msg: String) {
        Log.d(TAG, msg)
        runOnUiThread { tvLog.text = "[$TAG] $msg\n" + tvLog.text }
    }
}

// ════════════════════════════════════════════
// WebSocket サーバー
// ════════════════════════════════════════════
class WsServer(
    port: Int,
    private val onLoad:    (String) -> Unit,
    private val onPlay:    ()       -> Unit,
    private val onSilence: ()       -> Unit,
    private val onLog:     (String) -> Unit
) : WebSocketServer(InetSocketAddress(port)) {

    override fun onOpen(conn: WebSocket, hs: ClientHandshake) {
        onLog("open: ${conn.remoteSocketAddress}")
    }
    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        onLog("close: $code")
    }
    override fun onMessage(conn: WebSocket, msg: String) {
        try {
            val j = JSONObject(msg)
            when (val cmd = j.getString("cmd")) {
                "load"    -> onLoad(j.getString("data"))
                "play"    -> onPlay()
                "silence" -> onSilence()
                else      -> onLog("unknown cmd: $cmd")
            }
        } catch (e: Exception) {
            onLog("msg ERR: ${e.message}")
        }
    }
    override fun onError(conn: WebSocket?, ex: Exception) { onLog("WS ERR: ${ex.message}") }
    override fun onStart() { onLog("WsServer onStart") }
}
