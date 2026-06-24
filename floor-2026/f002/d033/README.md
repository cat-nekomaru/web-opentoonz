# Day 33（d033）

Raspberry Pi 5（4GB）を導入しました。

.  

🍓🥧

## 今回の内容 LIST

- 対応ずみ
  - 古い32GB microSDカード（W: 2MB/sec）
  - 無線LAN
  - DHCPクライアント
  - MACアドレスから固定IPの取得
  - ディスプレイなしでのOS起動
  - SSH：パスワード認証
  - VNCとRDPの比較
  - ドライヤーの冷風による放熱

- 未対応
  - 遅くないmicroSD
  - ヒートシンクとFAN
  - 金属ケース
  - 有線LAN
  - SSH：公開鍵認証
  - HAT：PoE, NVMe
  - HDMI：音を出す
  - 実行：Python, C lang, Kotlin
  - コンテナ：Docker, NATSサーバー
  - ブラウザ：JavaScript, webGPU, WebSocket

.  

# "RDP" xrdpセットアップ手順

***※以下は全てClaude AIによる説明です***

.  

## Pi側の設定

- パッケージをインストール
  ```bash
  sudo apt update
  sudo apt install xrdp xorgxrdp -y
  ```

- xrdpをSSL証明書グループに追加（権限エラー防止）
  ```bash
  sudo adduser xrdp ssl-cert
  sudo systemctl restart xrdp
  ```

- 状態確認（`active (running)`、ポート3389で待ち受けているか）
  ```bash
  sudo systemctl status xrdp
  ```

- **自動ログインを無効化**（重要：無効化しないとXorgが衝突してクラッシュする）
  ```bash
  sudo raspi-config
  ```
  → `System Options` → `Boot / Auto Login` → **`Desktop`**（自動ログインなし）を選択

- 再起動して反映
  ```bash
  sudo reboot
  ```

## macOS側の設定

1. Mac App Storeで **「Windows App」** を検索してインストール
2. 起動して「PCを追加」
3. **PC名** にPiのIPアドレスを入力（Pi側で `hostname -I` を実行すると確認できる）
4. 一覧から追加したPiをクリック
5. ユーザー名・パスワードはPiのログイン情報を入力
6. 初回は自己署名証明書の警告が出るが「接続」を選んで進める
7. セッション種別を聞かれたら **`Xorg`** を選択

## ポイントまとめ

- xrdpはVNCと違い、**現在の画面のミラーリングではなく新しいデスクトップセッションを作る**仕組み
- そのため**自動ログイン設定との衝突に注意**が必要
- 同じLAN内ならRaspberry Pi ConnectやRealVNCより快適に動くことが多い

## 関連情報（運用の使い分け）

| 状況 | おすすめの方法 |
|---|---|
| 同じLAN内にいる時 | xrdp + Windows App |
| 外出先からアクセスしたい時 | Raspberry Pi Connect |
| その他（互換性確認など） | RealVNC（そのまま残しておいてもOK） |


.  

🏰