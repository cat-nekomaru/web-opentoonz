# m003_ws_pngPalette

mdID = **0x17**（このメッセージ定義のバージョンID。全JSON通信に含まれる）
Room = **r001**（Tools: Android API Lv28, Kotlin / JavaScript-browser, port 9000）

f001 の `Day 29` (`d029`, mother=`m003`, room=`r001`, mdID=`0x17`) に対応する実装。
characteristics: `ws: Tx PNG bin, change color, Rx`

UI(ブラウザ JavaScript) と Engine(Android/Kotlin, API 28+) を WebSocket で接続し、
PNG-8（パレットカラーPNG）のパレットをリアルタイムに書き換えて表示するツール。

## 構成

```
m003_ws_pngPalette/
├── ui/
│   └── index.html          UI側 (HTML+CSS+JS 単一ファイル)
└── engine/                 エンジン側 (Android Studioプロジェクト)
    └── app/src/main/
        ├── java/com/m003/wspngpalette/
        │   ├── MainActivity.kt           画面・ログ表示・サーバー起動
        │   ├── EngineWebSocketServer.kt  WebSocketサーバー本体
        │   ├── Protocol.kt               JSON通信プロトコル定義 (mdID=0x17)
        │   ├── Png8PaletteEditor.kt      PLTEチャンク書き換えロジック
        │   └── PngAsset.kt               PNG8_500x500.png をBase64で内包
        ├── assets/PNG8_500x500.png       参照用の実ファイル(19969 bytes)
        └── res/layout/activity_main.xml  ログ表示テキストボックス + Clearボタン
```

## UI部品 (uiID)

| uiID | 種別 | 役割 |
|---|---|---|
| A | button | `A_RAW_REQUEST` 送信。無加工PNGをHへ |
| B | button | `B_PALETTE_APPLY` 送信。D,E,F,Gの設定で加工したPNGをHへ |
| C | button | バッファ(H)のクリア + エンジンへ通知 |
| D | number | パレット番号 (0〜15) |
| E | slider | R (0〜255) |
| F | slider | G (0〜255) |
| G | slider | B (0〜255) |
| H | image area | PNGバッファ表示エリア (初期値=不定/空) |

## 通信プロトコル

- コマンド・パラメータ：**JSONテキストフレーム**
- PNG本体：**Binaryフレーム**（対応するJSONヘッダの直後に送信）

### UI → Engine

```json
// ボタンA
{ "mdID": 23, "room": "r001", "cmd": "A_RAW_REQUEST" }

// ボタンB
{ "mdID": 23, "room": "r001", "cmd": "B_PALETTE_APPLY",
  "palette": { "index": 3, "r": 10, "g": 200, "b": 250 } }

// ボタンC
{ "mdID": 23, "room": "r001", "cmd": "C_CLEAR_NOTIFY" }
```
(mdID=23 は 0x17 の10進表記)

### Engine → UI

```json
{ "mdID": 23, "room": "r001", "res": "PNG_DATA",
  "fileName": "PNG8_500x500.png", "byteLength": 19969, "processed": false }
```
直後に Binary フレームで `byteLength` バイトのPNGデータが続く。
`processed` が `true` の場合はボタンBによるパレット加工後データ。

## PNG8_500x500.png

- 500×500, PNG-8 (パレットカラー)
- パレット16色 (index 0〜15) を使用
- ファイルサイズ 19969 bytes（カスタムチャンク `mdId` に `0x17` を埋め込み、
  サイズ調整と識別を兼ねている）
- `Png8PaletteEditor.kt` が PLTEチャンクの対象indexのRGBだけを書き換え、
  ピクセル配置(IDAT)は無加工のまま保持する

## 動作確認

`engine/` を Android Studio で開いてビルド・実機(またはエミュレータ)起動すると、
ポート `9000` でWebSocketサーバーが起動する（f001配下の他room/dayと共通のポート番号）。
`ui/index.html` をブラウザで開き、接続バーに以下のいずれかを入力して Connect。

- 実機: `ws://192.168.179.61:9000`
- エミュレータ: `ws://127.0.0.1:9000`

Kotlin実行環境が無い検証環境では、同一プロトコルを実装したNode.jsモックサーバーで
A/B/Cの一連の往復・PNGバイト長一致・パレット書き換え結果を確認済み（その際はポート8017で検証）。
