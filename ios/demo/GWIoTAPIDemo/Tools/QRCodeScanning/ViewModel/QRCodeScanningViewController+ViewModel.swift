//
//  ViewModel.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 10/8/2023.
//

import Foundation
import Vision
import GWIoTApi
import RQDeviceAddition
import RQCore
import SwiftUI

extension QRCodeScanningViewController.ViewModel {
    enum Event {
        // 成功识别到二维码
        case didSucceedRecognizeQRCodePayload(value: String?)
        // 捕获到视频数据, 用于AI分析
        case didCaptureVideoDataPixelBuffer(CVImageBuffer)
        // 对一张图片进行AI分析
        case analyzeImage(UIImage)
    }

    enum Status {
        case idle
        /// 二维码不支持
        case QRCodeNotSupported
        /// 识别图片中的二维码失败, 没有二维码
        case cannotRecognizeAnythingFromImage
        /// Vision 成功识别到二维码
        case didDetectedQRCode(results: [VNBarcodeObservation])
        /// 分析二维码结果失败
        case analyzeQRCodeFailure(error: Swift.Error)
        /// 绑定结果
        case bindResult(result: Result<IDevice, Swift.Error>)
        /// 成功识别设备分享二维码
        case didSucceedRecognizeShare(model: ShareQRCodeInfo)
        /// 识别到二维码是旧小豚当家的
        case didRecognizeOldXiaotunCode
    }

    /// 访客面对面扫码接受分享model
    struct ShareQRCodeInfo {
        /// 设备id
        var deviceID: String = ""
        /// 邀请码
        var inviteCode: String = ""
        /// 权限
        var permission: String = ""
        /// 过期时间
        var expireTime: Int = 0
        /// 产品id
        var productId: String = ""
        /// 产品名称
        var productName: String = ""
    }

}

extension QRCodeScanningViewController {

    enum QRCodeFrom {
        case album
        case camera
    }

    class ViewModel {

        var anyCancellables: Set<AnyCancellable> = []

        @DidSetPublished var status: Status = .idle

        // Vision AI识别视频二维码请求
        private lazy var videoDataDetectQRCodeRequest: VNDetectBarcodesRequest = .init(completionHandler: { [weak self] request, err in
            // 识别结果回调
            if let err = err {
                logError("[图像识别] AI识别二维码发生错误", err)
                return
            }
            guard let results = request.results as? [VNBarcodeObservation], results.count != 0 else { return }
            self?.status = .didDetectedQRCode(results: results)
            self?.qrCodeFrom = .camera
        }).then {
            $0.symbologies = [.qr]
        }

        // Vision AI识别图片二维码请求
        private lazy var imageDetectQRCodeRequest: VNDetectBarcodesRequest = .init(completionHandler: { [weak self] request, err in
            // 识别结果回调
            if let err = err {
                logError("[图像识别] AI识别二维码发生错误", err)
                return
            }
            guard let results = request.results as? [VNBarcodeObservation], results.count != 0 else {
                self?.status = .cannotRecognizeAnythingFromImage
                return
            }
            self?.status = .didDetectedQRCode(results: results)
            self?.qrCodeFrom = .album
        }).then {
            $0.symbologies = [.qr]
        }

        /// 用于记录二维码的来源, 埋点用, 非业务
        private(set) var qrCodeFrom: DeviceAdditionFlowItem.QRCodeFrom = .camera

        func processEvent(_ event: Event) {
            switch event {
            case .didSucceedRecognizeQRCodePayload(let value):
                self.handleQRCodePayload(value)
            case .didCaptureVideoDataPixelBuffer(let sampleBuffer):
                self.qrcodeAiAnalyze(sampleBuffer)
            case .analyzeImage(let img):
                self.qrcodeAiAnalyze(img)
            }
        }

        // MARK: Helper
        /// 分析和处理二维码内容
        func handleQRCodePayload(_ value: String?) {

            // 先判断是不是 SDK 调试用的私货
            if value == "https://reoqoo.com/d/?p=REOQOO_DEBUG&u=GO_TO_DEBUG_MODE" {
                let vm: DebugConfigurationViewModel = .init()
                let debugConfigurationViewController = UIHostingController.init(rootView: DebugConfigurationView(vm: vm))
                debugConfigurationViewController.modalPresentationStyle = .fullScreen
                AppEntranceManager.shared.tabbarViewController?.present(debugConfigurationViewController, animated: true)
                return
            }

            var result: Swift.Result<QRRecognizeResult, Swift.Error>
            // 识别二维码内容
            do {
                result = .success(try RQCore.QRRecognizer.recognize(value))
            } catch let err {
                result = .failure(err)
                self.status = .analyzeQRCodeFailure(error: err)
            }

            guard case let .success(recognize) = result else { return }
            switch recognize {
            // 配网
            case let .addDevice(factor_u, model):
                let opts = BindOptions.init()
                opts.qrCodeValue = value
                GWIoTApi.GWIoT.shared.openBind(opts: opts, completionHandler: { [weak self] result, err in
                    switch (gwiot_handleCb(result, err)) {
                    case .success(let dev):
                        if let deviceId = dev?.deviceId, let solution = dev?.solution {
                            self?.status = .bindResult(result: .success(GWDevice(solution: solution, deviceId: deviceId)))
                        } else {
                            self?.status = .bindResult(result: .failure(NSError(domain: "未知错误", code: 4)))
                        }
                    case .failure(let err):
                        self?.status = .bindResult(result: .failure(err))
                    }
                })
            // 分享
            case .acceptedDeviceShareInvite(deviceID: let deviceID, inviteCode: let inviteCode, permission: let permission, expireTime: let expireTime, productID: let productID, productName: let productName):
                var model = ShareQRCodeInfo()
                model.productId = productID
                model.deviceID = deviceID
                model.inviteCode = inviteCode
                model.permission = permission
                model.expireTime = Int(expireTime)
                model.productName = productName
                self.status = .didSucceedRecognizeShare(model: model)
            @unknown default:
                break
            }
        }

        // MARK: Vision framework 分析
        /// 分析视频数据流
        func qrcodeAiAnalyze(_ sampleBuffer: CVPixelBuffer) {
            let imageRequestHandler = VNImageRequestHandler.init(cvPixelBuffer: sampleBuffer)
            do {
                try imageRequestHandler.perform([self.videoDataDetectQRCodeRequest])
            }catch let err {
                logInfo("[图像识别] AI识别视频中的二维码失败了", err)
            }
        }

        /// 分析一张图片
        func qrcodeAiAnalyze(_ image: UIImage) {
            guard let cgImage = image.cgImage else { return }
            let imageRequestHandler = VNImageRequestHandler.init(cgImage: cgImage)
            do {
                try imageRequestHandler.perform([self.imageDetectQRCodeRequest])
            }catch let err {
                logInfo("[图像识别] AI识别图片中的二维码失败了", err)
            }
        }
    }
}
