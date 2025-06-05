//
//  DevicesAdditionQRCodeScanningViewController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 7/8/2023.
//

import UIKit
import RQImagePicker
import Photos
import Vision

import GWIoTApi
import RQCore
import RQDeviceShared
import RQDeviceAddition

extension QRCodeScanningViewController {

    /// 扫码目的描述
    enum Objective {
        case addDevice
        case justScanning

        var title: String {
            switch self {
            case .addDevice:
                return String.localization.localized("AA0049", note: "添加设备")
            case .justScanning:
                return String.localization.localized("AA0051", note: "扫一扫")
            }
        }

        var description: String {
            switch self {
            case .addDevice:
                return String.localization.localized("AA0062", note: "扫描设备机身二维码添加设备")
            case .justScanning:
                return String.localization.localized("AA0176", note: "请扫描设备上的二维码或他人分享的二维码")
            }
        }
    }
}

class QRCodeScanningViewController: BaseViewController {

    let vm: ViewModel = .init()

    let objective: Objective
    init(for objective: Objective) {
        self.objective = objective
        super.init(nibName: nil, bundle: nil)
    }

    private override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) { fatalError("init(coder:) has not been implemented") }

    private init() { fatalError("init(coder:) has not been implemented") }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    let noLimitsOfCameraAuthorityDescriptionAttributedString: NSAttributedString = {
        let content = String.localization.localized("AA0068", note: "没有相机权限，请在设置中开启")
        let rangeOfSetting = (content as NSString).range(of: String.localization.localized("AA0103", note: "设置"))
        let res = NSMutableAttributedString.init(string: content)

        /// 统一添加样式
        res.addAttributes([.foregroundColor: R.color.text_FFFFFF()!, .font: UIFont.systemFont(ofSize: 14)], range: .init(location: 0, length: content.count))

        // 链接部分
        // 设置
        res.addAttributes([.foregroundColor: R.color.text_link_4A68A6()!, .link: URL.systemSetting], range: rangeOfSetting)

        return res
    }()

    lazy var cameraOutputView: CameraOutputView = .init().then {
        // 设置自动对焦区域 (取值范围为 (0, 0) ~ (1, 1))
        $0.autoFocusPoint = .custom(.init(x: 0.5, y: 0.4))
    }

    // 扫描区域 size
    lazy var scanningFrameSize: CGSize = .init().with {
        if UIDevice.current.userInterfaceIdiom == .pad {
            $0 = .init(width: 220, height: 220)
        }else{
            $0 = .init(width: UIScreen.main.bounds.width - 120, height: UIScreen.main.bounds.width - 120)
        }
    }

    // 挖孔背景
    lazy var transparentBackground: ShapeLayerView = .init().then {
        $0.isUserInteractionEnabled = false
    }

    // 扫描框
    lazy var qrCodeScanningFrameContainer: UIImageView = .init(image: R.image.scanningFrame()).then {
        $0.clipsToBounds = true
        $0.contentMode = .scaleAspectFill
    }

    // 扫描光
    lazy var qrCodeScanningLine: UIImageView = .init(image: R.image.scanningLine())

    lazy var flashlightBtn: IVButton = .init(.top, space: 8).then {
        $0.setTitle(String.localization.localized("AA0063", note: "手电筒"), for: .normal)
        $0.titleLabel?.font = .systemFont(ofSize: 14)
        $0.setTitleColor(R.color.text_FFFFFF()!.withAlphaComponent(0.9), for: .normal)
        $0.setImage(R.image.scanningFlashlightOff(), for: .normal)
        $0.setImage(R.image.scanningFlashlightOn(), for: .selected)
    }

    lazy var albumBtn: IVButton = .init(.top, space: 8).then {
        $0.setTitle(String.localization.localized("AA0064", note: "相册"), for: .normal)
        $0.titleLabel?.font = .systemFont(ofSize: 14)
        $0.setTitleColor(R.color.text_FFFFFF()!.withAlphaComponent(0.9), for: .normal)
        $0.setImage(R.image.scanningAlbum(), for: .normal)
    }

    lazy var descriptionTextView: UITextView = .init().then {
        $0.isEditable = false
        $0.isScrollEnabled = false
        $0.backgroundColor = .clear
        $0.textColor = R.color.text_FFFFFF()!
        $0.font = .systemFont(ofSize: 14)
        $0.text = self.objective.description
        $0.textAlignment = .center
    }

    private var anyCancellables: Set<AnyCancellable> = []

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        self.cameraOutputView.startCapture()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        self.startScanningAnimation()
    }

    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        self.cameraOutputView.stopCapture()
        self.stopScanningAnimation()
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        self.transparentBackgroundHollowOut(self.qrCodeScanningFrameContainer.frame)
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = self.objective.title
        self.setNavigationBarBackground(.clear, tintColor: R.color.text_FFFFFF()!)

        self.view.backgroundColor = .black

        self.view.addSubview(self.cameraOutputView)
        self.cameraOutputView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.view.addSubview(self.transparentBackground)
        self.transparentBackground.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.view.addSubview(self.qrCodeScanningFrameContainer)
        self.qrCodeScanningFrameContainer.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(74)
            make.centerX.equalToSuperview()
            make.size.equalTo(self.scanningFrameSize)
        }

        self.qrCodeScanningFrameContainer.addSubview(self.qrCodeScanningLine)
        self.qrCodeScanningLine.snp.makeConstraints { make in
            make.bottom.equalTo(self.qrCodeScanningFrameContainer.snp.top).offset(0)
            make.leading.trailing.equalToSuperview()
        }

        self.view.addSubview(self.descriptionTextView)
        self.descriptionTextView.snp.makeConstraints { make in
            make.top.equalTo(self.qrCodeScanningFrameContainer.snp.bottom).offset(20)
            make.centerX.equalToSuperview()
            make.leading.greaterThanOrEqualToSuperview().offset(16)
            make.trailing.lessThanOrEqualToSuperview().offset(-16)
        }

        let buttonStackView: UIStackView = .init(arrangedSubviews: [self.flashlightBtn, self.albumBtn])
        buttonStackView.axis = .horizontal
        buttonStackView.spacing = 123
        self.view.addSubview(buttonStackView)
        buttonStackView.snp.makeConstraints { make in
            make.centerX.equalToSuperview()
            make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom).offset(-44)
        }

        // 手电筒按钮点击
        self.flashlightBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.flashlightBtn.isSelected.toggle()
            self?.cameraOutputView.torchMode = (self?.flashlightBtn.isSelected ?? false) ? .on : .off
        }).store(in: &self.anyCancellables)

        // 相册按钮点击
        self.albumBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.imagePicking()
        }).store(in: &self.anyCancellables)

        // 相机输出Video数据
        self.cameraOutputView.didCaptureVideoDataPublisher.sink(receiveValue: { [weak self] sampleBuffer in
            guard let pixelBuffer = sampleBuffer.imageBuffer else { return }
            self?.vm.processEvent(.didCaptureVideoDataPixelBuffer(pixelBuffer))
        }).store(in: &self.anyCancellables)

        // 监听 CaptureOutView 的错误
        self.cameraOutputView.deviceErrorPublisher.sink(receiveValue: { [weak self] err in
            // 摄像头权限未打开
            if (err as NSError).domain == AVFoundationErrorDomain && (err as NSError).code == AVError.applicationIsNotAuthorizedToUseDevice.rawValue {
                self?.descriptionTextView.attributedText = self?.noLimitsOfCameraAuthorityDescriptionAttributedString
            }
        }).store(in: &self.anyCancellables)

        // 监听摄像头权限
        AVCaptureDevice.authorizationRequestPublisher(.video).receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { _ in }, receiveValue: { [weak self] status in
                // 无摄像头使用权限
                if status == .denied {
                    self?.descriptionTextView.attributedText = self?.noLimitsOfCameraAuthorityDescriptionAttributedString
                }else{
                    self?.descriptionTextView.text = self?.objective.description
                }
            }).store(in: &self.anyCancellables)

        // 开始扫描动画
        self.startScanningAnimation()

        self.vm.$status.sink(receiveValue: { [weak self] status in
            switch status {
            case .idle:
                break
            case let .didSucceedRecognizeShare(model):
                self?.handleScanDeviceShare(model)
            case .cannotRecognizeAnythingFromImage:
                // 无法从图片中识别二维码
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0478", note: "不支持的二维码"))
            case .QRCodeNotSupported:
                // 二维码不支持
                MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0478", note: "不支持的二维码"))
                self?.cameraOutputView.startCapture()
            case let .bindResult(result):
                self?.bindResultHandling(result)
            case .didRecognizeOldXiaotunCode:
                // 扫到旧小豚的码
                self?.cameraOutputView.stopCapture()
                let alertContent = Localization.shared.localized("#", note: "当前版本暂不支持直接绑定该设备，请下载\"小豚云\"APP添加使用。")
                let alert = ReoqooAlertViewController(alertTitle: .none, alertContent: .string(alertContent), actions: [
                    IVPopupAction.init(title: Localization.shared.localized("#", note: "知道了"), style: .cancel, handler: {
                        self?.cameraOutputView.startCapture()
                    }),
                    IVPopupAction.init(title: Localization.shared.localized("#", note: "去下载"), handler: {
                        UIApplication.shared.open(.init(string: "https://apps.apple.com/app/1226835767")!)
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                            self?.cameraOutputView.startCapture()
                        }
                    })
                ])
                self?.present(alert, animated: true)
            default:
                break
            }
        }).store(in: &self.anyCancellables)

        self.vm.$status
            .throttle(for: .milliseconds(1000), scheduler: DispatchQueue.main, latest: true)
            .receive(on: DispatchQueue.main)
            .sink(receiveValue: { [weak self] status in
                if case let .didDetectedQRCode(results) = status {
                    self?.didDetectedQRCodeResultsHandling(results)
                }
            }).store(in: &self.anyCancellables)
    }

    override var preferredStatusBarStyle: UIStatusBarStyle { .lightContent }
}

