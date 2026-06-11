package com.neko.api22_tester

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// Material 2 のコンポーネントを使用
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Material 2 のテーマシステムを適用
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    StableRealTimerScreen()
                }
            }
        }
    }
}

@Composable
fun StableRealTimerScreen() {
    RealDeviceScreen()
}

@Composable
fun RealDeviceScreen() {
    var nanoTime by remember { mutableStateOf(0L) }
    var tick10k by remember { mutableStateOf(0L) }
    var elapsedMs by remember { mutableStateOf(0L) }
    var pressCount by remember { mutableStateOf(0) }
    var currentMemoryMb by remember { mutableStateOf(0L) }

    // システム発振器を準備
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }

    // 10回ごとに変わる「現在の音の名前」を表示するための状態
    var currentToneName by remember { mutableStateOf("通常音 (0〜9回)") }

    fun getMemoryUsageFromPOSIX(): Long {
        return try {
            val statmFile = File("/proc/self/statm")
            if (!statmFile.exists()) return -2L

            val tokens = statmFile.readText().trim().split(Regex("\\s+"))
            if (tokens.size >= 2) {
                val rssPages = tokens[1].toLong()
                val pageSize = 4096L
                (rssPages * pageSize) / (1024L * 1024L)
            } else {
                -3L
            }
        } catch (e: Exception) {
            -1L
        }
    }

    val startTime = remember { System.nanoTime() }

    LaunchedEffect(Unit) {
        currentMemoryMb = getMemoryUsageFromPOSIX()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "📱 Real Device Timer Tester",
            style = MaterialTheme.typography.h5
        )

        Button(
            onClick = {
                val now = System.nanoTime()
                nanoTime = now
                tick10k = now / 100_000L
                elapsedMs = (now - startTime) / 1_000_000L

                // 10回ごとに音程（トーンの種類）を切り替えるロジック
                val toneType = when (pressCount / 10) {
                    0 -> {
                        currentToneName = "通常音・高 (0〜9回)"
                        ToneGenerator.TONE_PROP_ACK
                    }
                    1 -> {
                        currentToneName = "警告音・中 (10〜19回)"
                        ToneGenerator.TONE_SUP_RADIO_ACK
                    }
                    2 -> {
                        currentToneName = "エラー音・低 (20〜29回)"
                        ToneGenerator.TONE_SUP_ERROR
                    }
                    else -> {
                        currentToneName = "終了合図・長 (30回〜)"
                        ToneGenerator.TONE_PROP_PROMPT
                    }
                }

                val duration = if (pressCount >= 30) 100 else 50
                toneGenerator.startTone(toneType, duration)

                pressCount++
                currentMemoryMb = getMemoryUsageFromPOSIX()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔥 今すぐ取得！")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("System.nanoTime() : $nanoTime ns")
                Text("10kHz Tick       : $tick10k")
                Text("起動からの経過   : $elapsedMs ms")
                Text("ボタン押した回数 : $pressCount 回")
                Text("現在の音の種類   : $currentToneName")
                Text("アプリの物理メモリ(POSIX) : $currentMemoryMb MB")
            }
        }
    }
}
