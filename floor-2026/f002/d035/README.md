# Day 35（d035）

Raspberry Pi 5のメンテナンス。  
ヒートシンクの取り付け、microSDの入れ替えを行なった。

.  

🍓🥧

## 今回までの内容 LIST

- 対応ずみ
  - microSDカード
    - d033 → 32GB, Write 2MB/s, Verify 87MB/s
    - d035 → 128GB, Write 60MB/s, Verify 87MB/s
  - 無線LAN
    - DHCPクライアント
    - MACアドレスから固定IPの取得
    - VNCとRDPの比較
  - ログイン
    - ディスプレイなしでのOS起動
    - SSH：パスワード認証
  - 放熱処理
    - d033 → ドライヤーの冷風による放熱
    - d035 → 大型ヒートシンク、3cm FAN
- 未対応
  - 有線LAN
  - SSH：公開鍵認証
  - HAT：PoE, NVMe
  - HDMI：音を出す
  - 実行：Python, C lang, Kotlin
  - コンテナ：Docker, NATSサーバー, FastAPIサーバー
  - ブラウザ：JavaScript, webGPU, WebSocket

.  

🔑

# "SSH" ホスト鍵エラーの対処
***※以下は全てClaude AIによる説明です***

.  

## 状況
microSDをKioxia 128GBに入れ替えてOSを新規書き込みしたところ、SSH接続時に以下の警告が発生。

```
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@    WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!     @
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
Host key verification failed.
```

これはOSを新規に書き込むとSSHホスト鍵も新規生成されるため、同じIPでも「相手が変わった」とSSH側が警戒する**正常な挙動**。

.  

## 対処コマンド

古いホスト鍵をMac側の`known_hosts`から削除する。

```bash
ssh-keygen -R 192.168.179.40
```

```
# Host 192.168.179.40 found: line 7
# Host 192.168.179.40 found: line 8
# Host 192.168.179.40 found: line 9
/Users/neko/.ssh/known_hosts updated.
Original contents retained as /Users/neko/.ssh/known_hosts.old
```

再度SSH接続し、新しい鍵のfingerprintを確認して`yes`で承諾。

```bash
ssh neko@192.168.179.40
```

```
The authenticity of host '192.168.179.40 (192.168.179.40)' can't be established.
ED25519 key fingerprint is: SHA256:JnrhVm3MjJM/GUrsWgvc3PFibEIKugrqnfB6k1XPDHk
Are you sure you want to continue connecting (yes/no/[fingerprint])? yes
Warning: Permanently added '192.168.179.40' (ED25519) to the list of known hosts.
```

パスワード認証でログイン成功。OSは`Debian 1:6.18.34-1+rpt1`（Pi5用）。

.  

---

# "FAN" CPU温度とファン制御確認

.  

## 温度・クロック・スロットリング確認

```bash
vcgencmd measure_temp
vcgencmd measure_clock arm
vcgencmd get_throttled
```

```
temp=35.6'C
frequency(0)=1500016128
throttled=0x0
```

- 温度35〜36℃台はPi5にしてはかなり低め（良好）
- `throttled=0x0`はスロットリング（熱・電圧制限）が過去・現在ともに無いことを示す

.  

## FANの認識・手動制御確認

純正FANコネクタ（4pin, PWM対応）にFANを接続。`pwm-fan`として認識されていることを確認。

```bash
cat /sys/class/thermal/cooling_device0/type
cat /sys/class/thermal/cooling_device0/cur_state
cat /sys/class/thermal/cooling_device0/max_state
```

```
pwm-fan
0
4
```

`cur_state: 0`は「温度が低いため自動制御で停止中」、`max_state: 4`は0〜4の5段階で速度調整できることを示す。FANが回っていないのは故障ではなく、正常な温度判断によるもの。

手動でON/OFFをテスト。

```bash
echo 4 | sudo tee /sys/class/thermal/cooling_device0/cur_state
echo 0 | sudo tee /sys/class/thermal/cooling_device0/cur_state
```

`echo 4`でFANが回転、`echo 0`で停止することを確認。PWM制御が正常に機能している。

.  

## ポイントまとめ
- ホスト鍵警告は故障ではなく、OS再書き込み時の正常な挙動
- `ssh-keygen -R [IPアドレス]`で該当エントリだけを削除できる
- Pi5のFANは段階的なPWM制御（0〜4）で、温度に応じて自動的に回転数が変わる
- `cooling_device0`経由で手動テストも可能

.  

---

# "SD" microSD Verify速度が頭打ちになる理由

.  

## 現象
`Raspberry Pi Imager`での書き込み結果を比較すると、Write速度はカードごとに大きく違うのに、**Verify（検証読み出し）速度だけが2枚とも87MB/sで一致**した。

| カード | Write | Verify |
|---|---|---|
| 古い32GB | 2MB/s | 87MB/s |
| KIOXIA 128GB | 60MB/s以上 | 87MB/s |

.  

## 考察
使用しているカードリーダーは**ELECOM MR3-C004**（USB3.0対応スティック型）。USB3.0自体の理論上限はもっと高いため、一見「USB3.0なのに頭打ち」は不自然に見える。

しかし実際のボトルネックは**USBの規格ではなく、microSD自体のUHS-I規格の上限**だった。

- USB3.0の理論上限：5Gbps（数百MB/s）→ 全く余裕がある
- **UHS-Iの実用上限：90MB/s前後**（理論上限は104MB/s）→ これが頭打ちの正体
- MR3-C004はUHS-Iの上限近くまで性能を引き出せるリーダーであるため、87MB/sはほぼ規格通りの数値

つまり、古い32GBカードもKIOXIA 128GBカードも**両方UHS-I対応カード**であるため、読み出し（Verify）側はリーダーとカードの組み合わせで同じ上限に揃ってしまう。一方Write速度はカード自体のNAND劣化・性能差がそのまま表れるため、大きな差が出た。

.  

## ポイントまとめ
- Verify速度が同じになったのは、カードリーダーの故障や相性ではなく、UHS-I規格そのものの上限
- USB3.0は「最大何Mbps出せるか」の上限を示すだけで、実効速度はカード側の規格・リーダーのコントローラ性能で決まる
- さらに高速化したい場合はUHS-II対応カード＋UHS-II対応リーダーへの変更が必要（現状のカードはUHS-Iのため恩恵は薄い）

.  

🦆

---

.  

<div align=center>
<img width=400 src=img/d033-s01.png><BR>
<img width=400 src=img/d033-s02.png><BR>
<img width=350 src=img/d033-s03.png><BR>
<img width=350 src=img/d033-s04.png><BR>
</div>


.  

🏰