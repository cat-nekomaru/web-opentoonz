# Day 25（d025）
.  

コーディングをしますか？（はい／[いいえ](../)）

💻 

. 

## 動作の様子

.  

<div align=center>
<video src="https://github.com/user-attachments/assets/bb926c0b-fbda-4082-9cc8-315c5dad1663" controls width="350"></video></div>

.  

## （講師）Claudeさん

**😿 AIコーディング**  
- （いろいろトラブルがあった後）➠ ソフトウェア開発のテストコードをお願いします。➠ 言語：Swift(GUIあり)、プロジェクト名：m001_hello_SwiftUI 、macOS 26.x、➠ 機能は、33ms毎のTimer から 時刻 hh:mm:ss.sss（1ms単位）を表示します。➠ SwiftUI側はテキストを受け取って表示するだけのシンプルなものです。➠ 時刻の取得はApfsTimeをそのまま活用してください。33msのループについては、精度は雑でいいので、シンプルな構造でお願いします。➠ テストは不要です

.  

**🤖 m001_hello_SwiftUI テストコード**
- 仕切り直しですね！今度は**テストなし**、シンプルな実装コードだけ作ります

**🤖 方針確認**
- `clock_gettime(CLOCK_REALTIME)` でApfsTime（ナノ秒）を取得
- 33msループは `Timer` でシンプルに
- SwiftUIは `Text` で受け取って表示するだけ
- 3ファイルです。構造はシンプルにまとめました

.  

| 項目 | 内容 | コメント |
|-----|------|--|
| 時刻取得 | `clock_gettime(CLOCK_REALTIME)` | APFSと同じカーネル時刻源 |
| ms計算 | `tv_nsec / 1_000_000`|整数除算、誤差ゼロ |
| ループ | `Timer.publish` | シンプル、精度は雑でOK |
| View | `Text(vm.timeString)`| 表示するだけ |

.  
.  

# (Proj) m001_hello_SwiftUI

***※以下は全て作成者：Claude AIによる説明です***

.

Swift（macOS 26）+ SwiftUI の時刻表示プロジェクト。  
33msタイマーが現在時刻（`hh:mm:ss.sss`）を取得し、SwiftUIの画面に表示する。

---

## 概要

```
:------------------------------:
:       TimerViewModel         :
:                              :
:    Timer (33ms cycle)        :
:          |                   :
:    clock_gettime()           :
:     -> hh:mm:ss.sss          :
:          |                   :-----------------------------:
:    @Published timeString ----------> ContentView           :
:                              :            |                :
:------------------------------:            |                :
                             :              v                :
                             :      Text(vm.timeString)      :
                             :              |                :
                             :              v                :
                             :      Display on screen        :
                             :                               :
                             :-------------------------------:
```

- **TimerViewModel側**：時刻の取得・フォーマット・公開のみ担当
- **ContentView側**：受け取った文字列を表示するだけ
- **通信手段**：`@Published` プロパティ（Combine フレームワーク）

---

## ファイル構成

```
m001_hello_SwiftUI/
├── m001_hello_SwiftUIApp.swift   # @main エントリポイント
├── TimerViewModel.swift          # タイマー・時刻取得・フォーマット
└── ContentView.swift             # SwiftUI View（表示のみ）
```

---

## 時刻の取得：clock_gettime(CLOCK_REALTIME)

```swift
var ts = timespec()
clock_gettime(CLOCK_REALTIME, &ts)
```

```
ts.tv_sec   → エポック秒（整数）
ts.tv_nsec  → ナノ秒部分（整数）
```

`CLOCK_REALTIME` はAPFSが内部で使っているカーネル時刻源と同じもの。  
**整数演算だけ**で完結するため、浮動小数点誤差がゼロ。

```
tv_sec  の分解（整数除算）
    totalSec % 60        → 秒
   (totalSec / 60) % 60  → 分
   (totalSec / 3600) % 24 → 時

tv_nsec の分解
    tv_nsec / 1_000_000  → ミリ秒（切り捨て）
```

---

## 33ms タイマー

```swift
Timer.publish(every: 0.033, on: .main, in: .common)
    .autoconnect()
    .sink { [weak self] _ in self?.update() }
```

| 項目 | 内容 |
|------|------|
| 実行スレッド | メインスレッド（UIスレッド） |
| 精度 | 雑でOK（表示用途のため） |
| 停止方法 | `cancellable?.cancel()` |

---

## SwiftUI との接続

```
@Published timeString   ←  update() が毎回書き換え
       ↓
ContentView が自動で再描画（SwiftUIの仕組み）
       ↓
Text(vm.timeString) が新しい文字列を表示
```

`@StateObject` で ViewModel のライフサイクルを View に束ねているため、  
画面が表示されている間だけタイマーが動く。

---

## 使用 API 一覧

| 使用 API | 所属 | 最低要件 | macOS 26 |
|---------|------|---------|:----------:|
| `clock_gettime(CLOCK_REALTIME)` | Darwin / POSIX | macOS 10.12 | ✅ |
| `timespec` | Darwin / POSIX | macOS 10.12 | ✅ |
| `Timer.publish(every:on:in:)` | Combine | macOS 10.15 | ✅ |
| `AnyCancellable` | Combine | macOS 10.15 | ✅ |
| `ObservableObject` | Combine | macOS 10.15 | ✅ |
| `@Published` | Combine | macOS 10.15 | ✅ |
| `@StateObject` | SwiftUI | macOS 11.0 | ✅ |
| `@MainActor` | Swift Concurrency | macOS 12.0 | ✅ |
| `Text(_:)` | SwiftUI | macOS 10.15 | ✅ |
| `Font.system(size:weight:design:)` | SwiftUI | macOS 10.15 | ✅ |
| `String(format:)` | Foundation | macOS 10.0 | ✅ |

.

<div align=center>

**追記⚡️ zipファイルを [用意しました](https://github.com/cat-nekomaru/web-opentoonz/tree/main/floor-2026/f001/d025/download)**

</div>

.

🦆

---
.  


<div align=center>
<img width=450 src=img/d025-s01.png><BR>
<img width=450 src=img/d025-s02.png><BR>
<img width=450 src=img/d025-s03.png><BR>
<img width=550 src=img/d025-s04.png><BR>
<img width=300 src=img/d025-s05.png><BR>
</div>

.  

🏰