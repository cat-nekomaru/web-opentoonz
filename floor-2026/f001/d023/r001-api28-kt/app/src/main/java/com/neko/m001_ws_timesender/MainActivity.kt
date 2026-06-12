package com.neko.m001_ws_timesender

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

// floor 1 / room 1 / mother 1 / Day 23
// 2026.6.13
// (claude AI, private) https://claude.ai/share/bad71340-e36d-406d-99c1-96c012b38a4c
//
class MainActivity : AppCompatActivity() {

    private lateinit var wsServer: TimeWebSocketServer
    private lateinit var tvStatus: TextView
    private lateinit var tvTime: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val timerRunnable = object : Runnable {
        override fun run() {
            val now = sdf.format(Date())
            tvTime.text = now

            // 接続中の全クライアントへ送信
            try {
                wsServer.broadcast(now)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvTime   = findViewById(R.id.tvTime)

        // サーバー起動
        wsServer = TimeWebSocketServer(9000)
        wsServer.isReuseAddr = true   // ← 再起動時のポート占有を防ぐ
        wsServer.start()

        tvStatus.text = "サーバー起動中 ws://192.168.179.61:9000"

        // タイマー開始
        handler.post(timerRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
        try {
            wsServer.stop(1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}