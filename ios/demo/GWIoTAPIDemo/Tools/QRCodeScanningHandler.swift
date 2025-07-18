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

    private init() {}

    func openScanningWithTitle(_ title: String, description: String) async throws {
        let opts = ScanQRCodeOptions(enableBuiltInHandling: true, title: title, description: description)
        try await GWIoT.shared.openScanQRCodePage(opts: opts) { qrCodeType in
            // 处理其他二维码
            if qrCodeType.qrCodeValue.contains("smarthome.hicloud.com") {
                DispatchQueue.main.async {
                    let alertContent = String.localization.localized("AA0659", note: "当前版本暂不支持直接绑定该设备，请下载\"小豚云\"APP添加使用。")
                    let alert = RQCoreUI.AlertViewController.init(title: nil, content: .string(alertContent), actions: [
                        .init(title: String.localization.localized("AA0131", note: "知道了"), style: .default, handler: { _, alert in
                            alert.dismiss(animated: true)
                        }),
                        .init(title: String.localization.localized("AA0660", note: "去下载"), style: .default, handler: { _, alert in
                            alert.dismiss(animated: true)
                            let url: URL = .init(string: "dophigo://")!
                            if UIApplication.shared.canOpenURL(url) {
                                UIApplication.shared.open(url)
                            }else{
                                UIApplication.shared.open(.init(string: "https://apps.apple.com/app/1226835767")!)
                            }
                        })
                    ])
                    UIApplication.rootViewController()?.present(alert, animated: true)
                }
            }

            // 配网
            if let qrCodeType = qrCodeType as? QRCodeType.BindDevice {
                Task {
                    try await GWIoT.shared.openBind(opts: .init(qrCodeValue: qrCodeType.qrCodeValue))
                }
                return
            }

            // 分享
            if let qrCodeType = qrCodeType as? QRCodeType.ShareDevice {
                Task {
                    try await GWIoT.shared.acceptShareDevice(qrCode: .init(deviceId: qrCodeType.deviceId, inviteCode: qrCodeType.inviteCode, permission: qrCodeType.permission, expireTime: qrCodeType.expireTime, pid: qrCodeType.pid, qrCodeValue: qrCodeType.qrCodeValue))
                }
                return
            }

            DispatchQueue.main.async {
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0478", note: "不支持的二维码"))
            }
        }
    }
}
