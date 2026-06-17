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

```
⭐️
mdID=0x16（d028）のコードを書いてください。

README.md：https://github.com/cat-nekomaru/web-opentoonz/tree/main/floor-2026/f001

プロジェクト名：m002_swift_beep
動作：UI側に3つのボタン。押すとエンジン側で短いwavを再生。バッファのクリアも可能。

ボタン1：サイン波データ送信
  PCM16bit 48kHz wav, 1ch モノラル, 1kHz サイン波（音圧0スタート）
  サンプル数=1632（約1frame@30fps）
  Base64エンコードしてWebView側コードに埋め込む

ボタン2：発音コマンド
  バッファ内のデータを再生

ボタン3：サイレント用コマンド
  同サンプル数（1632）で先頭4サンプルのみ最大値、残りは無音
  バッファ生成はエンジン側で行う

ボタンラベル：英単語


（ドキュメント用）
先頭にあるアスキーアートですが、日本語が混ざると表示が崩れる可能性があるので、1バイト文字の英文表記でお願いします。
⭐️
```
- ＊
- ＊
- ＊

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