// MARK: Helper
extension QRCodeScanningViewController {

    // 挖孔扫描区域
    func transparentBackgroundHollowOut(_ rect: CGRect) {
        let path = CGMutablePath()
        path.addRect(rect)
        path.addRect(self.view.bounds)
        self.transparentBackground.shapeLayer.fillRule = .evenOdd
        self.transparentBackground.shapeLayer.path = path
        self.transparentBackground.shapeLayer.fillColor = UIColor(rgb: 0x000000).cgColor
        self.transparentBackground.shapeLayer.opacity = 0.5
    }

    func startScanningAnimation() {
        self.stopScanningAnimation()
        let animation = CABasicAnimation.init(keyPath: "position.y")
        animation.duration = 2
        animation.repeatCount = Float.greatestFiniteMagnitude
        animation.beginTime = CACurrentMediaTime()
        // 往复动画
        animation.autoreverses = true
        animation.fromValue = 0
        animation.toValue = self.qrCodeScanningFrameContainer.size.height
        self.qrCodeScanningLine.layer.add(animation, forKey: nil)
    }

    func stopScanningAnimation() {
        self.qrCodeScanningLine.layer.removeAllAnimations()
    }

    // 图片选择
    func imagePicking() {
        let vc = ImagePickerViewController.init(mediaTypes: [.image], allowUsingCamera: false)
        vc.delegate = self
        self.present(vc, animated: true)
    }

