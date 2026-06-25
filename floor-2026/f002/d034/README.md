# Day 34（d034）

***※以下は全てClaude AIによる説明です***

[Apple Container](https://github.com/apple/container) `1.0.0` を使って、Alpine LinuxのVM（コンテナ）を試しました。

.  

🐳📦

## 今回の内容 LIST
- 対応ずみ
  - `container machine`（残るタイプ）の作成・起動・削除
  - `container run`（消えるタイプ）の起動
  - CPU・メモリの設定変更
  - Alpine 3.22 / 3.24 の動作確認（3.23は未確認）
  - MACアドレスの固定指定（概要のみ確認、実機検証はせず）
  - Mac/Linuxの見分け方（`uname -a`）
  - Alpine内でのPython動作確認（ephemeral containerでは成功）
- 未対応
  - IPアドレスの固定指定（現バージョン未実装）
  - `container build`（自分のイメージを作る）→ buildkitが詰まって失敗
  - `container machine`内での`sudo`/`su`（権限エラーで断念）
  - Docker Compose相当の機能

.

# "Apple Container" 基本コマンドまとめ

.  

## 2つのコンテナタイプ

| | ephemeral container（消えるタイプ） | container machine（残るタイプ） |
|---|---|---|
| 作成 | `container run` | `container machine create` |
| 起動 | `run`（`--rm`で終了後自動削除） | `run`（`start`はない） |
| 性質 | アプリ実行用、使い捨て前提 | Linux環境そのもの、常駐前提 |
| ファイル | 終了したら基本消える（`--rm`時） | ディスクに保持され続ける |
| メモリ下限 | 200MB以上（199MBはNG） | 1GB以上必須 |
| ホームディレクトリ | 自動共有なし（`--volume`で個別指定） | デフォルトで自動マウント |

## よく使ったコマンド

```bash
# サービス起動（最初の1回）
container system start

# --- ephemeral container（使い捨て） ---
container run --rm -it alpine sh
container run --memory 200m --rm -it alpine sh

# --- container machine（常駐） ---
container machine create alpine:3.22 --name my-machine
container machine run -n my-machine
container machine set -n my-machine cpus=2 memory=1g
container machine stop my-machine
container machine rm my-machine

# 一覧・状態確認
container image ls
container machine ls
container machine inspect my-machine
```

## ハマったポイント

- **メモリの下限**：
  - `container machine`は1GB未満を指定するとエラー。
    ```
    Error: invalid memory value '512mb'. Must be greater than 1gb.
    ```
  - `container run`（ephemeral）にも下限があり、**200MBはOK、199MBはNG**だった。無制限ではない。

- **IPアドレスは固定できない**：`--network`でMACアドレスは指定できるが、IPはmacOS側のDHCP（vmnet）が自動割当。`--ip`相当の機能はまだ存在せず、GitHub上でも要望（Issue）止まり。

- **Alpineの`sudo`/`su`が機能しない**：`container machine`内で`apk add`しようとすると権限エラー。`sudo`は未インストール、`su`は`must be suid to work properly`で失敗。Alpineと`container machine`の組み合わせ特有の不具合と思われる（Ubuntuなど別ディストリでは発生しない可能性）。

- **`container build`がbuildkitで詰まる**：Dockerfileから自分のイメージを作ろうとすると、`[resolver] fetching image...`の表示のまま数分単位で停止。`container image pull`単体は正常に進むため、ネットワークではなくbuildkit（ビルダー）側の不具合と判断。Alpine 3.22 / 3.24 どちらでも同症状。`builder stop/delete/start`や`system stop/start`でも解消せず、現バージョンの既知の不具合の可能性。

## 区別の確認方法

```sh
uname -a
```
- `Darwin ...` → Mac側のターミナル
- `Linux ...`  → コンテナ／machineの中

ユーザー名・ホームディレクトリ・作業ディレクトリがMacと地続きに見えるため、見た目だけでは判別しづらい。これで一発確認できる。

## 今後（次に試したいこと）

- IPアドレス固定機能の実装を待つ
- `container build`の不具合がバージョンアップで直るか確認
- Alpine以外（Ubuntuなど）の`container machine`での`sudo`挙動を比較

.  

🍓➡️🐳（Raspberry Piの次はコンテナへ）