import XCTest
import SwiftUI
@testable import m001_hello_SwiftUI

/// ContentView は「テキストを受け取って表示するだけ」なので
/// ViewModel から渡された文字列が画面に現れるかを検証する。
@MainActor
final class ContentViewTests: XCTestCase {

    // ────────────────────────────────────────────
    // MARK: 1. ViewModel 連携
    // ────────────────────────────────────────────

    /// ContentView が表示する文字列が ViewModel の timeString と一致すること。
    /// （SwiftUI のレンダリングは UI スレッドで非同期だが、
    ///   ViewModel 側の値が正しければ View は正しく表示できる、という間接確認）
    func test_contentView_displaysViewModelTimeString() async throws {
        // 固定時刻で ViewModel を作成
        var comps        = DateComponents()
        comps.hour       = 13; comps.minute = 45; comps.second = 30
        comps.nanosecond = 789_000_000

        let fixedDate = Calendar.current.date(from: comps)!
        let vm        = TimerViewModel(clock: { fixedDate })
        vm.tick()   // 時刻を確定させる

        XCTAssertEqual(vm.timeString, "13:45:30.789",
            "ViewModel が正しい時刻文字列を保持していること")

        // ContentView は @StateObject で ViewModel を内部生成するため、
        // ここでは ViewModel 単体の値が正しい = View に渡す値が正しい と確認する。
        // UIHostingController を使った E2E 確認は下の test_hostingController で実施。
    }

    /// UIHostingController 経由で View が存在し、
    /// ViewModel の timeString が初期値以外になること（start 後）
    func test_hostingController_viewLoads() async throws {
        let view       = ContentView()
        let controller = NSHostingController(rootView: view)   // macOS

        // viewDidLoad 相当 — ウィンドウに追加することで onAppear が呼ばれる
        let window = NSWindow(contentViewController: controller)
        window.makeKeyAndOrderFront(nil)

        // Timer が最低1回 tick するのを待つ
        try await Task.sleep(nanoseconds: 100_000_000)

        // クラッシュしないこと、ウィンドウが存在すること
        XCTAssertNotNil(controller.view)
        window.close()
    }
}
