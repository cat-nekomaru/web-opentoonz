# Day 29（d029）
.  

コーディングをしますか？（はい／[いいえ](https://github.com/cat-nekomaru/web-opentoonz/blob/main/floor-2026/f001/README.md)）

💻 

. 

## 動作の様子

.  

<div align=center>
<video src="https://github.com/user-attachments/assets/f81216f3-54e9-4b4c-92fb-04808ec72621"></video></div>

.  

# (Proj) m003_ws_pngPalette

***※以下は全て作成者：Claude AIによる説明です***

.

Kotlin（API 28）+ Java-WebSocket の画像通信テストプロジェクト。
ブラウザ（JavaScript）のボタン操作がコマンドをWebSocketで送信し、Kotlin側がPNG-8（パレットカラーPNG）のパレットを書き換えて返す。

---

## 概要

```
:-------------------------------:
:          JavaScript           :
:                               :
:   Button A: Fetch Raw         :
:   Button B: Apply Palette     :
:   Button C: Clear Buffer      :
:                               :
:-------------------------------:
              |
              | JSON(Tx): cmd, palette{index,r,g,b}
              | Binary(Rx): PNG bytes (Kotlin -> JS only)
              |
:-------------------------------:
:            Kotlin             :
:                               :
:     WsServer (port 9000)      :
:     PNG8_500x500.png (fixed)  :
:     rewrite RGB at index      :
:     (IDAT intact)             :
:                               :
:-------------------------------:
```

| UI action | -> | Tx - command | Rx - Engine response |
|---|---|---|---|
| Button A (Fetch Raw) | -> | `A_RAW_REQUEST` | `PNG_DATA` (raw) |
| Button B (Apply Palette) | -> | `B_PALETTE_APPLY` | `PNG_DATA` (processed) |
| Button C (Clear Buffer) | -> | `C_CLEAR_NOTIFY` | (no response) |

- **JavaScript側**：ボタン操作・パレット設定値(uiID D,E,F,G)の保持・WebSocket送受信・画像表示(uiID H)を担当
- **Kotlin側**：WebSocketサーバー・PNG-8のPLTEチャンク書き換え・ログ表示を担当
- **通信手段**：`Java-WebSocket`ライブラリ（`org.java-websocket:Java-WebSocket:1.5.4`）

---

## ファイル構成

```
m003_ws_pngPalette/
├── app/src/main/java/com/neko/m003_ws_pngpalette/
│   ├── MainActivity.kt            # サーバー起動・ログ表示
│   ├── EngineWebSocketServer.kt   # WebSocketサーバー本体
│   ├── Protocol.kt                # JSON通信プロトコル定義 (mdID=0x17)
│   ├── Png8PaletteEditor.kt       # PLTEチャンク書き換えロジック
│   └── PngAsset.kt                # PNG8_500x500.png をBase64で内包
├── app/src/main/res/
│   └── layout/activity_main.xml   # UI（ログ表示・Clearボタン）
├── app/src/main/AndroidManifest.xml
├── app/build.gradle.kts
└── r001-javascript/
    └── index.html                 # 3ボタン + パレット設定UI + 画像表示
```

---

## 通信プロトコル（JSON + Binary）

```javascript
// Button A: 無加工PNGを要求
{ "mdID": 23, "room": "r001", "cmd": "A_RAW_REQUEST" }

// Button B: パレット加工PNGを要求 (uiID D,E,F,G の現在値を送る)
{ "mdID": 23, "room": "r001", "cmd": "B_PALETTE_APPLY",
  "palette": { "index": 3, "r": 10, "g": 200, "b": 250 } }

// Button C: UI側バッファをクリアした通知
{ "mdID": 23, "room": "r001", "cmd": "C_CLEAR_NOTIFY" }
```

```javascript
// Engine -> UI: PNG送信前のヘッダ。直後にBinaryフレームでPNG本体が続く
{ "mdID": 23, "room": "r001", "res": "PNG_DATA",
  "fileName": "PNG8_500x500.png", "byteLength": 19969, "processed": false }
```

`mdID` は `0x17` (10進で23)。コマンド・パラメータはJSONテキストフレーム、PNG本体だけがBinaryフレームで続く2段構成。

---

## PNG-8のパレット書き換え

PNG8_500x500.png（500x500, パレット16色使用, 19969 bytes）のうち、**PLTEチャンクの指定index 1色だけ**をRGBで書き換える。IDAT（ピクセル配置）には一切手を入れないため、画像の形そのものは変わらず、選んだ箇所の色だけが変わる。

```kotlin
// Kotlin側（書き換え）
val processed = Png8PaletteEditor.applyPaletteChange(
    raw,
    Png8PaletteEditor.PaletteChange(index = 3, r = 10, g = 200, b = 250)
)
```

---

## API 互換性メモ

| 使用 API / ライブラリ | 所属 | 最低要件 | API 28 |
|----------------------|------|---------|:-------:|
| `Java-WebSocket 1.5.4` | org.java-websocket | Java 7+ | ✅ |
| `WebSocketServer` | Java-WebSocket | Java 7+ | ✅ |
| `Base64.decode()` | android.util | API 8 | ✅ |
| `CRC32` | java.util.zip | Java標準 | ✅ |
| `JSONObject` | org.json | API 1 | ✅ |
| `AppCompatActivity` | androidx.appcompat | API 14+ | ✅ |
| `ScrollView` / `TextView` | android.widget | API 1 | ✅ |

---

## ハマりポイント（ビルドのエラー）まとめ

| エラー | 原因 | 解決策 |
|--------|------|--------|
| `Unable to find method ...DependencyHandler.module(...)` | Gradle Wrapperの指定漏れ・キャッシュ不整合 | `gradle-wrapper.properties` でGradleバージョンを明示し、Sync後にキャッシュをクリア |
| `Invalid Gradle JDK configuration found` | プロジェクトが参照するJDKパスが無効 | Embedded JDK（Android Studio内蔵）に切り替え |
| `You need to use a Theme.AppCompat theme` | `AppCompatActivity`継承なのに`styles.xml`が`android:Theme.Material...`を指していた | `parent="Theme.AppCompat.Light.NoActionBar"` に修正 |
| `Unresolved reference 'R'` | ビルドキャッシュの不整合（パッケージ名は一致していても発生） | Build > Clean Project |
| `Please Select Gradle JVM to Import Project`（Gradle 8.4とJVM 21が不適合） | プロジェクトのGradleバージョンと、Android Studioが使うJVMのバージョンが噛み合わない | ダイアログの「Use JVM 17」を選択 |
| `Failed to find Platform SDK with path: platforms;android-37` | AGP 8.1.4は`compileSdk`の動作確認上限が34で、37は未サポート | `compileSdk`/`targetSdk`を`34`に戻す（37を使うにはAGP 9.1以降への大幅アップグレードが必要） |

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

**(Proj) zipファイルを [用意しました](https://github.com/cat-nekomaru/web-opentoonz/tree/main/floor-2026/f001/d029/download)**

</div>

.  

<div align=center>
<img width=420 src=img/d029-s01.png><BR>
<img width=600 src=img/d029-s02.png><BR>
<img width=600 src=img/d029-s03.png><BR>
<img width=380 src=img/d029-s04.png>
</div>

.  

🏰