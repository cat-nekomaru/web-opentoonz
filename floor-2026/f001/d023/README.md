# Day 23（d023）
.  

コーディングをしますか？（はい／[いいえ](../)）

. 

💻 

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

.  

🪎