    // 成功请求到设备id
    func requestDeviceIdWithResultHandling(_ result: Result<RQDeviceAddition.PreconnectDevice, Swift.Error>) {
        MBProgressHUD.fromTag(100)?.hideDispatchOnMainThread()
        if case let .success(preconnectDevice) = result {
            logInfo("[DevicesAddition]: 用SN<\(preconnectDevice.sn)>换取设备id<\(preconnectDevice.sn)>")
            let flowItem = DeviceAdditionFlowItem.init(preconnectDevice: preconnectDevice, qrCodeFrom: self.vm.qrCodeFrom)
            /// start device addition work flow, and the func will return a NavigationController, present it
            let workflowVC = RQDeviceAddition.Agent.shared.startDeviceAdditionWorkflow(withWorkflowItem: flowItem)
            workflowVC.modalPresentationStyle = UIModalPresentationStyle.fullScreen
            self.navigationController?.present(workflowVC, animated: true)
        }
        if case let .failure(err) = result {
            logInfo("[DevicesAddition]: 用SN换取设备id失败: \(err)")
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0478", note: "不支持的二维码"))
            self.cameraOutputView.startCapture()
        }
        // 如果是遇到app版本过低错误, 弹窗提示
        if case let .failure(err) = result, (err as? ReoqooError)?.isReason(ReoqooError.DeviceConnectError.appVersionNotSupport) ?? false {
            self.presentUpdateAlert()
        }
    }

