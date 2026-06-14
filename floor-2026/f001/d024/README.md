# Day 24（d024）
.  

コーディングをしますか？（はい／[いいえ](../)）

💻 

. 

## 動作の様子

.  

<div align=center>
<video src="https://github.com/user-attachments/assets/343254b8-61b4-44d6-acf7-302e7d0f1b84" controls width="300"></video></div>

.  

# (Proj) m001_hello_WebViewApi22

***※以下は全て作成者：Claude AIによる説明です***

.  

Kotlin（API 22）+ WebView の通信テストプロジェクト。  
Kotlin側の33msタイマーが現在時刻（`hh:mm:ss.sss`）をWebViewに送信し、WebViewはそれを表示する。

---

## 概要

```
┌─────────────────────────────┐
│         Kotlin 側           │
│                             │
│  タイマー（33ms周期）        │
│      ↓                      │
│  時刻取得 hh:mm:ss.sss      │
│      ↓                      │
│  evaluateJavascript()  ────────────→  WebView
└─────────────────────────────┘              │
                                             ↓
                                    JS: showTime(t)
                                             ↓
                                    画面に時刻を表示
```

- **Kotlin側**：時刻の取得・送信のみ担当
- **WebView側**：受け取った文字列を表示するだけ
- **通信手段**：`evaluateJavascript()`（API 19以上で使用可能）

---

## 2つの実装比較

このプロジェクトはタイマーの実装方法が異なる2バージョンを収録している。  
`./type-A-handler.txt`, `./type-B-timer.txt`

---

### 実装A：Handler + Runnable

```kotlin
private val timerRunnable = object : Runnable {
    override fun run() {
        updateWebView()
        handler.postDelayed(this, 33L)  // 自分自身を33ms後に再投入
    }
}
```

```
[UIスレッド]
     │
     ├─ handler.post(timerRunnable)  ← onResume() で開始
     │
     ↓
 timerRunnable.run()
     │
     ├─ updateWebView()              ← 時刻をWebViewに送信
     │
     └─ handler.postDelayed(this, 33ms)  ← 自己再投入でループ
          │
          ↓（33ms後）
     timerRunnable.run()  ← 繰り返し
```

**特徴**

| 項目 | 内容 |
|------|------|
| 実行スレッド | UIスレッド |
| `runOnUiThread()` | 不要 |
| 周期の精度 | `updateWebView()` の処理時間分だけ遅延が蓄積する |
| 停止方法 | `handler.removeCallbacks(timerRunnable)` |
| 向いている用途 | 軽量なUI更新・短時間の動作 |

---

### 実装B：Timer + TimerTask

```kotlin
timer = Timer().also { t ->
    t.scheduleAtFixedRate(object : TimerTask() {
        override fun run() {
            runOnUiThread {             // UIスレッドに切り替えてから送信
                updateWebView()
            }
        }
    }, 0L, 33L)
}
```

```
[バックグラウンドスレッド]
     │
     ├─ Timer.scheduleAtFixedRate(task, 0, 33ms)  ← onResume() で開始
     │
     ↓（33ms経過ごとに）
 TimerTask.run()
     │
     └─ runOnUiThread { updateWebView() }
               │
               ↓ UIスレッドに切り替え
          evaluateJavascript()  ← WebViewへ送信
```

**特徴**

| 項目 | 内容 |
|------|------|
| 実行スレッド | バックグラウンドスレッド |
| `runOnUiThread()` | 必須（WebView操作はUIスレッドのみ可） |
| 周期の精度 | `scheduleAtFixedRate` が前回予定時刻を基準に補正するため累積誤差が少ない |
| 停止方法 | `timer.cancel()` |
| 向いている用途 | 長時間・高精度な周期処理 |

---

### 2つの決定的な違い

```
【Handler版】
  時刻A送信 → 33ms待機 → 時刻B送信 → 33ms待機 → ...
             ↑処理時間が含まれないので長時間でズレが蓄積

【Timer版】
  時刻A送信     時刻B送信     時刻C送信
  |←── 33ms ──→|←── 33ms ──→|
  予定時刻を基準に次回を計算するのでズレが自動補正される
```

---

## Kotlin → WebView 通信の仕組み

```kotlin
// Kotlin側（送信）
webView.evaluateJavascript("showTime('12:34:56.789');", null)
```

```javascript
// WebView側 HTML内のJavaScript（受信）
function showTime(t) {
    document.getElementById('time').textContent = t;
}
```

WebViewは完全に**受信専用**。自分でタイマーを持たず、Kotlinから送られた文字列を表示するだけ。

---

## API 22 互換性メモ

| 使用API | 最低要件 | API 22 |
|---------|---------|--------|
| `evaluateJavascript()` | API 19 | ✅ |
| `WebSettings.javaScriptEnabled` | API 1 | ✅ |
| `Handler(Looper.getMainLooper())` | API 1 | ✅ |
| `Timer.scheduleAtFixedRate()` | Java標準 | ✅ |

.  
. 

🦆

# (Info) Android app development starting kit

- ① Android Studio（IDE）をインストール
- ② `API Lv22`用のSDKを追加
  - IDEを起動してから、
  - メニューから`Settings..`を開きます。<kbd>⌘ + ,</kbd>
  - `検索` sdk > Android SDK > ☑️ Android 5.1（API Level 22）> OKを押す
  - DLが終了したらFinishボタン、これで完了
  - ⭐️下にスクショを貼り付けています
- ③ IDEに新規プロジェクトを作成
  - New Project > Empty Viws Activity > Nextボタンを押す
    - Name：m001_hello_WebViewApi22
    - Save Location：任意
    - Minimum SDK：API 23（これ以下にはできないのでコード側で指定する）
  - FinishボタンでIDE画面が開く
  - ⭐️下にスクショを貼り付けています
- ④ 3つのコードの書き換えと、1つのコードを追加
  - Kotlinプロジェクトのルート `./r002-api22-kt/`
    1. build.gradle.kts (app)
    2. MainActivity.kt
    3. activity_main.xml
    4. res/values/themes/styles.xml（新規作成するもの）
  - `build.gradle.kts` 定番の設定ファイル😼
  - `MainActivity.kt` これがメインのコード😸
  - `その他のもの` よく分かりませんが必要です😾
  - 準備できたらビルドできるか確認してください！
  - ⭐️詳しい手順はAIに聞けばOK（未確認：Package nameの修正が必要かも？）
- ⑤ Android端末とIDEを接続
  - USBケーブルでPCと接続する
  - Android端末を開発者モードにする
  - IDEでRunを実行、KotlinアプリがAndroidで起動するか確認！
  - ⭐️手順はAIに教えてもらいましょう
- 以上、正常動作を確認できれは完了です。🐈..

.  

<div align=center>
<img width=450 src=img/d024-s02.png><BR>
<img width=450 src=img/d024-s03.png><BR>
<img width=450 src=img/d024-s04.png><BR>
<img width=600 src=img/d024-s05.png><BR><BR>
<img width=220 src=img/d024-s06.png><BR>
</div>

.  

🏰