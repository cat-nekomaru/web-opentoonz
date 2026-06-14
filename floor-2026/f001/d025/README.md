# Day 25（d025）
.  

コーディングをしますか？（はい／[いいえ](../)）

💻 

. 

## 今回の内容

``` txt
🐈 ソフトウェア開発のテストコードをお願いします。➠ 言語：Swift(GUIあり)、プロジェクト名：m001-hello-SwiftUI 、macOS 26.x、➠ 機能は、33ms毎のTimer から 時刻 hh:mm:ss.sss（1ms単位）を表示します。➠ （詳しくないので推測で話しますが）SwiftUI側はテキストを受け取って表示するだけのシンプルなものです。
```

**🤖 m001-hello-SwiftUI テストコード**
- SwiftUIプロジェクトのテストコードを作成します。まずプロジェクト構造を確認してから実装します。

**設計方針**
- TimerViewModel のユニットテスト（ロジック分離）
- 時刻フォーマット のテスト（hh:mm:ss.sss）
- Timerの動作 テスト（33ms間隔）
- SwiftUIビューはシンプルなのでViewModelテストに集中


.  

<div align=center>
<img width=450 src=img/d025-s01.png>
</div>

.  

🏰