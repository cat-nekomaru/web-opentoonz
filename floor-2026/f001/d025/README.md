# Day 25（d025）
.  

コーディングをしますか？（はい／[いいえ](../)）

💻 

. 

## （講師）Claudeさん✨

**依頼：**  
ソフトウェア開発のテストコードをお願いします。➠ 言語：Swift(GUIあり)、プロジェクト名：m001-hello-SwiftUI 、macOS 26.x、➠ 機能は、33ms毎のTimer から 時刻 hh:mm:ss.sss（1ms単位）を表示します。➠ （詳しくないので推測で話しますが）SwiftUI側はテキストを受け取って表示するだけのシンプルなものです。

.  

**🤖 m001-hello-SwiftUI テストコード**
- SwiftUIプロジェクトのテストコードを作成します。まずプロジェクト構造を確認してから実装します。

**🤖 設計方針**
- TimerViewModel のユニットテスト（ロジック分離）
- 時刻フォーマット のテスト（hh:mm:ss.sss）
- Timerの動作 テスト（33ms間隔）
- SwiftUIビューはシンプルなのでViewModelテストに集中

.  
.  

# (Proj) m001-hello-SwiftUI

***※以下は全て作成者：Claude AIによる説明です***

.

Swift（macOS 26）+ SwiftUI の時刻表示テストプロジェクト。  
33msタイマーが現在時刻（`hh:mm:ss.sss`）を取得し、SwiftUIの画面に表示する。

---

## 概要

```
┌─────────────────────────────┐
│       TimerViewModel        │
│                             │
│  Timer（33ms周期）           │
│      ↓                      │
│  時刻取得 hh:mm:ss.sss      │
│      ↓                      │
│  @Published timeString ──────────→  ContentView
└─────────────────────────────┘              │
                                             ↓
                                    Text(vm.timeString)
                                             ↓
                                    画面に時刻を表示
```

- **TimerViewModel側**：時刻の取得・フォーマット・公開のみ担当
- **ContentView側**：受け取った文字列を表示するだけ
- **通信手段**：`@Published` プロパティ（Combine フレームワーク）

---

## ファイル構成

```
m001-hello-SwiftUI/
├── m001_hello_SwiftUIApp.swift   # @main エントリポイント
├── TimerViewModel.swift          # ビジネスロジック（テスト対象の中心）
├── ContentView.swift             # SwiftUI View（表示のみ）
└── Tests/
    ├── TimerViewModelTests.swift  # ユニットテスト（主軸）13個
    ├── ContentViewTests.swift     # View 結合テスト 2個
    └── TimerPerformanceTests.swift# パフォーマンステスト 3個
```

---

## テスト設計の考え方

SwiftUI の View は直接テストしにくいため、**MVVMパターン** でロジックを
`TimerViewModel` に分離しています。View は `timeString` を受け取って
表示するだけ（パッシブ）にすることで、テストが書きやすくなります。

```
Timer(33ms) ──→ TimerViewModel.tick()
                    └─ format(Date) → timeString: String
                                           └─ ContentView（Text表示のみ）
```

---

## テスト可能にするための設計ポイント

```swift
// 2つの引数を差し替えられる設計にしている
init(interval: TimeInterval = 0.033, clock: @escaping () -> Date = Date.init)
```

| 工夫 | 理由 |
|------|------|
| `clock: () -> Date` を DI | 固定時刻でテストできる |
| `format()` を `static` メソッドに | インスタンス不要で単体テスト可能 |
| `interval` を DI | テストで短い間隔を指定できる |
| `tick()` を `internal` に | テストから直接呼び出して同期的に確認できる |

テストでの使い方：

```swift
// 固定時刻を注入して、結果を即座に確認できる
let vm = TimerViewModel(clock: { fixedDate })
vm.tick()
XCTAssertEqual(vm.timeString, "13:45:30.789")
```

---

## テスト一覧

### TimerViewModelTests.swift（ユニットテスト）

