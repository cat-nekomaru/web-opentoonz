# Day 28（d028）
.  

コーディングをしますか？（はい／[いいえ](../)）

💻 

. 

## 動作の様子

.  

<div align=center>
<video src="https://github.com/user-attachments/assets/e006debc-9c37-46e3-b71a-92647ae08824"></video></div>

.  

# (Proj) m002_swift_beep

***※以下は全て作成者：Claude AIによる説明です***

.

SwiftUI（macOS 26）のネイティブUIと AVAudioEngine を組み合わせた音声再生テストプロジェクト。
ボタン操作で Base64 エンコードした wav データをエンジン側に渡し、AVAudioPlayerNode で再生する。

---

## 概要

```
:--------------------------------------:
:           SwiftUI - UI               :
:                                      :
:  [SINE]       [PLAY]     [SILENT]    :
:    |             |           |       :
:  Base64(wav)  cmd:play  cmd:silent   :
:    |             |           |       :
:--------------------------------------:
                  |
            function call
                  |
   :-------------------------------:
   :        Swift - Engine         :
   :                               :
   :   BeepAudioEngine             :
   :     loadWAV(b64)              :
   :       Base64 decode           :
   :       AVAudioPCMBuffer        :
   :                               :
   :     play()                    :
   :       AVAudioPlayerNode       :
   :                               :
   :     silentPulse()             :
   :       [1.0 x4][0.0...]        :
   :-------------------------------:
```

- **SwiftUI側**：wav データの生成・Base64 埋め込み・ボタン操作・ログ表示を担当
- **Engine側**：Base64 デコード・AVAudioPCMBuffer 生成・AVAudioPlayerNode 再生を担当
- **通信手段**：同一プロセス内の直接関数呼び出し

---

## ファイル構成

```
m002_swift_beep/
├── ContentView.swift       # SwiftUI UI + Base64 wav embedded + @main
└── BeepAudioEngine.swift   # AVAudioEngine / AVAudioPlayerNode
```

---

## wav データの仕様

| item | value |
|------|-------|
| format | PCM 16bit |
| channel | mono (1ch) |
| sample rate | 48,000 Hz |
| frequency | 1,000 Hz (sine wave) |
| length | 1,632 samples (1 frame @ 30fps) |
| encoding | Base64 (4,412 chars) |

---

## Swift側の処理フロー

```swift
// Button SINE -> Base64 decode -> AVAudioPCMBuffer
let data = Data(base64Encoded: base64)
let file = try AVAudioFile(forReading: tmpURL)
try file.read(into: pcmBuffer)

// Button PLAY -> AVAudioPlayerNode
playerNode.scheduleBuffer(buffer, at: nil, options: [])
playerNode.play()

// Button SILENT -> generate buffer on engine side
for i in 0..<1632 {
    channelData[i] = (i < 4) ? 1.0 : 0.0
}
```

---

## Button SILENT の設計メモ

```
First 4 samples : 1.0 (Float32 peak)
Remaining 1,628 : 0.0 (silence)

-> PLAY: audible as a very short click
-> contrast with SINE -> PLAY: sustained 1kHz tone ("pit")
-> buffer is generated on the Swift side;
   UI passes only the function call "silentPulse()"
```

---

## API 互換性メモ

| API / Framework | belongs to | requirement | macOS 26 |
|-----------------|-----------|-------------|:--------:|
| `SwiftUI` | SwiftUI | macOS 10.15 | ✅ |
| `@main` | Swift 5.3 | macOS 11.0 | ✅ |
| `App` | SwiftUI | macOS 11.0 | ✅ |
| `WindowGroup` | SwiftUI | macOS 11.0 | ✅ |
| `@State` | SwiftUI | macOS 10.15 | ✅ |
| `ScrollViewReader` | SwiftUI | macOS 11.0 | ✅ |
| `AVAudioEngine` | AVFAudio | macOS 10.10 | ✅ |
| `AVAudioPlayerNode` | AVFAudio | macOS 10.10 | ✅ |
| `AVAudioFile(forReading:)` | AVFAudio | macOS 10.10 | ✅ |
| `AVAudioPCMBuffer` | AVFAudio | macOS 10.10 | ✅ |
| `AVAudioFormat` | AVFAudio | macOS 10.10 | ✅ |
| `AVAudioFormat.pcmFormatFloat32` | AVFAudio | macOS 10.10 | ✅ |
| `Data(base64Encoded:)` | Foundation | macOS 10.9 | ✅ |
| `Data.write(to:)` | Foundation | macOS 10.9 | ✅ |
| `FileManager.temporaryDirectory` | Foundation | macOS 10.12 | ✅ |

.

🦆

---
.

<div align=center>

**(Proj) zipファイルを [用意しました](https://github.com/cat-nekomaru/web-opentoonz/tree/main/floor-2026/f001/d028/download)**

</div>

.  

<div align=center>
<img width=450 src=img/d028-s01.png><BR>
<img width=620 src=img/d028-s02.png><BR>
<img width=480 src=img/d028-s03.png><BR>
</div>

.  

🏰