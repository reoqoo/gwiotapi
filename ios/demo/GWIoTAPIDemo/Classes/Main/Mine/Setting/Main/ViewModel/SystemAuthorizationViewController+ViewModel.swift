//
//  SystemAuthorizationViewController+ViewModel.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 11/6/2024.
//

import Foundation
import CoreLocation
import CoreBluetooth
import Photos
import RQCore

extension SystemAuthorizationViewController {
    class ViewModel {
        /// 定位访问权限
        var localicationAuthorization: SettingViewController.AuthorizationStatusItem = .init(title: String.localization.localized("AA0619", note: "位置"), description: String.localization.localized("AA0620", note: "开启后，可帮助设备在添加过程中连接Wi-Fi"))
        /// 蓝牙访问权限
        var bluetoothAuthorization: SettingViewController.AuthorizationStatusItem = .init(title: String.localization.localized("AA0621", note: "蓝牙"), description: String.localization.localized("AA0620", note: "开启后，可帮助设备在添加过程中连接Wi-Fi"))
        /// 相机访问权限
        var cameraAuthorization: SettingViewController.AuthorizationStatusItem = .init(title: String.localization.localized("AA0546", note: "相机"), description: String.localization.localized("AA0626", note: "开启后，可使用扫一扫添加设备"))
        /// 相册访问权限
        var albumAuthorization: SettingViewController.AuthorizationStatusItem = .init(title: String.localization.localized("AA0064", note: "相册"), description: String.localization.localized("AA0623", note: "开启后，可帮助保存视频截图和录像"))
        /// 麦克风访问权限
        var micAuthorization: SettingViewController.AuthorizationStatusItem = .init(title: String.localization.localized("AA0627", note: "麦克风"), description: String.localization.localized("AA0628", note: "开启后，可使用设备语音对讲、设置自定义语音"))
        /// 本地网络访问权限
        var localNetworkAuthorization: SettingViewController.AuthorizationStatusItem = .init(title: String.localization.localized("AA0636", note: "本地网络"), description: String.localization.localized("AA0620", note: "开启后，可帮助设备在添加过程中连接Wi-Fi"))

        var tableViewDataSources: [SettingViewController.AuthorizationStatusItem] {
            [self.localicationAuthorization, self.bluetoothAuthorization, self.cameraAuthorization, self.albumAuthorization, self.micAuthorization, self.localNetworkAuthorization]
        }

        var anyCancellables: Set<AnyCancellable> = []

        init() {
            // 定位权限
            var locationAuthorizedStatus: CLAuthorizationStatus = .notDetermined
            if #available(iOS 14.0, *) {
                locationAuthorizedStatus = CLLocationManager().authorizationStatus
            } else {
                locationAuthorizedStatus = CLLocationManager.authorizationStatus()
            }
            self.localicationAuthorization.isValid = locationAuthorizedStatus == .authorizedAlways || locationAuthorizedStatus == .authorizedWhenInUse

            // 蓝牙
            let bluetoothAuthorizedStatus = CBCentralManager.init().state
            self.bluetoothAuthorization.isValid = bluetoothAuthorizedStatus == .unknown || bluetoothAuthorizedStatus == .unauthorized

            // 相机
            let cameraAuthorizedStatus = AVCaptureDevice.authorizationStatus(for: .video)
            self.cameraAuthorization.isValid = cameraAuthorizedStatus == .authorized

            // 相册
            var albumAuthorizedStatus: PHAuthorizationStatus = .notDetermined
            if #available(iOS 14, *) {
                albumAuthorizedStatus = PHPhotoLibrary.authorizationStatus(for: .readWrite)
            } else {
                albumAuthorizedStatus = PHPhotoLibrary.authorizationStatus()
            }
            self.albumAuthorization.isValid = albumAuthorizedStatus == .authorized

            // 麦克风
            let micAuthorizedStatus = AVCaptureDevice.authorizationStatus(for: .audio)
            self.micAuthorization.isValid = micAuthorizedStatus == .authorized

            // 本地网络
            // 这个权限比较特殊
            // 1. 到系统开关该项后, APP不会重启, 因此需要结合状态实时监听该开关状态
            // 2. 和上面的权限状态获取不一样, 上面的状态查询操作不会触发授权操作, 这个状态获取即触发授权操作
            AppEntranceManager.shared.$applicationState
                .sink(receiveValue: { [weak self] state in
                    if state != .didBecomeActive { return }
                    RQCore.LocalNetworkAuthorization.shared.requestAuthorization(completion: { flag in
                        self?.localNetworkAuthorization.isValid = flag
                    })
                }).store(in: &self.anyCancellables)
        }
    }
}
