# macOS Thumbnail Extension 実装手順（.tlw2対応）作成日: 2026-05-23

**目的:** `.tlw2`ファイルのFinderアイコンにPreview Blocksの画像を表示する

## `.tlw2` ファイル構造（重要）

```
Header           1024バイト固定（ASCII/UTF-8）
Preview Blocks   512バイトアライメント　← Thumbnail Extensionがここを主に読む
Index Table      512バイトアライメント（UTF-8 JSON）
PNG Data Blocks  512バイトアライメント
Footer           任意Headerの先頭例（1024バイト固定）
TZW2_FORMAT v0.2
version=0.2
width=500
height=500
previewCount=1
indexOffset=1024
```

（1024バイトまで0x00でパディング）Preview Blocksには代表フレームの合成画像（PNG）を先頭に入れるのが推奨。XcodeでThumbnail Extensionを作成する手順

## 手順1: プロジェクト作成Xcodeを起動
macOS App を新規作成（仮のAppでOK）  
プロジェクト作成後、
- 左側のプロジェクト名を右クリック → Add Target
- Thumbnail Extension を検索して追加
- Extension名例: WebOpenToonzThumbnailExtension

## 手順2: .tlw2 の UTI 登録（メインターゲット側）左側でメインターゲットを選択
Info タブを開く  
`Exported Type Identifiers` の + をクリック
以下を入力：
- Description: TLW2 File Format
- Identifier: com.cat-nekomaru.tlw2
- Conforms To: public.data
- Extensions: tlw2

## 手順3: Extension側の設定左側で WebOpenToonzThumbnailExtension を選択
Info タブを開く  
NSExtension → NSExtensionAttributes を展開  
NSExtensionAttributes で + をクリック
```
Key: QLSupportedContentTypes
Type: Array
配列の中に com.cat-nekomaru.tlw2 を追加
```

## 手順4: ThumbnailProvider.swift の実装ThumbnailProvider.swift を開く
内容をすべて以下に置き換える：

```
import QuickLookThumbnailing
import AppKitclass ThumbnailProvider: QLThumbnailProvider {override func provideThumbnail(for request: QLFileThumbnailRequest, _ handler: @escaping (QLThumbnailReply?, Error?) -> Void) {
    
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
}}
```

## テスト方法
Xcodeで Run を実行（Extensionをインストール）  
ターミナルで以下を実行：

```
qlmanage -r
qlmanage -r cache
killall Finder
```
Finderで .tlw2 ファイルを確認

🐾
