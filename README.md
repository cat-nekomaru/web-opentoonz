# 🇺🇸Web-OpenToonz

An open-source, next-generation web/mobile toolkit optimized for traditional 2D animation workflows.

## The .TLW2 Format: Designed for Speed & Robustness

While the ultimate goal of this project is to develop `.tlw` (a ZIP-based complete production format), we created **`.tlw2`** as a lightweight, sequential image file format specifically designed for **high-speed validation, instant previews, and robust file I/O benchmarking**.

### Why `.tlw2` is built differently:
- **Zero-Calculation Previews**: By placing a 2500-byte capped preview block immediately after the 1024-byte fixed header, any viewer can stream and render the first frame instantly by reading just the first 4KB of the file.
- **512-Byte Sector Alignment**: Every binary block is strictly aligned to 512-byte boundaries, mimicking hardware-level sector storage. This maximizes OS file I/O cache efficiency and simplifies internal pointer calculations.
- **Fail-Safe / Self-Proving Structure**: Even if the `Index Table` is corrupted or lost due to a write failure, a simple binary scan for the PNG signature and the `IEND` (stop byte) marker allows 100% recovery of the embedded thumbnails.  

.  

*Let's see also:*
https://github.com/cat-nekomaru/web-opentoonz/blob/main/TLW2/tlw2-simple.md

🗽
# 🇯🇵Web-OpenToonz
モバイルファースト・縦持ち最適なお絵描きアプリのプロトタイプ。  
将来的にOpenToonz `.tnz`互換の軽量XMLデータ構造を目指します。  

## 特徴
- ブラウザだけでアニメーション作業が可能です
- OpenToonzのセルに対応（`.tlv`フォーマット）
- タブレット縦位置（ポートレート）に最適化
- 下部に固定されたツールバー
- 2台目のスマホをモニターとして活用（将来的にWebSocket）
- HTML5 Canvas + JavaScript（軽量・低スペック対応）

.  

***Demo Pages Link:***  
1. [2D canvas, 6x4K image, UI check](https://cat-nekomaru.github.io/web-opentoonz/github-pages/A/0100/032_6x4-divideLR.html)   

2. [webGL, 3D model, texture mapping](https://cat-nekomaru.github.io/web-opentoonz/github-pages/A/0100/050_12-flat-face.html)  

3. [webGPU, compare w/webGL](https://cat-nekomaru.github.io/web-opentoonz/github-pages/A/0100/052_12ff-webGPU.html)  

.  

***開発日誌：*** [LEARNINGS.md 🐈⚡️](https://github.com/cat-nekomaru/web-opentoonz/blob/main/LEARNINGS.md)


🐾
---
