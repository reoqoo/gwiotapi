//
//  URL+.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 7/8/2023.
//

import Foundation

extension URL {

    // APP设置
    static let appSetting: URL = URL.init(string: UIApplication.openSettingsURLString)!

    /// 反馈功能上传图片压缩路径
    static let feedbackImagesZipDestination: URL = {
        let tmp = NSTemporaryDirectory()
        let tmpDirURL = URL.init(fileURLWithPath: tmp)
        let feedbackImages = "feedbackImages.zip"
        let zipFilePath = tmpDirURL.appendingPathComponent(feedbackImages)
        return zipFilePath
    }()

    // AppStore 地址
    static let AppStoreURL: URL = .init(string: "https://apps.apple.com/app/id6466230911")!

}
