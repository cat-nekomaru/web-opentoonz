import Foundation
import Combine

/// 33ms毎にタイマーを起動し、現在時刻を hh:mm:ss.sss 形式の文字列で公開する ViewModel
@MainActor
final class TimerViewModel: ObservableObject {

    // MARK: - Published

    /// View が表示する時刻文字列（例: "14:05:03.123"）
    @Published private(set) var timeString: String = "--:--:--.---"

    // MARK: - Private

    private var timer: AnyCancellable?
    private let interval: TimeInterval
    private let clock: () -> Date          // テスト時に差し替えられるクロック

    // MARK: - Init

    init(interval: TimeInterval = 0.033, clock: @escaping () -> Date = Date.init) {
        self.interval = interval
        self.clock   = clock
    }

    // MARK: - Public API

    func start() {
        guard timer == nil else { return }
        timer = Timer.publish(every: interval, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in
                self?.tick()
            }
    }

    func stop() {
        timer?.cancel()
        timer = nil
    }

    // MARK: - Internal (テスト用に internal)

    /// 現在時刻を受け取り timeString を更新する
    func tick() {
        timeString = Self.format(date: clock())
    }

    /// Date → "hh:mm:ss.sss" 変換（static にしてテストしやすくする）
    static func format(date: Date) -> String {
        let cal = Calendar.current
        let h   = cal.component(.hour,   from: date)
        let m   = cal.component(.minute, from: date)
        let s   = cal.component(.second, from: date)

        // Calendar.component(.nanosecond) は浮動小数点誤差で不正確なため
        // timeIntervalSince1970 の小数部から ms を直接計算する
        let ms  = Int(date.timeIntervalSince1970 * 1_000) % 1_000

        return String(format: "%02d:%02d:%02d.%03d", h, m, s, ms)
    }
}
