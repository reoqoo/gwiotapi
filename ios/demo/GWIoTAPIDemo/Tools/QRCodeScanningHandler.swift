//
//  QRCodeScanningHandler.swift
//  Reoqoo
//
//  Created by xiaojuntao on 18/7/2025.
//

import Foundation
import GWIoTApi
import RQCoreUI

class QRCodeScanningHandler {
    static let shared: QRCodeScanningHandler = .init()

    private init() {
        // 监听 bind 结果
        GWPlugin.shared.bindEvents.observe(weakRef: self) { event in
            guard let event = event else { return }
            if let event = event as? BindEvent.BindCancelled {
                
            }
            if let event = event as? BindEvent.BindFailed {
                
            }
            if let event = event as? BindEvent.BindSuccess {
                // 点击 "开始使用" 后, 会触发这里
            }
        }
    }

    func openScanningWithTitle(_ title: String, description: String) async throws {
        try await GWIoT.shared.openScanQRCodePage(opts: .init(), recognized: nil)
        return
        
        // 自定义处理二维码结果 参考代码
        let opts = ScanQRCodeOptions(enableBuiltInHandling: false, title: title, descTitle: description)
        try await GWIoT.shared.openScanQRCodePage(opts: opts) { qrCodeType, closeHandler in
            // 配网
            if let qrCodeType = qrCodeType as? QRCodeType.BindDevice {
                Task {
                    try await GWIoT.shared.openBind(qrCodeValue: qrCodeType.qrCodeValue)
                    let _ = closeHandler() // 关闭扫码页面
                }
                return
            }

            // 分享
            if let qrCodeType = qrCodeType as? QRCodeType.ShareDevice {
                Task {
                    let _ = closeHandler()
                    try await GWIoT.shared.acceptShareDevice(qrCode: qrCodeType)
                }
                return
            }

            DispatchQueue.main.async {
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0478", note: "不支持的二维码"))
            }
        }
    }
}
