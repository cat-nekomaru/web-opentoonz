## macOS 26.5 - Thumbnail Extension 実装メモ

**作成日:** 2026-5-23  
**環境:** macOS 26.5, Xcode 26.5  
**目的:** `.tlw2`形式ファイルの`Preview Blocks`領域に埋め込んだサムネイル画像を、Finderアイコンに表示させる  
**展望：** 将来的には`QuickLook Preview Extension`にも対応し、Spaceキーを押した際にアニメーションを再生できるようにしたいと考えています。これにより、Finder上での確認作業が大幅に快適になるはずです

<div align="center">
<img width="350" alt="Image" src="https://github.com/user-attachments/assets/956407ad-cda1-432d-bae8-52a28e1d730b" />
</div>  
<img width="340" height="184" alt="Image" src="https://github.com/user-attachments/assets/c259fff8-437e-49ee-b20e-8fd83d8f8d24" />

---

## `.tlw2` ファイル構造（重要）

```sh
Header          # 1024バイト固定（ASCII/UTF-8）
Preview Blocks  # 512バイトアライメント　← Thumbnail Extensionがここを主に読む
Index Table     # 512バイトアライメント（UTF-8 JSON）
PNG Data Blocks # 512バイトアライメント
Footer          

# 任意Headerの先頭例（1024バイト固定）
TZW2_FORMAT v0.2
version=0.2
width=500
height=500
previewCount=1
indexOffset=1024
```

1024バイトまで0x00でパディング。Preview Blocksには代表フレームの合成画像（PNG）を先頭に入れることを推奨    

---

# Xcode 26.5 - Thumbnail Extensionを作成する

## 手順1: Xcodeのプロジェクト作成

Xcodeを起動する  
macOS App を新規作成（仮のAppでOK）  
プロジェクト作成後、
- 左側のプロジェクト名を選択 ＞ Editorメニュー ＞ <kbd>Add Target...</kbd>
- Thumbnail Extension を検索して追加
- Extension名例: WebOpenToonzThumbnailExtension

<img width="1032" height="868" alt="Image" src="https://github.com/user-attachments/assets/6972e8e1-a85d-4ef7-b619-949492a278d7" />  

（下部にある<kbd>+</kbd>をクリックでもOK ⬆︎）  
（Editorから開くやり方はこれ ⬇︎）  

<img width="489" height="388" alt="Image" src="https://github.com/user-attachments/assets/3f721165-e400-4999-8d77-cafa8317c0f1" />


## 手順2: `.tlw2`のUTIを登録

左側でメインターゲットを選択  
Info タブを開く  
`Exported Type Identifiers` の <kbd>+</kbd> をクリック  
以下を入力：
- Description: TLW2 File Format
- Identifier: com.cat-nekomaru.tlw2
- Conforms To: public.data
- Extensions: tlw2

<img width="1101" height="632" alt="Image" src="https://github.com/user-attachments/assets/2c99a38e-313e-403d-bb68-f5b3cb364a03" />

## 手順3: Extension側の設定

- 左側で WebOpenToonzThumbnailExtension を選択
- Info タブを開く  
- NSExtension → NSExtensionAttributes を展開  
- NSExtensionAttributes で <kbd>+</kbd> をクリック
```
Key: QLSupportedContentTypes
Type: Array
配列の中に com.cat-nekomaru.tlw2 を追加
```
<img width="1172" height="539" alt="Image" src="https://github.com/user-attachments/assets/d6b55c86-b985-42bd-8931-f2a59a60fa48" />

## 手順4: ThumbnailProvider.swift

ThumbnailProvider.swift を開いて、  
内容をすべて以下に置き換える：

```swift
import QuickLookThumbnailing
import AppKit

class ThumbnailProvider: QLThumbnailProvider {
    
    override func provideThumbnail(for request: QLFileThumbnailRequest, _ handler: @escaping (QLThumbnailReply?, Error?) -> Void) {
        
        do {
            let fileData = try Data(contentsOf: request.fileURL)
            let pngSignature = Data([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A])
            
            if let range = fileData.range(of: pngSignature, in: 1024..<fileData.count) {
                let start = range.lowerBound
                let length = min(150000, fileData.count - start)
                
                let pngData = fileData.subdata(in: start..<start + length)
                
                if let image = NSImage(data: pngData) {
                    let reply = QLThumbnailReply(contextSize: request.maximumSize) { () -> Bool in
                        image.draw(in: CGRect(origin: .zero, size: request.maximumSize))
                        return true
                    }
                    handler(reply, nil)
                    return
                }
            }
        } catch {
            print("TLW2 Thumbnail Error: \(error)")
        }
        
        handler(nil, nil)
    }
}
```
<img width="1333" height="975" alt="Image" src="https://github.com/user-attachments/assets/d8e3c240-1f91-4898-89b0-731a969c47dc" />

## テスト方法

Xcodeでビルドを実行 <kbd>⌘ + B</kbd>（Extensionをインストール）  
ターミナルで以下を実行：

```sh
qlmanage -r
qlmanage -r cache
killall Finder
```
Finderで `.tlw2` ファイルを確認すれば完了

<div align="center">
<img width="380" alt="Image" src="https://github.com/user-attachments/assets/83587cdb-d3bf-441c-b2af-eb4dbb7e1645" />
</div>

---

# Thumbnail Extensionを消す～アイコンを登録する

**作成日:** 2026-5-25  
**環境:** macOS 26.5, Xcode 26.5  
**目的:** Extensionは登録できたもののアイコンが登録されておらず白いまま。アイコンを登録してみたくなったので、一旦消してから再インストールしました  

## 手順1: ターミナルでコマンドをたたく

起動中のExtensionを停止させてからファイルを削除をするので、  
ターミナルで以下を実行：  

```sh
# pluginkit → 存在しているかをchk（web-opentoonzの場合）
pluginkit -vmA | grep -i webopentoonz

# pluginkit → 停止させる（nekomaruの場合）
pluginkit -e ignore -i nekomaru.WebOpenToonzThumbnail.WebOpenToonzThumbnailExtension

# フォルダごと削除する（nekoの場合）
rm -rf "/Users/neko/Library/Developer/Xcode/DerivedData/WebOpenToonzThumbnail-*"
```

<img width="680" height="290" alt="Image" src="https://github.com/user-attachments/assets/dfee5103-806a-4e0b-a383-b3e3ecd307ab" />  

最後に、FinderをリセットすればOK  

```sh
qlmanage -r
qlmanage -r cache
killall Finder
```

## 手順2: Xcodeにアイコンを登録してからビルド

Xcodeを起動する  
プロジェクトを読み込んだら、
- 左側のプロジェクト名 ＞ Assets.xcassets ＞ AppIcon を選択
- 右側のエリアに1024x1024pxの画像を配置する

<img width="1100" height="707" alt="Image" src="https://github.com/user-attachments/assets/1ab5f5d5-3b9d-4842-987a-a8ef543ae091" />  

Xcodeでビルドを実行 <kbd>⌘ + B</kbd>すれば、自動でインストールされます（10秒くらい待つ）  
これで完了です。  

🐾
---

**2026-05-25** | version: 0.2
- Extensionの削除方法を追加
- アイコンの登録方法を追加

**2026-05-23** | version: 0.1
- 初版作成
