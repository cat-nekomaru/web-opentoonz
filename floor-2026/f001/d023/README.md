# Day 23（d023）
.  

コーディングをしますか？（はい／[いいえ](../)）

💻 

. 

## 今回の内容

- Kt: 1000ms Timerを使用して時刻を取得する
- JS: それをテキストで受信して表示。コード量がとても少なくて驚いた
- GUI側を極力簡素にしておけば、かなり使いやすいツールになる気がする
- それにしてもAIがなければ不可能に思える。必要な知識量が膨大すぎる

``` html
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>WS Time Receiver🕰️</title>
</head>
<body>
  <h2>受信時刻</h2>
  <p id="time">--:--:--</p>

  <script>
    const ws = new WebSocket("ws://192.168.179.61:9000");

    ws.onopen    = () => console.log("接続成功");
    ws.onmessage = (e) => document.getElementById("time").textContent = e.data;
    ws.onerror   = (e) => console.error("エラー", e);
    ws.onclose   = ()  => console.log("切断");
  </script>
</body>
</html>
```

``` js
Android (Kotlin)
  └─ Java-WebSocket サーバー (port 9000)
       └─ Timer で 1000ms ごとに時刻送信
            └─ ブラウザ (JavaScript) ws://192.168.179.61:9000
```

.  

## (Info) Android app development starting kit

- Visual Studio Code（VSCode）をインストール
  - Live Serverエクステンションを追加
  - ⭐️詳しい手順はAIに教えてもらう
  - あとで行う：JavaScriptをAndroidデバイスに配信する
- Android Studio（IDE）をインストール
- IDEに新規プロジェクトを作成
  - New Project > Empty Viws Activity > Nextボタンを押す
    - Name：m001-ws-TimeSender
    - Save Location:任意
    - Minimum SDK: API 28
  - ⭐️下にスクショを貼り付けています
  - FinishボタンでIDE画面が開く（セットアップ完了まで数秒かかります）
- 4つのコードの書き換えと、2つのコードを追加
  - Kotlinプロジェクトのルート `./r001-api28-kt/`
  - 全部用意できたらビルドできるか確認！
  - ⭐️詳しい手順はAIに教えてもらう（可能であればGitHubを活用して取得）
    1. build.gradle.kts (app)
    2. AndroidManifest.xml
    3. MainActivity.kt
    4. TimeWebSocketServer.kt（新規作成する）
    5. activity_main.xml
    6. res/xml/network_security_config.xml（これも新規作成）
  - `MainActivity` これがメインのコード😻
  - `TimeWebSocketServer` 増設したコード😼
  - `build.gradle.kts` 設定ファイル、有名なもの😺
  - `他のものALL` 誰にも理解できないAndroidのダークサイド🙀
- Android端末とIDEを接続
  - USBケーブルでPCと接続する
  - Android端末を開発者モードにする
  - IDEでRunを実行、KotlinアプリがAndroidで起動するか確認！
  - ⭐️詳しい手順はAIに教えてもらう
- Android端末とPCを同じネットワークにする
  - 端末をPCと同じWi-Fiに接続。もしくは端末のテザリングにPCを接続する
  - あとで使う：Android端末のIPアドレスを確認する
  - ⭐️詳しい手順はAIに教えてもらう
- JavaScriptを送り込んで実行
  - JSプロジェクトのルート `./r001-javascript/`
  - この中にある`index.html`をDLする
  - 上記コード内にあるIPアドレスを修正（Android端末のもの）
  - VSCodeで開いてから、Live ServerをONにする
  - PCのアドレス（例 192.168.179.50:5050）をAndroidのブラウザからアクセス、
  - index.htmlを見つけて開く！
  - ⭐️詳しい手順はAIに教えてもらう
  - 以上、正常動作を確認できれは完了です。🐈..
- 参考URL：[Google AIとの雑談](https://share.google/aimode/YQX612Mh0FBWnQvlc)

.  

<div align=center>
<img width=450 src=img/d023-s01.png><BR><BR>
<img width=450 src=img/d023-s02.png><BR><BR>
<img width=450 src=img/d023-s03.png>
</div>

.  

🏰