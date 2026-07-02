package com.neko.m003_api22_pngPalette

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

/**
 * UI side (mdID=0x18).
 * WebView hosts the UI. JS sends JSON commands to the Engine via the
 * "AndroidBridge" JavascriptInterface; the Engine replies with PNG binary
 * (Base64 encoded) or log text via evaluateJavascript().
 *
 * Pattern: Mother 3 (WebView <-> Kotlin, bidirectional).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val engine = Engine()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(Bridge(), "AndroidBridge")
        webView.loadUrl("file:///android_asset/index.html")
        setContentView(webView)
    }

    /** JS -> Kotlin entry point. Receives a JSON command string. */
    inner class Bridge {
        @JavascriptInterface
        fun postMessage(json: String) {
            val result: Engine.Result = try {
                val cmd = JSONObject(json)
                engine.handleCommand(cmd)
            } catch (e: Exception) {
                Engine.Result(log = "engine error: ${e.message}")
            }
            // Kotlin -> JS. evaluateJavascript must run on the UI thread.
            runOnUiThread {
                result.png?.let { bytes ->
                    val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    webView.evaluateJavascript("onPngFromEngine('$b64')", null)
                }
                result.log?.let { msg ->
                    val safe = JSONObject.quote(msg)
                    webView.evaluateJavascript("onLogFromEngine($safe)", null)
                }
            }
        }
    }
}
