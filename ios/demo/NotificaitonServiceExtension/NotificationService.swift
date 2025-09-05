//
//  NotificationService.swift
//  NotificaitonServiceExtension
//
//  Created by xiaojuntao on 25/8/2025.
//

import UserNotifications

class NotificationService: UNNotificationServiceExtension {
    var contentHandler: ((UNNotificationContent) -> Void)?
    var bestAttemptContent: UNMutableNotificationContent?

    override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
        self.contentHandler = contentHandler
        bestAttemptContent = (request.content.mutableCopy() as? UNMutableNotificationContent)
        print("didReceive contentHandler")

        // 获取图片的链接地址
        if let imgUrl = bestAttemptContent?.userInfo["media-url"] as? String,
           !imgUrl.isEmpty,
           let fileURL = URL(string: imgUrl) {
            /// 下载图片并临时保存到本地
            downloadAndSave(fileURL: fileURL) { localPath in
                if let localPath = localPath,
                   let attachment = try? UNNotificationAttachment(identifier: "myAttachment", url: URL(fileURLWithPath: localPath), options: nil) {
                    self.bestAttemptContent?.attachments = [attachment]
                }
                self.contentHandler?(self.bestAttemptContent ?? request.content)
            }
        } else {
            contentHandler(bestAttemptContent ?? request.content)
        }
    }

    /// 下载并保存图片的方法
    func downloadAndSave(fileURL: URL, handler: @escaping ((String?) -> Void)) {
        let session = URLSession.shared
        let task = session.downloadTask(with: fileURL) { location, _, error in
            var localPath: String? = nil
            if error == nil, let location = location {
                // Temporary directory path, which will clear the image automatically when the app is not running
                let localURL = NSTemporaryDirectory().appending("/\(fileURL.lastPathComponent)")
                do {
                    try FileManager.default.moveItem(atPath: location.path, toPath: localURL)
                    localPath = localURL
                } catch {
                    print("Error moving file: \(error)")
                }
            }
            handler(localPath)
        }
        task.resume()
    }

    override func serviceExtensionTimeWillExpire() {
        if let contentHandler = contentHandler, let bestAttemptContent = bestAttemptContent {
            contentHandler(bestAttemptContent)
        }
    }
}