| テスト名 | 検証内容 |
|----------|---------|
| `test_format_returnsCorrectPattern` | 正規表現で `HH:MM:SS.mmm` 形式を確認 |
| `test_format_correctValues` | 時・分・秒・ms が正しく変換される |
| `test_format_midnight` | `00:00:00.000` の境界値 |
| `test_format_endOfDay` | `23:59:59.999` の境界値 |
| `test_format_zeroPadding` | 1桁の値が2桁にゼロパディングされる |
| `test_format_millisecondZeroPadding` | 1ms が `001` になる |
| `test_tick_updatesTimeString` | `tick()` で `timeString` が更新される |
| `test_tick_multipleCallsReflectLatestTime` | 複数回 tick で最新値になる |
| `test_initialTimeString_isPlaceholder` | 初期値が `--:--:--.---` |
| `test_start_updatesTimeStringAfterInterval` | 100ms 待つと更新される |
| `test_stop_freezesTimeString` | `stop()` 後は変化しない |
| `test_start_idempotent` | `start()` を2回呼んでも問題ない |
| `test_tick_publishesChange` | `objectWillChange` が発火する |

### ContentViewTests.swift（結合テスト）

| テスト名 | 検証内容 |
|----------|---------|
| `test_contentView_displaysViewModelTimeString` | VM の値が正しければ View も正しい |
| `test_hostingController_viewLoads` | `NSHostingController` でロードしてクラッシュしない |

### TimerPerformanceTests.swift（パフォーマンステスト）

| テスト名 | 検証内容 |
|----------|---------|
| `test_format_performance` | `format()` を1000回呼んでも高速 |
| `test_tick_performance` | `tick()` を1000回呼んでも高速 |
| `test_timerInterval_approximateSampleCount` | 330ms で ≈10回 tick される |

---

## 使用 API 一覧

| 使用 API | 所属フレームワーク | 最低要件 | macOS 26 |
|---------|---------------|---------|----------|
| `Timer.publish(every:on:in:)` | Combine | macOS 10.15 | ✅ |
| `@Published` | Combine | macOS 10.15 | ✅ |
| `ObservableObject` | Combine | macOS 10.15 | ✅ |
| `@StateObject` | SwiftUI | macOS 11.0 | ✅ |
| `@MainActor` | Swift Concurrency | macOS 12.0 | ✅ |
| `Task.sleep(nanoseconds:)` | Swift Concurrency | macOS 12.0 | ✅ |
| `Calendar.component(_:from:)` | Foundation | macOS 10.0 | ✅ |
| `String(format:)` | Foundation | macOS 10.0 | ✅ |
| `Text(_:)` | SwiftUI | macOS 10.15 | ✅ |
| `Font.system(size:weight:design:)` | SwiftUI | macOS 10.15 | ✅ |
| `NSHostingController` | SwiftUI | macOS 10.15 | ✅ |
| `XCTestCase` | XCTest | macOS 10.0 | ✅ |
| `measure(_:)` | XCTest | macOS 10.0 | ✅ |
| `NSRegularExpression` | Foundation | macOS 10.7 | ✅ |

---

## Xcode セットアップ手順

1. **macOS / App** で新規プロジェクト作成
   - Interface: **SwiftUI**、Language: **Swift**
   - Product Name: `m001-hello-SwiftUI`

2. プロダクションコード3ファイルを差し替え  
   `TimerViewModel.swift`、`ContentView.swift`、`m001_hello_SwiftUIApp.swift`

3. **File → New → Target → Unit Testing Bundle** を追加  
   テストターゲット名: `m001-hello-SwiftUITests`

4. `Tests/` フォルダの `.swift` を **テストターゲット** に追加

5. <kbd>⌘ + U</kbd> で全テスト実行

---

## 注意事項

- `@MainActor` をテストクラスに付与しているのは、`TimerViewModel` が
  `@MainActor final class` であるため（UIスレッドでの動作を保証）。
- `ContentViewTests` の `NSHostingController` は **macOS 専用** API。
  iOS / iPadOS の場合は `UIHostingController` に変更してください。
- パフォーマンステストの Timer 精度許容範囲（±30%）は CI 環境の負荷を考慮した広めの設定。
  必要に応じて `setUpWithBaseline` で厳密なベースラインを設定してください。

.

🦆

---
.  

⭐️書きかけ

<div align=center>
<img width=450 src=img/d025-s01.png>
</div>

.  

🏰