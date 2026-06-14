import SwiftUI

struct ContentView: View {

    @StateObject private var vm = TimerViewModel()

    var body: some View {
        Text(vm.timeString)
            .font(.system(size: 48, weight: .medium, design: .monospaced))
            .padding(32)
            .onAppear  { vm.start() }
            .onDisappear { vm.stop() }
    }
}

#Preview {
    ContentView()
}
