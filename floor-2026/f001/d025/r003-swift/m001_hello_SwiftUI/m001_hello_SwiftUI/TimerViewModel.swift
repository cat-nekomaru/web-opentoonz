import Foundation
import Combine

@MainActor
final class TimerViewModel: ObservableObject {

    @Published private(set) var timeString: String = "--:--:--.---"

    private var cancellable: AnyCancellable?

    func start() {
        cancellable = Timer.publish(every: 0.033, on: .main, in: .common)
            .autoconnect()
            .sink { [weak self] _ in self?.update() }
    }

    func stop() {
        cancellable?.cancel()
        cancellable = nil
    }

    // MARK: - Private

    private func update() {
        // APFS / カーネルの時刻を nanosecond 精度で取得
        var ts = timespec()
        clock_gettime(CLOCK_REALTIME, &ts)

        // 秒部分 → h / m / s に分解
        let totalSec = Int(ts.tv_sec)
        let sec  = totalSec % 60
        let min  = (totalSec / 60) % 60
        let hour = (totalSec / 3600) % 24

        // ナノ秒部分 → ms（切り捨て）
        let ms = ts.tv_nsec / 1_000_000

        timeString = String(format: "%02d:%02d:%02d.%03d", hour, min, sec, ms)
    }
}
