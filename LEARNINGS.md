# LEARNINGS & DEVLOG | 2026

ソフトウェア開発の入門者である私のPDCA記録です。  
ここでの取り組みが、開発時の参考になることを想定しています。

🦉

## Memo | Recent Ideas & TODO
- README.mdを人間が書いたものに差し替える。不自然なことが問題
- 本家OpenToonzのQtフレームワークに近いUIを狙う。学習コストの低減
- 「SSH認証」の知識になるものを探す
- `Acode editor` for AndroidのGitHubの環境構築

🕊️

## 2026-5-20 | Day 3

- Today's Achievements
  - Androidタブレット環境
  - GitHubの使い方
  - GUI画面の仕組み
  - VScode／Markdown機能拡張のメンテ

- Key Learnings
  - テキストエディタとして利用しているAndroid版 `Acode editor`、GitHubを使う仕組みがあることに気づいたので軽く調べる。実際のテストは気が乗らないので見送り
  - GitHubのコミットメッセージの書き方を予習する。https://note.com/neco_marui/n/n03a3ff153a93 @blog ➠ ソフトウェア開発そのものはAIによって塗り替えられるだろうけど、こういうのは大事
  - web-toonzのGUIに、漏れなくAからZのIDを割り振ることを思いつく。その反面、ボタンを増やすなど拡張性は考えないことにする。プロトタイプの進行を最優先
  - VScode機能拡張 `Markdown All in One` の動作不調。以前は機能していたList editingがNG。原因特定できないため断念（ヒント：keybindings.json, setings.json）。混乱を避けるためアンインストールしておいた
  - KBDタグの存在をAIから教わる。これカッコイイね！<kbd>That's right.</kbd>

- Reflections
  - このプロジェクトのスタートと最初のゴールを設定することに成功したので安心している所。それにしても、javaScriptの実用性がこれほどとは。小生も驚きを禁じ得ず。🙀
  - 一人で開発している割にAIからの支援も必要とするため、設計の共通認識が必要な状況。回避策としては、①自分が使っている開発環境に最適化したものを作る ②設計の増築は避ける ③画面推移など、機能のネストをしない ⇒ IDEの支援がないのでweb版はMSペイント程度を目指す（優先事項：ファイルの互換性の実証）

. . . **Score**: 2/5  
. . . **Update**: 2026-5-20  


## 2026-5-19 | Day 2

- Today's Achievements
  - GitHubのCLI操作（APIキー取得, git add/commit/push）
  - LEARNINGS.mdを手作業で作る
  - Live Server機能拡張をVScodeに入れてテスト
  - 3.2K画面のレイアウト https://x.com/YojiNishimura/status/2056556472759386511 @sns
  - 訳あって、リポジトリの再作成を行なった
  - PCとリモートでGitが動作NG。Google AIでは解決せず、Grokのアドバイスにて華麗に復旧した

- Key Learnings
  - VScode & Live Serverの組み合わせはLAN内でhtml検証にとても便利（例： http://192.168.xx.xx:5050/ @lan で検証デバイスからPCのVScodeにアクセス可能）
  - Gitの動作NGは、公開鍵の未登録が原因。知識がなく理解は及ばず。Grokは優秀
  - 設定ミスにてリポジトリを再作成。キツい洗礼。仕事じゃなくて良かったDeath
  - VScodeで.mdのプレビューは"arkdown All in One"機能拡張。command+k vで起動
  - SSH認証の仕組みに無知でトラブル解決できず。ほんと心底イヤになった

- Reflections
  - Gitコマンドはターミナル上でも難しくない。でもVScodeからも可能なはず
  - Markdownにログを残しておけば、全方位に有益なことを察してしまった。これこそソフトウェア開発の基礎ではないだろうか
  - Grokがトラブルシューティングにむちゃ強なので見直した。Claude Code、OpenAI Codexの評判は目にするものの、トラブルの最中には試す気にはなれないのは人のサガだね
  - 16inchディスプレイでは画面領域が足りない気がする。3072x1920pxなので性能的には合格。購入を検討していたNeoは2560x1440なのでコーディングには辛いのかも（横長のほうが便利だね）

. . . **Score**: 3/5  
. . . **Update**: 2026-5-19  

## 2026-5-18 | Day 1

- Today's Achievements
  - GitHubアカウント作成
  - README.md, index.html, index2.htmlをGrokが全部作る
  - AIにOpenToonzのリポジトリを調べてもらう

- Key Learnings
  - VScode & Live Serverの組み合わせが有用とのこと
  - GitHubに公開しておけばAIが自由にアクセスできるらしい。すごいね！

- Reflections
  - 不本意ながらもGrok AIによるノーコード開発に頼ることでサンプルコードを作成した（いずれ手作り品に入れ替えたい）。でもJavaScript／Canvasのコード量の少なさに衝撃を受けてしまった。これが21世紀のテクノロジーなのか。全てが、まぶしい（？）。

. . . **Score**: 5/5  
. . . **Update**: 2026-5-19

🐾
---

# temp

## 2026-5-＊ | Day ⭐️

- Today's Achievements
  - ＊

- Key Learnings
  - ＊

- Reflections
  - ＊

. . . **Score**: ⭐️/5  
. . . **Update**: 2026-5-⭐️  
