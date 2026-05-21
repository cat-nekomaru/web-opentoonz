# 🇺🇸Web-OpenToonz
Web-based OpenToonz compatible drawing app (HTML5 Canvas) - Mobile First, Portrait Default

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

Demo:  
1. https://cat-nekomaru.github.io/web-opentoonz/
2. https://cat-nekomaru.github.io/web-opentoonz/index2.html

UI Layout - 11inch Tablet :  
<img width="90%" alt="Image" src="https://github.com/user-attachments/assets/b2aa4752-241a-4233-ba8f-fd7d2118fac5" />

<img alt="Image" src="https://github.com/user-attachments/assets/3f2e5e60-201a-4982-9564-acac6170fbe4" />

<table border="1" style="color: #fff;">
    <tr align="center" bgcolor=#404060>
        <td>.　　A, B　　.<BR>button</td>
        <td colspan="2">H<BR>text</td>
        <td>.　C, D, E, F, G　.<BR>button</td>
    </tr>
    <tr align="center" valign="center" bgcolor=#404050>
        <td colspan="4">
            <br><br><br><u>Interface ID:</u> I<br><u>Interface Type:</u> canvas<br><br><br><br>
        </td>
    </tr>
    <tr align="center">
        <td rowspan="2" bgcolor=#404040>J, K<br>widget<br>(toggle)</td>
        <td bgcolor=#504040>O<br>button</td><td bgcolor=#504040>.　Q, R, S, T, U　.<br>button</td>
        <td rowspan="2" bgcolor=#404040>L, M, N<br>widget<br>(toggle)</td>
    </tr>
    <tr align="center" bgcolor=#605050>
        <td>P<br>button</td><td>V, W, X, Y, Z<br>button</td>
    </tr>
</table>

UI Layout - 8inch Tablet :  
<img width="350" height="560" alt="Image" src="https://github.com/user-attachments/assets/eca5415e-02a5-4db3-a535-f10b7c32723f" />

<table border="1" style="color: #fff;">
    <tr align="center" bgcolor=#404060>
        <td>.　　A, B　　.<BR>button</td>
        <td>.　　H　　.<BR>text</td>
        <td colspan="2">.　　C, D, E, F, G　　.<BR>button</td>
    </tr>
    <tr align="center" valign="center" bgcolor=#404050>
        <td colspan="4">
            <br><br><br><u>Interface ID:</u> I<br><u>Interface Type:</u> canvas<br><br><br><br>
        </td>
    </tr>
    <tr align="center">
        <td rowspan="2" bgcolor=#404040>J, K<br>widget<br>(toggle)</td>
        <td bgcolor=#504040>O<br>button</td><td bgcolor=#504040>Q, R, S, T, U<br>button</td>
    </tr>
    <tr align="center" bgcolor=#605050>
        <td>P<br>button</td><td>V, W, X, Y, Z<br>button</td>
    </tr>
</table>

UI Layout - Smartphone :  
<img width="55%" alt="Image" src="https://github.com/user-attachments/assets/24c27b4b-f356-4646-9f86-1fb202f2d423" />

<table border="1" style="color: #fff;">
    <tr align="center" bgcolor=#404070>
        <td>C, D, E, F, G<BR>button</td>
    </tr>
    <tr align="center" valign="center" bgcolor=#404050>
        <td><br><br><u>Interface ID:</u> I<br>.　　<u>Interface Type:</u> canvas　　.<br><br><br></td>
    </tr>
    <tr align="center" bgcolor=#50404>
        <td>Q, R, S, T, U<br>button</td>
    </tr>
</table>

🐾