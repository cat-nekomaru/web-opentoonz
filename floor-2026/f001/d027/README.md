# Day 27（d027）
.  

コーディングをしますか？（はい／[いいえ](../)）

💻 

. 

## 動作の様子

.  

<div align=center>
<video src="https://github.com/user-attachments/assets/e3813c02-2750-478b-b7bc-ebd57fc28a95"></video></div>

.  

# (Proj) m002_api22_beep

***※以下は全て作成者：Claude AIによる説明です***

Kotlin（API 22）+ addJavascriptInterface の音声通信テストプロジェクト。  
WebView（JavaScript）のボタン操作がBase64エンコードしたwavデータをエンジン側に送信し、Kotlin側がそれをデコードしてAudioTrackで再生する。

```
:--------------------------------------:
:             WebView - UI             :
:                                      :
:  [Load Sine]    [Play]    [Silent]   :
:       |           |           |      :
:  Base64(wav)  cmd:play   cmd:silent  :
:       |           |           |      :
:--------------------------------------:
                   |
         addJavascriptInterface()
                   |
   :-------------------------------:
   :        Kotlin - Engine        :
   :                               :
   :   AudioBridge                 :
   :     loadWav(b64)              :
   :       Base64 decode           :
   :       pcmBuffer: ShortArray   :
   :                               :
   :     play()                    :
   :       AudioTrack.play()       :
   :                               :
   :     loadSilent()              :
   :       [0x7FFF x4][0x0000...]  :
   :-------------------------------:
```

---

## 概要

- **UI側（運転席）**：wavデータの生成・Base64エンコード・コマンド送信を担当
- **エンジン側**：Base64デコード・バッファ管理・AudioTrack再生を担当
- **通信手段**：`addJavascriptInterface`（同一プロセス内、ネットワーク不要）

---

## ファイル構成

```
m002_api22_beep/
├── app/src/main/java/com/neko/m002_api22_beep/
│   └── MainActivity.kt            # WebView埋め込み・AudioBridge・AudioTrack
├── app/src/main/res/
│   ├── layout/activity_main.xml   # 空レイアウト（WebViewで上書き）
│   └── values/themes.xml          # Theme.AppCompat.Light.NoActionBar
├── app/src/main/AndroidManifest.xml
└── app/build.gradle.kts
```

---

## wavデータの仕様

| 項目 | 値 |
|------|----|
| フォーマット | PCM 16bit |
| チャンネル | mono（1ch） |
| サンプリングレート | 48,000 Hz |
| 周波数 | 1,000 Hz（サイン波） |
| サンプル数 | 1,632（約1frame @30fps） |
| エンコード | Base64、WebView側コードに埋め込み |

---

## エンジン側の処理フロー

```kotlin
// loadWav(b64) → Base64デコード → pcmBuffer
val wav = Base64.decode(b64, Base64.DEFAULT)
// WAVヘッダ44byteスキップ、PCM16bitをShortArrayに格納

// play() → AudioTrack再生
track.write(pcmBuffer, 0, NUM_SAMPLES)
track.play()

// loadSilent() → 先頭4サンプルだけピーク、残りゼロ
pcmBuffer[0..3] = 0x7FFF
pcmBuffer[4..] = 0x0000
```

---

## Button [Silent] の設計メモ

```
先頭4サンプル: 32767（0x7FFF = ピーク音）
残り 1,628サンプル: 0（無音）

→ [Play] で再生すると「プツッ」と短く鳴る
→ [Load Sine] → [Play] の1kHz「ピー」との違いが耳で確認できる
```

バッファの生成をエンジン側で処理することで通信コストを削減。
UI側から一言で済む設計。

---

## API互換性メモ

| 使用 API / ライブラリ | 所属 | 最低要件 | API 22 |
|----------------------|------|---------|:-------:|
| `addJavascriptInterface()` | android.webkit | API 1 | ✅ |
| `WebView.loadDataWithBaseURL()` | android.webkit | API 1 | ✅ |
| `WebView.settings.javaScriptEnabled` | android.webkit | API 1 | ✅ |
| `@JavascriptInterface` | android.webkit | API 17 | ✅ |
| `Base64.decode()` | android.util | API 8 | ✅ |
| `AudioTrack` (constructor) | android.media | API 3 | ✅ |
| `AudioTrack.MODE_STATIC` | android.media | API 3 | ✅ |
| `AudioFormat.ENCODING_PCM_16BIT` | android.media | API 3 | ✅ |
| `AudioFormat.CHANNEL_OUT_MONO` | android.media | API 3 | ✅ |
| `AudioManager.STREAM_MUSIC` | android.media | API 1 | ✅ |
| `AudioTrack.write()` | android.media | API 3 | ✅ |
| `AudioTrack.play()` | android.media | API 3 | ✅ |
| `AudioTrack.release()` | android.media | API 3 | ✅ |
| `Thread` | java.lang | Java標準 | ✅ |

---

## ハマりポイント（ビルドのエラー）まとめ

| エラー | 原因 | 解決策 |
|--------|------|--------|
| `AAR metadata` FAILED | `compileSdk = 34` が低すぎた | `compileSdk = 37` に変更 |
| `minSdkVersion 22 cannot be smaller than 23` (material) | material 1.14.0 が minSdk23以上を要求 | `implementation(libs.material)` を削除 |
| `minSdkVersion 22 cannot be smaller than 23` (activity) | activity 1.13.0 が minSdk23以上を要求 | `implementation(libs.androidx.activity.ktx)` を削除 |
| `Theme.Material3 not found` | materialライブラリ削除の余波 | `themes.xml` を `Theme.AppCompat.Light.NoActionBar` に変更 |
| `layout_constraintXxx not found` | ConstraintLayout削除の余波 | `activity_main.xml` を空の `LinearLayout` に差替 |
| `AudioTrack constructor is deprecated` | API21以降はBuilderが推奨 | 警告のみ、動作に影響なし |

.  

🦆

---
.

<div align=center>

**(Proj) zipファイルを [用意しました](https://github.com/cat-nekomaru/web-opentoonz/tree/main/floor-2026/f001/d027/download)**

</div>

.  

<div align=center>
<img width=430 src=img/d027-s01.png><BR>
<img width=430 src=img/d027-s02.png><BR>
<img width=620 src=img/d027-s03.png><BR>
<img height=380 src=img/d027-s04.png>
</div>

.  

🏰