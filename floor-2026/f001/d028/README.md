# Day 28（d028）
.  

コーディングをしますか？（はい／[いいえ](../)）

💻 

. 

## 動作の様子

.  

<div align=center>
<video src="⭐️"></video></div>

.  

# (Proj) m002_swift_beep

***※以下は全て作成者：Claude AIによる説明です***

.

SwiftUI（macOS 26）+ WKWebView の音声通信テストプロジェクト。
HTML（JavaScript）のボタン操作がBase64エンコードしたwavデータをWKWebViewブリッジで送信し、Swift側がそれを受信・デコードして AVAudioEngine で再生する。

---

## 概要

```
:--------------------------------------:
:             WebView - UI             :
:                                      :
:  [SINE]        [PLAY]    [SILENT]   :
:    |              |          |       :
:  Base64(wav)  cmd:play  cmd:silent   :
:    |              |          |       :
:--------------------------------------:
                   |
     messageHandlers.beepEngine
                   |
   :-------------------------------:
   :         Swift - Engine        :
   :                               :
   :   BeepCoordinator             :
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

- **HTML側**：wavデータの生成・Base64エンコード・WKWebViewブリッジ送信を担当
- **Swift側**：メッセージ受信・Base64デコード・AVAudioEngine再生を担当
- **通信手段**：`WKScriptMessageHandler`（`window.webkit.messageHandlers.beepEngine`）

---

## ファイル構成

```
m002_swift_beep/
├── BeepEngine.swift   # WKWebView host / audio engine / SwiftUI app
└── ui.html            # 3-button UI + Base64 wav embedded
```

---

## 通信プロトコル（JSON）

```javascript
// Button 1: wav send
{ "cmd": "loadWAV", "data": "UklGR..." }   // Base64 wav

// Button 2: play
{ "cmd": "play" }

// Button 3: silent pulse
{ "cmd": "silentPulse" }
```

---

## wavデータの仕様

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
// cmd: loadWAV -> Base64 decode -> AVAudioPCMBuffer
let data = Data(base64Encoded: base64)
let file = try AVAudioFile(forReading: tmpURL)
try file.read(into: pcmBuffer)

// cmd: play -> AVAudioPlayerNode
playerNode.scheduleBuffer(buffer, at: nil, options: [])
playerNode.play()

// cmd: silentPulse -> generate buffer on engine side
for i in 0..<1632 {
    channelData[i] = (i < 4) ? 1.0 : 0.0
}
```

---

## Button 3（silentPulse）の設計メモ

```
First 4 samples : 1.0 (Float32 peak)
Remaining 1,628 : 0.0 (silence)

-> play via Button 2: audible as a very short click
-> contrast with Button 1 -> 2: sustained 1kHz tone
-> buffer is generated on the Swift side;
   JS sends only the one-word command "silentPulse"
```

---

## API 互換性メモ

| API / Framework | belongs to | requirement | macOS 26 |
|-----------------|-----------|-------------|:--------:|
| `WKWebView` | WebKit | macOS 10.10 | ✅ |
| `WKScriptMessageHandler` | WebKit | macOS 10.10 | ✅ |
| `WKUserContentController` | WebKit | macOS 10.10 | ✅ |
| `evaluateJavaScript(_:completionHandler:)` | WebKit | macOS 10.10 | ✅ |
| `NSViewRepresentable` | SwiftUI | macOS 10.15 | ✅ |
| `AVAudioEngine` | AVFAudio | macOS 10.10 | ✅ |
| `AVAudioPlayerNode` | AVFAudio | macOS 10.10 | ✅ |
| `AVAudioFile(forReading:)` | AVFAudio | macOS 10.10 | ✅ |
| `AVAudioPCMBuffer` | AVFAudio | macOS 10.10 | ✅ |
| `AVAudioFormat` | AVFAudio | macOS 10.10 | ✅ |
| `AVAudioFormat.pcmFormatFloat32` | AVFAudio | macOS 10.10 | ✅ |
| `Data(base64Encoded:)` | Foundation | macOS 10.9 | ✅ |
| `Data.write(to:)` | Foundation | macOS 10.9 | ✅ |
| `FileManager.temporaryDirectory` | Foundation | macOS 10.12 | ✅ |
| `JSONSerialization` | Foundation | macOS 10.7 | ✅ |
| `App` (SwiftUI lifecycle) | SwiftUI | macOS 11.0 | ✅ |
| `WindowGroup` | SwiftUI | macOS 11.0 | ✅ |
| `@main` | Swift 5.3 | macOS 11.0 | ✅ |

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
<img width=450 src=img/d028-s02.png><BR>
<img width=450 src=img/d028-s03.png><BR>
</div>

.  

🏰