import XCTest
import Combine
@testable import m001_hello_SwiftUI

// MARK: - Helper

/// テスト用に固定 Date を返すクロックを生成する
private func fixedClock(_ date: Date) -> () -> Date { { date } }

/// "hh:mm:ss.sss" の各フィールドを分解する
private struct TimeParts {
    let hh: Int; let mm: Int; let ss: Int; let sss: Int
    init?(_ s: String) {
        let parts = s.split(separator: ":").map(String.init)
        guard parts.count == 3 else { return nil }
        let secParts = parts[2].split(separator: ".").map(String.init)
        guard secParts.count == 2,
              let h  = Int(parts[0]),
              let m  = Int(parts[1]),
              let sc = Int(secParts[0]),
              let ms = Int(secParts[1]) else { return nil }
        hh = h; mm = m; ss = sc; sss = ms
    }
}

/// hh:mm:ss.sss の時刻を持つ Date を作る
/// DateComponents.nanosecond は浮動小数点誤差があるため使わない。
/// 代わりに「今日の 00:00:00 UTC+9」を基点にして秒＋ms をエポック秒で加算する。
private func makeDate(h: Int, m: Int, s: Int, ms: Int) -> Date {
    // UTC での今日 0時0分0秒 を Calendar で取得し、そこに offset を加える
    var comps  = DateComponents()
    comps.hour = h; comps.minute = m; comps.second = s
    // 秒まで Calendar で作り、ms は TimeInterval で加算 → 浮動小数点誤差が ms 未満に収まる
    let base   = Calendar.current.date(from: comps)!
    return base.addingTimeInterval(Double(ms) / 1_000.0)
}

// MARK: - Tests

@MainActor
final class TimerViewModelTests: XCTestCase {

    // ────────────────────────────────────────────
    // MARK: 1. フォーマット — 静的メソッドのテスト
    // ────────────────────────────────────────────