    // 绑定结果处理
    func bindResultHandling(_ result: Swift.Result<IDevice, Swift.Error>) {
        if case .success = result {
            self.navigationController?.popViewController(animated: false)
        }
        if case let .failure(err) = result {
            logInfo("[DevicesAddition] 绑定设备失败", err.localizedDescription)
            MBProgressHUD.showHUD_DispatchOnMainThread(text: err.localizedDescription)
            self.cameraOutputView.startCapture()
        }
    }

    /// 识别到类似二维码的结果
    func didDetectedQRCodeResultsHandling(_ results: [VNBarcodeObservation]) {
        self.cameraOutputView.stopCapture()
        logInfo("[图像识别] AI识别二维码/条码结果", results)
        // 过滤结果, 只识别 QRCode
        let qrResults = results.filter { $0.symbology == .qr }
        if qrResults.count > 1 { logInfo("识别到多个结果, 只取第一个结果作参考") }
        // 只取第一个结果作为参考, 如果以后要识别多个二维码可从这里入手
        guard let barcodeObservation = qrResults.first else {
            self.cameraOutputView.startCapture()
            return
        }
        guard let string = barcodeObservation.payloadStringValue else {
            logInfo("[图像识别] 识别二维码: payloadStringValue 为 nil")
            self.cameraOutputView.startCapture()
            return
        }
        // 信心过滤
        if barcodeObservation.confidence < 0.9 {
            logInfo("[图像识别] 识别二维码信心不足, 丢弃结果", string)
            self.cameraOutputView.startCapture()
            return
        }
        // 震
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
        // 采用结果
        self.vm.processEvent(.didSucceedRecognizeQRCodePayload(value: string))
    }

    func presentUpdateAlert() {
        let updateAction = IVPopupAction.init(title: String.localization.localized("AA0361", note: "去升级"), style: .custom, color: R.color.text_link_4A68A6()) {
            UIApplication.shared.open(URL.AppStoreURL)
        }
        let vc = ReoqooAlertViewController(alertContent: .string(String.localization.localized("AA0499", note: "当前APP版本过低,无法兼容此款新设备。")), actions: [
            .init(title: String.localization.localized("AA0059", note: "取消"), style: .custom, color: R.color.text_link_4A68A6(), font: .systemFont(ofSize: 16, weight: .medium)),
            updateAction
        ])
        vc.alertView.titleLabel.textAlignment = .center
        self.present(vc, animated: true)
    }
}

extension QRCodeScanningViewController: ImagePickerViewControllerDelegate {

