package com.neko.m001_hello_webviewapi22

// ============================================================
//  実装A: Handler + Runnable
//  - UIスレッド上で動作するため runOnUiThread() 不要
//  - postDelayed() でループ → 処理時間分だけ遅延が蓄積する
//    （正確な周期が必要な場合は SystemClock 補正が必要）
// ============================================================

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// [HandlerImpl flavor]
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TIMER_INTERVAL_MS = 33L   // 約33ms周期（≒30fps）
        private const val IMPL_LABEL = "Handler + Runnable"
    }

    private lateinit var webView: WebView
    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    // --------------------------------------------------------
    // タイマーのRunnable（自己再投入でループする）
    // --------------------------------------------------------
    private val timerRunnable = object : Runnable {
        override fun run() {
            updateWebView()
            handler.postDelayed(this, TIMER_INTERVAL_MS)
        }
    }

    // --------------------------------------------------------
    // ライフサイクル
    // --------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ラベル表示
        findViewById<TextView>(R.id.tvImplLabel).text = "[impl: $IMPL_LABEL]"

        // WebView 初期化
        webView = findViewById(R.id.webView)
        setupWebView()
    }

    override fun onResume() {
        super.onResume()
        // フォアグラウンド復帰時にタイマー開始
        handler.post(timerRunnable)
    }

    override fun onPause() {
        super.onPause()
        // バックグラウンド移行時にタイマー停止（バッテリー配慮）
        handler.removeCallbacks(timerRunnable)
    }

    // --------------------------------------------------------
    // WebView セットアップ
    // --------------------------------------------------------
    private fun setupWebView() {
        val settings: WebSettings = webView.settings
        @Suppress("SetJavaScriptEnabled")
        settings.javaScriptEnabled = true

        // 初期HTML をロード（JS関数 showTime() を定義）
        webView.loadDataWithBaseURL(
            null,
            buildInitialHtml(),
            "text/html",
            "UTF-8",
            null
        )
    }

    // --------------------------------------------------------
    // Kotlin → WebView 時刻送信
    // evaluateJavascript() は API 19+ で使用可能 → API 22 では問題なし
    // --------------------------------------------------------
    private fun updateWebView() {
        val timeStr = timeFormat.format(Date())
        // JS の showTime() を呼び出す
        webView.evaluateJavascript("showTime('$timeStr');", null)
    }

    // --------------------------------------------------------
    // 初期HTML（最小限）
    // --------------------------------------------------------
    private fun buildInitialHtml(): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
        </head>
        <body>
            <p id="time">--:--:--.---</p>
            <script>
                function showTime(t) {
                    document.getElementById('time').textContent = t;
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
