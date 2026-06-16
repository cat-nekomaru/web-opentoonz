// mdID=0x15  m002_api22_beep
// Android API Lv22, Kotlin, WebView(UI) + AudioTrack(engine)

package com.neko.m002_api22_beep

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val SAMPLE_RATE = 48000
    private val NUM_SAMPLES = 1632

    // PCM buffer (16-bit samples). Initial value is undefined (by design).
    private val pcmBuffer = ShortArray(NUM_SAMPLES)

    // -------------------------------------------------------------------------
    // Engine bridge exposed to WebView via addJavascriptInterface
    // -------------------------------------------------------------------------
    inner class AudioBridge {

        // Button 1: receive Base64 WAV from WebView, decode, store PCM in buffer
        @JavascriptInterface
        fun loadWav(base64Wav: String): String {
            return try {
                val wavBytes = android.util.Base64.decode(base64Wav, android.util.Base64.DEFAULT)
                // Skip 44-byte WAV header, read PCM16 samples
                val dataOffset = 44
                val sampleCount = minOf(NUM_SAMPLES, (wavBytes.size - dataOffset) / 2)
                for (i in 0 until sampleCount) {
                    val lo = wavBytes[dataOffset + i * 2].toInt() and 0xFF
                    val hi = wavBytes[dataOffset + i * 2 + 1].toInt() and 0xFF
                    pcmBuffer[i] = ((hi shl 8) or lo).toShort()
                }
                "loadWav: ok ($sampleCount samples)"
            } catch (e: Exception) {
                "loadWav: error - ${e.message}"
            }
        }

        // Button 2: play current buffer contents via AudioTrack
        @JavascriptInterface
        fun play(): String {
            return try {
                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    NUM_SAMPLES * 2,
                    AudioTrack.MODE_STATIC
                )
                track.write(pcmBuffer, 0, NUM_SAMPLES)
                track.play()
                // Release after playback completes
                Thread {
                    Thread.sleep((NUM_SAMPLES * 1000L) / SAMPLE_RATE + 50)
                    track.release()
                }.start()
                "play: ok"
            } catch (e: Exception) {
                "play: error - ${e.message}"
            }
        }

        // Button 3: fill buffer with silent data (4 samples at max, rest zeros)
        //           generated on engine side as specified
        @JavascriptInterface
        fun loadSilent(): String {
            pcmBuffer[0] = 0x7FFF.toShort()
            pcmBuffer[1] = 0x7FFF.toShort()
            pcmBuffer[2] = 0x7FFF.toShort()
            pcmBuffer[3] = 0x7FFF.toShort()
            for (i in 4 until NUM_SAMPLES) pcmBuffer[i] = 0
            return "loadSilent: ok"
        }
    }

    // -------------------------------------------------------------------------
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(AudioBridge(), "AudioEngine")
        webView.loadDataWithBaseURL(null, buildHtml(), "text/html", "UTF-8", null)
    }

    // -------------------------------------------------------------------------
    // UI (HTML embedded in Kotlin)
    // -------------------------------------------------------------------------
    private fun buildHtml(): String = """<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<style>
  body { font-family: monospace; background:#111; color:#ccc; padding:20px; }
  h2   { color:#fff; margin-bottom:20px; }
  button {
    display:block; width:100%; padding:14px;
    margin-bottom:12px; font-size:16px;
    background:#333; color:#fff; border:1px solid #555;
    border-radius:6px; cursor:pointer;
  }
  button:active { background:#555; }
  #log {
    margin-top:20px; padding:10px;
    background:#000; border:1px solid #333;
    border-radius:4px; font-size:13px;
    min-height:80px; white-space:pre-wrap;
    color:#0f0;
  }
</style>
</head>
<body>
<h2>m002 / beep</h2>

<button onclick="onLoad()">Load Sine</button>
<button onclick="onPlay()">Play</button>
<button onclick="onSilent()">Load Silent</button>

<div id="log">ready.</div>

<script>
// 1kHz sine wave, PCM16, 48kHz, mono, 1632 samples — WAV (Base64)
var SINE_WAV_B64 =
  "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";

function log(msg) {
  document.getElementById("log").textContent = msg;
}

function onLoad() {
  var result = AudioEngine.loadWav(SINE_WAV_B64);
  log(result);
}

function onPlay() {
  var result = AudioEngine.play();
  log(result);
}

function onSilent() {
  var result = AudioEngine.loadSilent();
  log(result);
}
</script>
</body>
</html>"""
}
