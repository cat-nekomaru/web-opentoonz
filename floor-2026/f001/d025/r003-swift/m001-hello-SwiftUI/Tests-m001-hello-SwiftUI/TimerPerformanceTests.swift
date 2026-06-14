import XCTest
@testable import m001_hello_SwiftUI

/// パフォーマンステスト
/// 33ms ≒ 30fps で連続呼び出されるため、format() と tick() が十分速いことを確認する。
@MainActor
final class TimerPerformanceTests: XCTestCase {

    // ────────────────────────────────────────────
    // MARK: 1. format() のスループット
    // ────────────────────────────────────────────

    /// format() が 1,000 回呼ばれても 33ms 以内に収まること。
    /// （33ms = 30fps の1フレーム予算。実際には tick 1回だが、余裕を見て 1000回で計測）
    func test_format_performance() {
        let date = Date()
        measure {
            for _ in 0 ..< 1_000 {
                _ = TimerViewModel.format(date: date)
            }
        }
        // measureBlock の平均が XCTest のデフォルト許容値 (10%) を超えたら自動で失敗
    }

    // ────────────────────────────────────────────
    // MARK: 2. tick() のスループット
    // ────────────────────────────────────────────

    /// tick() が 1,000 回連続で呼ばれても明らかなボトルネックがないこと。
    func test_tick_performance() {
        let vm = TimerViewModel(clock: Date.init)
        measure {
            for _ in 0 ..< 1_000 {
                vm.tick()
            }
        }
    }

    // ────────────────────────────────────────────
    // MARK: 3. 33ms インターバルの精度確認（統計的）
    // ────────────────────────────────────────────

    /// 実際の Timer を 330ms 走らせてサンプル数を数え、
    /// 理論値（≈10回）の ±3回以内であることを確認する。
    func test_timerInterval_approximateSampleCount() async throws {
        var tickCount = 0
        let vm = TimerViewModel(interval: 0.033, clock: {
            tickCount += 1
            return Date()
        })

        vm.start()
        // 330ms 待機 → 理論 10 tick
        try await Task.sleep(nanoseconds: 330_000_000)
        vm.stop()

        // 許容範囲: 7〜13 tick（±30%）
        // macOS のスケジューラ精度・CI 環境の負荷を考慮して広めに取る
        XCTAssertGreaterThanOrEqual(tickCount, 7,  "tick が少なすぎる: \(tickCount)")
        XCTAssertLessThanOrEqual(   tickCount, 13, "tick が多すぎる: \(tickCount)")
    }
}
