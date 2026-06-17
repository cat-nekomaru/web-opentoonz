// mdID: 0x16 (d028)
// BeepAudioEngine.swift — Engine side

import AVFoundation

final class BeepAudioEngine {   // ObservableObject を削除

    private let engine      = AVAudioEngine()
    private let playerNode  = AVAudioPlayerNode()
    private var buffer: AVAudioPCMBuffer?

    private let sampleRate: Double        = 48000
    private let numSamples: AVAudioFrameCount = 1632

    init() { setup() }

    // MARK: - Setup

    private func setup() {
        engine.attach(playerNode)
        engine.connect(playerNode, to: engine.mainMixerNode, format: workFormat)
        do {
            try engine.start()
        } catch {
            log("ERROR: engine start — \(error.localizedDescription)")
        }
    }

    private var workFormat: AVAudioFormat {
        AVAudioFormat(
            commonFormat: .pcmFormatFloat32,
            sampleRate:   sampleRate,
            channels:     1,
            interleaved:  false
        )!
    }

    // MARK: - Public API

    // Button 1: load WAV from Base64
    func loadWAV(_ base64: String) -> String {
        guard let data = Data(base64Encoded: base64) else {
            return "ERROR: base64 decode failed"
        }
        let tmp = FileManager.default.temporaryDirectory
            .appendingPathComponent(UUID().uuidString + ".wav")
        do {
            try data.write(to: tmp)
            let file = try AVAudioFile(forReading: tmp)
            guard let pcm = AVAudioPCMBuffer(
                pcmFormat: workFormat,
                frameCapacity: AVAudioFrameCount(file.length)
            ) else { return "ERROR: buffer alloc failed" }
            try file.read(into: pcm)
            buffer = pcm
            try? FileManager.default.removeItem(at: tmp)
            return "WAV loaded — \(pcm.frameLength) samples @ \(Int(sampleRate)) Hz"
        } catch {
            return "ERROR: \(error.localizedDescription)"
        }
    }

    // Button 2: play buffer
    func play() -> String {
        guard let buf = buffer else {
            return "ERROR: no buffer — press SINE first"
        }
        ensureRunning()
        playerNode.stop()
        playerNode.scheduleBuffer(buf, at: nil, options: [])
        playerNode.play()
        return "playing \(buf.frameLength) samples"
    }

    // Button 3: generate silent pulse on engine side, then play
    func silentPulse() -> String {
        guard let pulse = AVAudioPCMBuffer(
            pcmFormat: workFormat,
            frameCapacity: numSamples
        ) else { return "ERROR: pulse buffer alloc failed" }
        pulse.frameLength = numSamples
        if let ch = pulse.floatChannelData?[0] {
            for i in 0..<Int(numSamples) {
                ch[i] = (i < 4) ? 1.0 : 0.0
            }
        }
        buffer = pulse
        // playerNode.stop() と playerNode.play() を削除
        return "silent pulse — \(numSamples) samples [1.0 x4][0.0...] — ready"
    }

    // Clear
    func clearBuffer() -> String {
        playerNode.stop()
        buffer = nil
        return "buffer cleared"
    }

    // MARK: - Private

    private func ensureRunning() {
        if !engine.isRunning { try? engine.start() }
    }

    private func log(_ msg: String) {
        print("[BeepAudioEngine] \(msg)")
    }
}
