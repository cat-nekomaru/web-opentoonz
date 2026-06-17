// mdID: 0x16 (d028)
// ContentView.swift — SwiftUI UI side

import SwiftUI

// MARK: - Sine wave data (PCM16 48kHz 1ch, 1kHz, 1632 samples)
private let sineWavBase64 =
        "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"
        + "S+8="


// MARK: - Log entry

private struct LogEntry: Identifiable {
    let id   = UUID()
    let time : String
    let text : String
    let kind : Kind
    enum Kind { case ui, engine, error }

    static func now() -> String {
        let f = DateFormatter()
        f.dateFormat = "HH:mm:ss.SSS"
        return f.string(from: Date())
    }
}

// MARK: - ContentView

struct ContentView: View {

    @State private var engine = BeepAudioEngine()

    @State private var uiLogs:  [LogEntry] = []
    @State private var engLogs: [LogEntry] = []

    var body: some View {
        VStack(spacing: 0) {
            headerBar
            buttonRow
            Divider()
            logPanels
            footerBar
        }
        .frame(minWidth: 540, minHeight: 400)
        .background(Color(nsColor: .windowBackgroundColor))
    }

    // MARK: Header

    private var headerBar: some View {
        VStack(spacing: 0) {
            HStack {
                Text("m002_swift_beep")
                    .font(.system(size: 12, weight: .semibold, design: .monospaced))
                Spacer()
                Text("d028 · mdID 0x16 · macOS 26 · r003")
                    .font(.system(size: 10, design: .monospaced))
                    .foregroundStyle(.secondary)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            Divider()
        }
    }
    
    // MARK: Buttons

    private var buttonRow: some View {
        HStack(spacing: 12) {
            beepButton(
                label: "SINE",
                sub:   "1kHz · 1632 samples",
                color: .blue
            ) { pressSine() }

            beepButton(
                label: "PLAY",
                sub:   "buffer → audio out",
                color: .green
            ) { pressPlay() }

            beepButton(
                label: "SILENT",
                sub:   "pulse · engine-gen",
                color: .red
            ) { pressSilent() }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
    }

    private func beepButton(
        label: String,
        sub:   String,
        color: Color,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            VStack(spacing: 3) {
                Text(label)
                    .font(.system(size: 12, weight: .bold, design: .monospaced))
                    .foregroundStyle(color)
                Text(sub)
                    .font(.system(size: 10))
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 8)
        }
        .buttonStyle(.bordered)
    }

    // MARK: Log panels

    private var logPanels: some View {
        HStack(spacing: 0) {
            logPanel(title: "UI",     dot: .blue,  entries: uiLogs)
            Divider()
            logPanel(title: "Engine", dot: .green, entries: engLogs)
        }
    }

    private func logPanel(
        title:   String,
        dot:     Color,
        entries: [LogEntry]
    ) -> some View {
        VStack(spacing: 0) {
            HStack(spacing: 6) {
                Circle()
                    .fill(dot)
                    .frame(width: 6, height: 6)
                Text(title)
                    .font(.system(size: 10, weight: .semibold))
                    .foregroundStyle(.secondary)
                Spacer()
            }
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(Color(nsColor: .underPageBackgroundColor))

            Divider()

            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 1) {
                        ForEach(entries) { e in
                            HStack(alignment: .top, spacing: 4) {
                                Text(e.time)
                                    .foregroundStyle(.tertiary)
                                Text(e.text)
                                    .foregroundStyle(textColor(e.kind))
                            }
                            .font(.system(size: 10, design: .monospaced))
                            .padding(.horizontal, 10)
                            .id(e.id)
                        }
                    }
                    .padding(.vertical, 6)
                }
                .onChange(of: entries.count) { _, _ in
                    if let last = entries.last {
                        proxy.scrollTo(last.id, anchor: .bottom)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private func textColor(_ kind: LogEntry.Kind) -> Color {
        switch kind {
        case .ui:     return .blue
        case .engine: return .green
        case .error:  return .red
        }
    }

    // MARK: Footer

    private var footerBar: some View {
        HStack {
            Spacer()
            Button("Clear Buffer") {
                let msg = engine.clearBuffer()
                appendEng(msg)
                appendUI("Clear buffer requested")
            }
            .font(.system(size: 10))
            .buttonStyle(.borderless)
            .foregroundStyle(.secondary)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 5)
        .background(Color(nsColor: .windowBackgroundColor))
    }

    // MARK: - Button actions

    private func pressSine() {
        appendUI("Button: SINE — sending WAV base64 (\(sineWavBase64.count) chars)")
        let msg = engine.loadWAV(sineWavBase64)
        appendEng(msg)
    }

    private func pressPlay() {
        appendUI("Button: PLAY — issuing play command")
        let msg = engine.play()
        appendEng(msg)
    }

    private func pressSilent() {
        appendUI("Button: SILENT — requesting engine-side pulse buffer")
        let msg = engine.silentPulse()
        appendEng(msg)
    }

    // MARK: - Log helpers

    private func appendUI(_ text: String) {
        uiLogs.append(LogEntry(time: LogEntry.now(), text: text, kind: .ui))
    }

    private func appendEng(_ text: String) {
        let kind: LogEntry.Kind = text.hasPrefix("ERROR") ? .error : .engine
        engLogs.append(LogEntry(time: LogEntry.now(), text: text, kind: kind))
    }
}

// MARK: - App entry point

@main
struct BeepApp: App {
    var body: some Scene {
        WindowGroup("m002_swift_beep") {
            ContentView()
        }
        .windowResizability(.contentMinSize)
    }
}
