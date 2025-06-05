//
//  CameraOutputView.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 7/8/2023.
//

import UIKit
import RQCore

class CameraOutputView: UIView {

    static let videoOutputRecognizeQueue: DispatchQueue = .init(label: "CameraOutputView.VideoOutputRecognizeQueue")

    enum AutoFocusAreaOption {
        // 默认为视图中间点
        case `default`
        case custom(_ point: CGPoint)
    }
    
    private(set) var captureDevice: AVCaptureDevice?
    private(set) var deviceInput: AVCaptureDeviceInput?
    private(set) var videodataOutput: AVCaptureVideoDataOutput?
    private(set) var session: AVCaptureSession?
    private(set) var outputLayer: AVCaptureVideoPreviewLayer?

    private var latestFocusFactor: Double = 1
    // 双指缩放手势
    private(set) lazy var pullFocusGesture: UIPinchGestureRecognizer = .init()
    // 双击手势
    private(set) lazy var doubleTapGesture: UITapGestureRecognizer = .init().then {
        $0.numberOfTapsRequired = 2
    }

    // 自动对焦区域设置
    public var autoFocusPoint: AutoFocusAreaOption = .default {
        didSet {
            // 设置自动聚焦点
            guard let captureDevice = self.captureDevice else { return }
            if !captureDevice.isFocusPointOfInterestSupported { return }

            try? captureDevice.lockForConfiguration()

            if case .default = self.autoFocusPoint {
                captureDevice.focusPointOfInterest = self.center
            }
            if case let .custom(point) = self.autoFocusPoint {
                captureDevice.focusPointOfInterest = point
            }

            captureDevice.unlockForConfiguration()
        }
    }
    
    // 闪光灯是否开启
    public var torchMode: AVCaptureDevice.TorchMode = .off {
        didSet {
            guard let captureDevice = self.captureDevice else { return }
            try? captureDevice.lockForConfiguration()
            if captureDevice.hasFlash && captureDevice.isFlashAvailable && captureDevice.isTorchAvailable {
                captureDevice.torchMode = self.torchMode
            }
            captureDevice.unlockForConfiguration()
        }
    }

    /// 当从开启权限到采集中遇到任何错误, 此发布者都会将错误发布出来
    private(set) var deviceErrorPublisher: Combine.PassthroughSubject<Swift.Error, Never> = .init()

    /// 视频数据采集发布者
    private(set) var didCaptureVideoDataPublisher: Combine.PassthroughSubject<CMSampleBuffer, Never> = .init()

    var anyCancellables: Set<AnyCancellable> = []

    override init(frame: CGRect) {
        super.init(frame: frame)
        self.setup()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.setup()
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        self.outputLayer?.frame = self.bounds
    }

    func setup() {
        self.backgroundColor = .black
        
        // 添加缩放手势
        self.addGestureRecognizer(self.pullFocusGesture)
        // 手势处理
        self.pullFocusGesture.pinchPublisher.sink(receiveValue: { [weak self] gesture in
            guard let device = self?.captureDevice else { return }
            let maxFocusFactor = device.maxAvailableVideoZoomFactor
            let minFocusFactor = device.minAvailableVideoZoomFactor
            if gesture.state == .began {
                self?.latestFocusFactor = device.videoZoomFactor
                return
            }
            if gesture.state == .changed {
                var factor = (self?.latestFocusFactor ?? 1) * gesture.scale
                factor = min(factor, maxFocusFactor)
                factor = max(factor, minFocusFactor)
                do {
                    try device.lockForConfiguration()
                    device.videoZoomFactor = factor
                    device.unlockForConfiguration()
                } catch {}
            }
        }).store(in: &self.anyCancellables)

        // 添加缩放手势
        self.addGestureRecognizer(self.doubleTapGesture)
        // 手势处理
        self.doubleTapGesture.tapPublisher.sink(receiveValue: { [weak self] gesture in
            guard let device = self?.captureDevice else { return }
            let maxFocusFactor = floor(device.maxAvailableVideoZoomFactor * 0.7)
            let minFocusFactor = device.minAvailableVideoZoomFactor
            if gesture.state == .began {
                self?.latestFocusFactor = device.videoZoomFactor
                return
            }
            if gesture.state == .ended {
                let factor = device.videoZoomFactor < maxFocusFactor ? maxFocusFactor : minFocusFactor
                do {
                    try device.lockForConfiguration()
                    device.ramp(toVideoZoomFactor: factor, withRate: 10)
                    device.unlockForConfiguration()
                } catch {}
            }
        }).store(in: &self.anyCancellables)
    }