    func imagePickerViewController(_ controller: ImagePickerViewController, didFinishSelectedAssets assets: [ImagePickerViewController.MediaItem]) {
        controller.dismiss(animated: false)
        guard let asset = assets.first else { return }
        guard let image = asset.image else { return }
        self.vm.processEvent(.analyzeImage(image))
    }

}

/// 访客面对面扫码确认接受他人分享
extension QRCodeScanningViewController {

    /// 处理扫描分享二维码确认分享
    func handleScanDeviceShare(_ model: ViewModel.ShareQRCodeInfo) {
        if model.expireTime <= Int(Date().timeIntervalSince1970) {
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0171", note: "二维码已超时，您可以让主人重新生成"))
            self.cameraOutputView.startCapture()
            return
        }

        //确认分享
        DeviceShare.handleScanningInvitationPublisher(deviceId: model.deviceID, invideToken: model.inviteCode, remarkName: model.productName)
            .sink(receiveCompletion: { [weak self] completion in
                guard case let .failure(error) = completion else { return }
                self?.handleConfirmShareFail(code: (error as NSError).code)
            }, receiveValue: { [weak self] item in
                if let devGuestNumLimit = item.devGuestNumLimit, devGuestNumLimit > 0 {
                    self?.handleConfirmShareFail(devGuestMaxLimit: devGuestNumLimit)
                } else {
                    self?.handleConfirmShareSuccess(model, productId: model.productId, name: model.productName)
                }
            }).store(in: &self.anyCancellables)
    }

    /// 处理确认分享成功结果
    func handleConfirmShareSuccess(_ model: ViewModel.ShareQRCodeInfo, productId: String, name: String) {
        let customView = DeviceShareHeaderView(style: .default, frame: .zero)
        customView.setImage(productId: productId)
        customView.setProductName(productId: productId)

        let property = ReoqooPopupViewProperty()
        property.title = String.localization.localized("AA0089", note: "添加成功")
        property.customView = customView
        property.ratio = 0.65

        IVPopupView(property: property, actions: [
            IVPopupAction(title: String.localization.localized("AA0059", note: "取消"), style: .custom, color: R.color.text_link_4A68A6(), handler: {
                DeviceManager.shared.addDevice(deviceId: model.deviceID, deviceName: name, deviceRole: .shared, permission: model.permission)
            }),
            IVPopupAction(title: String.localization.localized("AA0166", note: "立即查看"), style: .custom, color: R.color.text_link_4A68A6(), handler: {
                DeviceManager.shared.addDevice(deviceId: model.deviceID, deviceName: name, deviceRole: .shared, permission: model.permission, launchAfterSuccess: true)
            })
        ]).show()

        self.navigationController?.popViewController(animated: true)
    }

    /// 处理确认分享失败结果
    func handleConfirmShareFail(devGuestMaxLimit: Int = 0, code: Int = 0) {
        if devGuestMaxLimit > 0 {
            self.navigationController?.popViewController(animated: true)

            let property = ReoqooPopupViewProperty()
            property.message = String.localization.localized("AA0169", note: "设备添加已超过%@个访客，请联系主人先删除部分访客。", args: String(devGuestMaxLimit))
            property.messageAlign = .left

            IVPopupView(property: property,
                        actions: [IVPopupAction(title: String.localization.localized("AA0131", note: "知道了"),
                                                style: .custom,
                                                color: R.color.text_link_4A68A6(),
                                                handler: nil
                                               )]).show()
        } else if code == ReoqooError.DeviceShareError.guestAlreadyExistent.rawValue {
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0362", note: "已经添加该设备了"))
            self.navigationController?.popViewController(animated: true)
        } else if code == ReoqooError.DeviceShareError.notSupportSpanned.rawValue {
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0172", note: "您和主人不是同一区域，不支持分享"))
            self.navigationController?.popViewController(animated: true)
        } else {
            MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0170", note: "二维码失效，您可以让主人重新生成"))
            self.cameraOutputView.startCapture()
        }
    }
}