    /// 正常系: 任意の Date が "HH:MM:SS.mmm" 形式に変換されること
    func test_format_returnsCorrectPattern() {
        let date   = makeDate(h: 14, m: 5, s: 3, ms: 123)
        let result = TimerViewModel.format(date: date)

        let regex = try! NSRegularExpression(pattern: #"^\d{2}:\d{2}:\d{2}\.\d{3}$"#)
        let range = NSRange(result.startIndex..., in: result)
        XCTAssertNotNil(
            regex.firstMatch(in: result, range: range),
            "フォーマットが 'HH:MM:SS.mmm' になっていない: \(result)"
        )
    }

    /// 時・分・秒・ミリ秒が正しく埋め込まれること
    func test_format_correctValues() {
        let date   = makeDate(h: 9, m: 8, s: 7, ms: 6)
        let result = TimerViewModel.format(date: date)
        let parts  = TimeParts(result)!

        XCTAssertEqual(parts.hh,  9, "hour")
        XCTAssertEqual(parts.mm,  8, "minute")
        XCTAssertEqual(parts.ss,  7, "second")
        XCTAssertEqual(parts.sss, 6, "millisecond")
    }

    /// 境界値: 0時0分0秒0ms
    func test_format_midnight() {
        let result = TimerViewModel.format(date: makeDate(h: 0, m: 0, s: 0, ms: 0))
        XCTAssertEqual(result, "00:00:00.000")
    }

    /// 境界値: 23時59分59秒999ms
    func test_format_endOfDay() {
        let result = TimerViewModel.format(date: makeDate(h: 23, m: 59, s: 59, ms: 999))
        XCTAssertEqual(result, "23:59:59.999")
    }

    /// 2桁ゼロパディング: 時・分・秒がひと桁のとき "0x" になること
    func test_format_zeroPadding() {
        let result = TimerViewModel.format(date: makeDate(h: 1, m: 2, s: 3, ms: 4))
        XCTAssertEqual(result, "01:02:03.004")
    }

    /// ミリ秒3桁ゼロパディング: 1ms → "001"
    func test_format_millisecondZeroPadding() {
        let result = TimerViewModel.format(date: makeDate(h: 12, m: 0, s: 0, ms: 1))
        let parts  = TimeParts(result)!
        XCTAssertEqual(parts.sss, 1, "1ms が \(parts.sss) になっている")
        XCTAssertTrue(result.hasSuffix(".001"), "末尾が '.001' であること: \(result)")
    }

    // ────────────────────────────────────────────
    // MARK: 2. tick() — timeString が更新されること
    // ────────────────────────────────────────────

    /// tick() を呼ぶと timeString が更新されること
    func test_tick_updatesTimeString() async {
        let vm = TimerViewModel(clock: fixedClock(makeDate(h: 10, m: 20, s: 30, ms: 456)))
        vm.tick()
        XCTAssertEqual(vm.timeString, "10:20:30.456")
    }

    /// tick() を複数回呼ぶたびに最新のクロック値が反映されること
    func test_tick_multipleCallsReflectLatestTime() async {
        var currentMs = 0
        let dynamicClock: () -> Date = { makeDate(h: 0, m: 0, s: 0, ms: currentMs) }
        let vm = TimerViewModel(clock: dynamicClock)

        currentMs = 100; vm.tick()
        XCTAssertEqual(vm.timeString, "00:00:00.100", "1回目")

        currentMs = 500; vm.tick()
        XCTAssertEqual(vm.timeString, "00:00:00.500", "2回目")

        currentMs = 999; vm.tick()
        XCTAssertEqual(vm.timeString, "00:00:00.999", "3回目")
    }

    // ────────────────────────────────────────────
    // MARK: 3. 初期値
    // ────────────────────────────────────────────

    /// 生成直後の timeString はプレースホルダー文字列であること
    func test_initialTimeString_isPlaceholder() {
        let vm = TimerViewModel()
        XCTAssertEqual(vm.timeString, "--:--:--.---")
    }

    // ────────────────────────────────────────────
    // MARK: 4. start / stop
    // ────────────────────────────────────────────

    /// start() 後、33ms 以上待つと timeString が更新されること
    func test_start_updatesTimeStringAfterInterval() async throws {
        let vm = TimerViewModel(interval: 0.033)
        vm.start()

        try await Task.sleep(nanoseconds: 100_000_000)

        XCTAssertNotEqual(vm.timeString, "--:--:--.---",
            "start() 後 100ms 経過しても timeString が初期値のまま")

        vm.stop()
    }

    /// stop() 後は timeString が変化しないこと
    func test_stop_freezesTimeString() async throws {
        let vm = TimerViewModel(interval: 0.033)
        vm.start()

        try await Task.sleep(nanoseconds: 50_000_000)
        vm.stop()
        let snapshot = vm.timeString

        try await Task.sleep(nanoseconds: 100_000_000)
        XCTAssertEqual(vm.timeString, snapshot, "stop() 後に timeString が変化した")
    }

    /// start() を2回呼んでも Timer が重複しないこと
    func test_start_idempotent() async throws {
        let vm = TimerViewModel(interval: 0.033)
        vm.start()
        vm.start()   // 2回目は無視されるべき

        try await Task.sleep(nanoseconds: 100_000_000)
        let regex = try NSRegularExpression(pattern: #"^\d{2}:\d{2}:\d{2}\.\d{3}$"#)
        let range = NSRange(vm.timeString.startIndex..., in: vm.timeString)
        XCTAssertNotNil(regex.firstMatch(in: vm.timeString, range: range))
        vm.stop()
    }

    // ────────────────────────────────────────────
    // MARK: 5. Published プロパティの変更通知
    // ────────────────────────────────────────────

    /// tick() が呼ばれたとき @Published の objectWillChange が発火すること
    func test_tick_publishesChange() async {
        let vm     = TimerViewModel(clock: fixedClock(makeDate(h: 8, m: 0, s: 0, ms: 0)))
        var count  = 0
        let cancel = vm.objectWillChange.sink { count += 1 }

        vm.tick()

        XCTAssertEqual(count, 1, "objectWillChange が1回発火すること")
        cancel.cancel()
    }
}
