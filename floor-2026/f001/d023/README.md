# Day 23（d023）
.  

コーディングをしますか？（はい／[いいえ](https://github.com/cat-nekomaru/web-opentoonz/blob/main/floor-2026/f001/README.md)）

💻 

. 

## 動作の様子

.  

<div align=center>
<video src="https://github.com/user-attachments/assets/c132a7dc-6d73-4cc6-81a5-cebb3b33c7ff"></video></div>

.  

## 今回の内容

- `Kotlin` 1000ms Timerを使用して時刻を取得
- `JavaScript` それをテキストで受信して表示。コード量がとても少なくて驚いた
- `wsライブラリ` org.java-websocket
- GUI側を極力簡素にしておけば、かなり使いやすいツールになる気がする
- それにしてもAIがなければ不可能に思える。必要な知識量が膨大すぎる

``` html
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>WS Time Receiver🕰️</title>
</head>
<body>
  <h2>受信時刻</h2>
  <p id="time">--:--:--</p>

  <script>
    const ws = new WebSocket("ws://192.168.179.61:9000");

    ws.onopen    = () => console.log("接続成功");
    ws.onmessage = (e) => document.getElementById("time").textContent = e.data;
    ws.onerror   = (e) => console.error("エラー", e);
    ws.onclose   = ()  => console.log("切断");
  </script>
</body>
</html>
```

``` js
Android (Kotlin)
  └─ Java-WebSocket サーバー (port 9000)
       └─ Timer で 1000ms ごとに時刻送信
            └─ ブラウザ (JavaScript) ws://192.168.179.61:9000
```

.  
.  

# (Proj) m001_ws_TimeSender

***※以下は全て作成者：Claude AIによる説明です***

.

Kotlin（API 28）+ Java-WebSocket の通信テストプロジェクト。  
Kotlin側の1000msタイマーが現在時刻（`hh:mm:ss`）をWebSocketでブロードキャストし、  
同一ネットワーク上のブラウザ（JavaScript）がそれを受信・表示する。

---

## 概要

```
:-----------------------------:
:           Kotlin            :
:                             :
:    Timer (1000ms cycle)     :
:           |                 :
:    Get time hh:mm:ss        :
:           |                 :---------------------------------:
:    wsServer.broadcast() ---------> WebSocket (port 9000)      :
:                             :           |                     :
:-----------------------------:           |                     :
                          :               v                     :
                          :    Browser: ws://192.168.x.x:9000   :
                          :               |                     :
                          :               v                     :
                          :        JS: ws.onmessage             :
                          :               |                     :
                          :               v                     :
                          :        Display on screen            :
                          :                                     :
                          :-------------------------------------:
```

- **Kotlin側**：WebSocketサーバーの起動・時刻の取得・ブロードキャストを担当
- **ブラウザ側**：受け取った文字列を表示するだけ
- **通信手段**：`Java-WebSocket`ライブラリ（`org.java-websocket:Java-WebSocket:1.5.4`）

---

## ファイル構成

```
m001_ws_TimeSender/
├── app/src/main/java/com/neko/m001_ws_timesender/
│   ├── MainActivity.kt            # タイマー・UI・サーバー管理
│   └── TimeWebSocketServer.kt     # WebSocketサーバー本体
├── app/src/main/res/
│   ├── layout/activity_main.xml   # UI（ステータス表示・時刻表示）
│   └── xml/network_security_config.xml  # ws://通信の許可設定
├── app/src/main/AndroidManifest.xml
└── app/build.gradle.kts
```

---

## Kotlin → ブラウザ 通信の仕組み

```kotlin
// Kotlin側（送信）
wsServer.broadcast("12:34:56")
```

```javascript
// ブラウザ側（受信）
const ws = new WebSocket("ws://192.168.179.61:9000");
ws.onmessage = (e) => document.getElementById("time").textContent = e.data;
```

ブラウザは完全に**受信専用**。自分でタイマーを持たず、Kotlinから送られた文字列を表示するだけ。

---

## タイマーの実装：Handler + Runnable

```kotlin
private val timerRunnable = object : Runnable {
    override fun run() {
        val now = sdf.format(Date())
        tvTime.text = now
        wsServer.broadcast(now)
        handler.postDelayed(this, 1000L)
    }
}
```

```
[UIスレッド]
     │
     ├─ handler.post(timerRunnable)  ← onCreate() で開始
     │
     ↓
 timerRunnable.run()
     │
     ├─ 時刻取得・UI更新・broadcast()
     │
     └─ handler.postDelayed(this, 1000ms)  ← 自己再投入でループ
          │
          ↓（1000ms後）
     timerRunnable.run()  ← 繰り返し
```

| 項目 | 内容 |
|------|------|
| 実行スレッド | UIスレッド |
| `runOnUiThread()` | 不要 |
| 周期 | 1000ms（時刻表示が目的のため精度は十分） |
| 停止方法 | `handler.removeCallbacks(timerRunnable)` |

---

## WebSocketサーバーの起動

```kotlin
wsServer = TimeWebSocketServer(9000)
wsServer.isReuseAddr = true   // 再起動時のポート占有を防ぐ
wsServer.start()
```

`isReuseAddr = true` はアプリ再起動時の `Address already in use` エラーを防ぐ**必須設定**。

---

## API 28 互換性メモ

| 使用API / ライブラリ | 最低要件 | API 28 |
|---------------------|---------|:--------:|
| `Java-WebSocket 1.5.4` | Java 7+ | ✅ |
| `Handler(Looper.getMainLooper())` | API 1 | ✅ |
| `SimpleDateFormat` | Java標準 | ✅ |
| `WebSocketServer.broadcast()` | Java-WebSocket | ✅ |
| `network_security_config`（ws://許可） | API 24+ | ✅ |

---

## ハマりポイント（ビルドのエラー）まとめ

| エラー | 原因 | 解決策 |
|--------|------|--------|
| `Unresolved reference 'R'` | パッケージ名の不一致 | `package com.neko.m001_ws_timesender` に統一 |
| `checkDebugAarMetadata` FAILED | `compileSdk` が低すぎた | `compileSdk = 37` に変更 |
| `platforms;android-37.1 not found` | `minorApiLevel = 1` が余分 | `compileSdk = 37` の1行に置換 |
| `Address already in use` | ポート占有 | `isReuseAddr = true` を追加 |

.  

🦆

## (Info) Android app development starting kit

- ① Visual Studio Code（VSCode）をインストール
  - Live Serverエクステンションを追加
  - ⭐️詳しい手順はAIに教えてもらう
  - あとで行う：JavaScriptをAndroidデバイスに配信する
- ② Android Studio（IDE）をインストール
- ③ IDEに新規プロジェクトを作成
  - New Project > Empty Viws Activity > Nextボタンを押す
    - Name：m001-ws-TimeSender
    - Save Location：任意
    - Minimum SDK：API 28
  - FinishボタンでIDE画面が開く（セットアップ完了まで数秒かかります）
  - ⭐️下にスクショを貼り付けています
- ④ 4つのコードの書き換えと、2つのコードを追加
  - Kotlinプロジェクトのルート `./r001-api28-kt/`
  - 全部用意できたらビルドできるか確認！
  - ⭐️詳しい手順はAIに教えてもらう（可能であればGitHubを活用して取得）
    1. build.gradle.kts (app)
    2. AndroidManifest.xml
    3. MainActivity.kt
    4. TimeWebSocketServer.kt（新規作成する）
    5. activity_main.xml
    6. res/xml/network_security_config.xml（これも新規作成）
  - `MainActivity` これがメインのコード😻
  - `TimeWebSocketServer` 増設したコード😼
  - `build.gradle.kts` 設定ファイル、有名なもの😺
  - `他のものALL` 誰にも理解できないAndroidのダークサイド🙀
- ⑤ Android端末とIDEを接続
  - USBケーブルでPCと接続する
  - Android端末を開発者モードにする
  - IDEでRunを実行、KotlinアプリがAndroidで起動するか確認！
  - ⭐️詳しい手順はAIに教えてもらう
- ⑥ Android端末とPCを同じネットワークにする
  - 端末をPCと同じWi-Fiに接続。もしくは端末のテザリングにPCを接続する
  - あとで使う：Android端末のIPアドレスを確認する
  - ⭐️詳しい手順はAIに教えてもらう
- ⑦ JavaScriptを送り込んで実行
  - JSプロジェクトのルート `./r001-javascript/`
  - PCからこの中にある`index.html`をDLする（もしくは新規作成 → コピペ）
  - VSCodeで開いてから、
    - コード内にあるIPアドレスを修正（Android端末の値に変更）
    - Live ServerをONにする
  - PCのアドレスをAndroidのブラウザからアクセス（例 192.168.179.50:5050）
  - index.htmlを見つけて開く！
  - ⭐️詳しい手順はAIに教えてもらう
- 以上、正常動作を確認できれは完了です。🐈..
- 参考URL：[Google AIとの雑談](https://share.google/aimode/YQX612Mh0FBWnQvlc)

.  

<div align=center>
<img width=450 src=img/d023-s01.png><BR><BR>
<img width=450 src=img/d023-s02.png><BR><BR>
<img width=450 src=img/d023-s03.png>
</div>

.  

🏰