    func deviceSetup() throws {
        // Device
        let captureDevice = AVCaptureDevice.default(for: .video)
        guard let captureDevice = captureDevice else {
            throw ReoqooError.generalError(reason: .cannotInitCameraDevice)
        }

        try captureDevice.lockForConfiguration()

        // 设置自动聚焦
        if captureDevice.isFocusModeSupported(.autoFocus) {
            captureDevice.focusMode = .continuousAutoFocus
        }

        captureDevice.unlockForConfiguration()
        self.captureDevice = captureDevice

        // Session
        let session = AVCaptureSession.init()
        if session.canSetSessionPreset(.high) {
            session.sessionPreset = .high
        }

        // Input
        let deviceInput = try AVCaptureDeviceInput.init(device: captureDevice)
        self.deviceInput = deviceInput

        if let deviceInput = self.deviceInput, session.canAddInput(deviceInput) {
            session.addInput(deviceInput)
        }

        // Video Data Output
        let videoDataOutput = AVCaptureVideoDataOutput.init()
        videoDataOutput.setSampleBufferDelegate(self, queue: Self.videoOutputRecognizeQueue)
        // 视频数据像素格式
        var pixelFormatType = kCVPixelFormatType_32BGRA
        // 优先选择BGRA 像素格式, BGRA 对 AI 识别功能更好但会消耗更多内存, 其次选择 420v https://developer.apple.com/documentation/technotes/tn3121-selecting-a-pixel-format-for-an-avcapturevideodataoutput
        let recommendedPixelFormatTypes = ["BGRA", "420f", "420v"]
        let availableVideoPixelFormatTypes = videoDataOutput.availableVideoPixelFormatTypes
        for i in recommendedPixelFormatTypes {
            for t in availableVideoPixelFormatTypes {
                if i == t.convertToString() {
                    pixelFormatType = t
                    break
                }
            }
        }
        videoDataOutput.videoSettings = [(kCVPixelBufferPixelFormatTypeKey as String): pixelFormatType]

        if session.canAddOutput(videoDataOutput) {
            session.addOutput(videoDataOutput)
        }

        self.videodataOutput = videoDataOutput
        self.session = session

        // OutputLayer
        let outputLayer = AVCaptureVideoPreviewLayer.init(session: session)
        outputLayer.videoGravity = .resizeAspectFill
        self.layer.addSublayer(outputLayer)
        self.outputLayer = outputLayer
    }

    func startCapture() {
        do {
            try self.deviceSetup()
        } catch let err {
            self.deviceErrorPublisher.send(err)
        }
        if self.session?.isRunning ?? false { return }
        DispatchQueue.global().async {
            self.session?.startRunning()
        }
    }

    func stopCapture() {
        self.session?.stopRunning()
    }
    
    // 拉近焦距 (放大)
    func zoomIn2MaxSmoothly() {
        guard let device = self.captureDevice else { return }
        let maxAvailableVideoZoomFactor = device.maxAvailableVideoZoomFactor
        do {
            try device.lockForConfiguration()
            device.ramp(toVideoZoomFactor: maxAvailableVideoZoomFactor, withRate: 0.6)
            device.unlockForConfiguration()
        } catch {
            
        }
    }
    
    // 拉远焦距 (缩小)
    func zoomOut2MinSmoothly() {
        do {
            try self.captureDevice?.lockForConfiguration()
            self.captureDevice?.ramp(toVideoZoomFactor: 1, withRate: 0.6)
            self.captureDevice?.unlockForConfiguration()
        } catch {
            
        }
    }
}

// 对 Video Data 进行捕获, 往外输出
extension CameraOutputView: AVCaptureVideoDataOutputSampleBufferDelegate {
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        self.didCaptureVideoDataPublisher.send(sampleBuffer)
    }
}

extension FourCharCode {
    // 打印 PixelFormatType
    // https://stackoverflow.com/questions/14537897/getting-actual-nsstring-of-avcapturevideodataoutput-availablevideocvpixelformatt
    func convertToString() -> String {
        let number: UInt32 = self
        let ostype = number.bigEndian // Assuming big-endian byte order
        let bytes = withUnsafeBytes(of: ostype) {
            Array($0)
        }
        let ostypeString = String(bytes: bytes, encoding: .ascii)
        return ostypeString ?? "error converting FourCharCode to String"
   }
}
