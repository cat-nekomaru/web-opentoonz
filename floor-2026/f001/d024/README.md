# Day 24（d024）
.  

コーディングをしますか？（はい／[いいえ](../)）

💻 

. 

## 今回の内容

- ⭐️書きかけ
- x
- x
- x

.  

<div align=center>
<video src="https://github.com/user-attachments/assets/343254b8-61b4-44d6-acf7-302e7d0f1b84" controls width="300"></video></div>

.  

## (Info) Android app development starting kit

- ① Android Studio（IDE）をインストール
- ② `API Lv22`用のSDKを追加
  - IDEを起動してから、
  - メニューから`Settings..`を開きます。<kbd>⌘ + ,</kbd>
  - `検索` sdk > Android SDK > ☑️ Android 5.1（API Level 22）> OKを押す
  - DLが終了したらFinishボタン、これで完了
  - ⭐️下にスクショを貼り付けています
- ③ IDEに新規プロジェクトを作成
  - New Project > Empty Viws Activity > Nextボタンを押す
    - Name：m001_hello_WebViewApi22
    - Save Location：任意
    - Minimum SDK：API 23（これ以上下げられない。コード側で修正しました）
  - FinishボタンでIDE画面が開く
  - ⭐️下にスクショを貼り付けています（未確認：Package nameの修正が必要かも？）
- ④ 3つのコードの書き換えと、1つのコードを追加
  - Kotlinプロジェクトのルート `./r002-api22-kt/`
    1. build.gradle.kts (app)
    2. MainActivity.kt
    3. activity_main.xml
    4. res/values/themes/styles.xml（新規作成するもの）
  - `build.gradle.kts` 定番の設定ファイル😼
  - `MainActivity.kt` これがメインのコード😸
  - `その他のもの` よく分かりませんが必要です😾
  - 準備できたらビルドできるか確認してください！
  - ⭐️詳しい手順はAI頼りです（再び：Package nameの修正が必要かも）
- ⑤ Android端末とIDEを接続
  - USBケーブルでPCと接続する
  - Android端末を開発者モードにする
  - IDEでRunを実行、KotlinアプリがAndroidで起動するか確認！
  - ⭐️手順はAIに教えてもらいましょう
- 以上、正常動作を確認できれは完了です。🐈..

.  

<div align=center>
<img width=450 src=img/d024-s02.png><BR>
<img width=450 src=img/d024-s03.png><BR>
<img width=450 src=img/d024-s04.png><BR>
<img width=600 src=img/d024-s05.png><BR><BR>
<img width=220 src=img/d024-s06.png><BR>
</div>

.  

🏰