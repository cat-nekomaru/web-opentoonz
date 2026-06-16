# Day 26（d026）
.  

コーディングをしますか？（はい／[いいえ](../)）

💻 

. 

## 動作の様子

.  

<div align=center>
<video src="https://github.com/user-attachments/assets/8884fc5b-cef5-4cb1-b1a1-517bf07f0e95"></video></div>

.  

# (Proj) m002_ws_Base64toBeep

***※以下は全て作成者：Claude AIによる説明です***

.

Kotlin（API 28）+ Java-WebSocket の音声通信テストプロジェクト。  
ブラウザ（JavaScript）のボタン操作がBase64エンコードしたwavデータをWebSocketで送信し、Kotlin側がそれを受信・デコードして AudioTrack で再生する。

---

## 概要

```
:-------------------------------:
:          JavaScript           :
:                               :
:    Button 1: Send WAV         :
:          |                    :
:        Base64(wav) -------------> WebSocket (ws://192.168.x.x:9000)
:                               :         |
:    Button 2: Play Command --------> cmd: "play"
:                               :         |
:    Button 3: Silence Command -------> cmd: "silence"
:                               :         |
:-------------------------------:         |
                                          V
                              :-------------------------------:
                              :            Kotlin             :
                              :                               :
                              :     WsServer (port 9000)      :
                              :              |                :
                              :        Base64 Decode          :
                              :              |                :
                              :     pcmBuffer: ShortArray     :
                              :              |                :
                              :       AudioTrack.play()       :
                              :-------------------------------:
```

- **JavaScript側**：wavデータの生成・Base64エンコード・WebSocket送信を担当
- **Kotlin側**：WebSocketサーバー・Base64デコード・AudioTrack再生を担当
- **通信手段**：`Java-WebSocket`ライブラリ（`org.java-websocket:Java-WebSocket:1.5.4`）

---

## ファイル構成

```
m002_ws_Base64toBeep/
├── app/src/main/java/com/neko/m002_ws_base64tobeep/
│   └── MainActivity.kt            # WSサーバー・デコード・再生・ログ表示
├── app/src/main/res/
│   └── layout/activity_main.xml   # UI（ログ表示のみ）
├── app/src/main/AndroidManifest.xml
├── app/build.gradle.kts
└── r001-javascript/
    └── index.html                 # 3ボタンUI + Base64 wav埋め込み
```

---

## 通信プロトコル（JSON）

```javascript
// Button ① wav送信
{ "cmd": "load", "data": "UklGR..." }   // Base64 wav

// Button ② 再生
{ "cmd": "play" }

// Button ③ 無音
{ "cmd": "silence" }
```

---

## wavデータの仕様

| 項目 | 値 |
|------|----|
| フォーマット | PCM 16bit |
| チャンネル | mono（1ch） |
| サンプリングレート | 48,000 Hz |
| 周波数 | 1,000 Hz（サイン波） |
| 長さ | 0.1秒（4,800サンプル） |
| エンコード | Base64（12,860文字） |

---

## Kotlin側の処理フロー

```kotlin
// cmd: load → Base64デコード → pcmBuffer
val wav  = Base64.decode(b64, Base64.DEFAULT)
val pcm  = wav.copyOfRange(44, wav.size)   // WAVヘッダ44byte スキップ
pcmBuffer = ShortArray(pcm.size / 2) { buf.short }

// cmd: play → AudioTrack再生
at.write(pcmBuffer, 0, pcmBuffer.size)
at.play()

// cmd: silence → 先頭4サンプルだけピーク、残りゼロ
pcmBuffer = ShortArray(size) { i -> if (i < 4) Short.MAX_VALUE else 0 }
```

---

## Button ③（silence）の設計メモ

```
先頭4サンプル: 32767（Short.MAX_VALUE = ピーク音）
残り 4,796サンプル: 0（無音）

→ Button ② で再生すると「ピッ」と短く鳴る
→ Button ① → ② の1kHz「ピー」との違いが耳で確認できる
```

バッファの書き換えをKotlin側で処理することで通信コストを削減。  
JS側から `cmd: "silence"` の一言で済む設計。

---

## API 互換性メモ

| 使用 API / ライブラリ | 所属 | 最低要件 | API 28 |
|----------------------|------|---------|:-------:|
| `Java-WebSocket 1.5.4` | org.java-websocket | Java 7+ | ✅ |
| `WebSocketServer` | Java-WebSocket | Java 7+ | ✅ |
| `Base64.decode()` | android.util | API 8 | ✅ |
| `AudioTrack.Builder` | android.media | API 21 | ✅ |
| `AudioAttributes.Builder` | android.media | API 21 | ✅ |
| `AudioFormat.Builder` | android.media | API 21 | ✅ |
| `AudioTrack.MODE_STATIC` | android.media | API 3 | ✅ |
| `AudioFormat.ENCODING_PCM_16BIT` | android.media | API 3 | ✅ |
| `AudioFormat.CHANNEL_OUT_MONO` | android.media | API 3 | ✅ |
| `JSONObject` | org.json | API 1 | ✅ |
| `ByteBuffer.order(ByteOrder.LITTLE_ENDIAN)` | java.nio | Java標準 | ✅ |
| `Thread` | java.lang | Java標準 | ✅ |
| `runOnUiThread()` | android.app | API 1 | ✅ |

---

## IDEのハマりポイント

| エラー | 原因 | 解決策 |
|--------|------|--------|
| `Unexpected tokens` | `build.gradle.kts` にGroovy構文を書いた | `.kts` はKotlin DSL、`"` と `()` を使う |
| `extension already registered 'kotlin'` | プラグインの二重登録 | 自動生成の `plugins {}` ブロックは触らない |
| `checkDebugAarMetadata` FAILED | `compileSdk` が低すぎた | `compileSdk = 37` に変更 |
| WS接続できない（実機） | `ws://` がブロックされた | `AndroidManifest` に `usesCleartextTraffic="true"` |

---

## エミュレーター vs 実機

| 環境 | WSアドレス |
|------|-----------|
| エミュレーター | `ws://127.0.0.1:9000` |
| 実機 | `ws://192.168.179.61:9000` |

同一端末でもブラウザとKtアプリは別プロセスのため、実機では `127.0.0.1` は届かない。

.

🦆

---
.

<div align=center>

**(Proj) zipファイルを [用意しました](https://github.com/cat-nekomaru/web-opentoonz/tree/main/floor-2026/f001/d026/download)**

</div>

.  

<div align=center>
<img width=450 src=img/d026-s01.png><BR>
<img width=450 src=img/d026-s02.png><BR>
<img width=550 src=img/d026-s03.png><BR>
<img height=450 src=img/d026-s04.png>
</div>

.  

🏰