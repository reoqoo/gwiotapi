//
//  DeviceEntity+Permission.swift
//  Reoqoo
//
//  Created by ZengYuYing on 2025/5/8.
//

import Foundation
import Combine
import GWIoTApi
import RQCore

class ReoqooDevice : GWIoTApi.IDevice {
    var deviceId: String
    
    var solution: GWIoTApi.Solution
    
    init(solution: GWIoTApi.Solution, deviceId: String) {
        self.solution = solution
        self.deviceId = deviceId
    }
}

extension RQCore.DeviceEntity {
    /// 获取设备回放权限描述发布者
    public func getDevicePlaybackPermissionPublisher() -> AnyPublisher<Bool, Swift.Error> {
        Deferred {
            Future { [weak self] promise in
                guard let self else { return }
                GWIoT.shared.getCloudPlaybackPermission(device: ReoqooDevice(solution: self.solution == .yoosee ? .yoosee : .reoqoo, deviceId: self.deviceId)) { res, error in
                    switch gwiot_handleCb(res, error) {

                    case .success(let support):
                        promise(.success(support?.boolValue ?? false))
                    case .failure(let err):
                        promise(.failure(err))

                    }
                }
            }
        }.eraseToAnyPublisher()
    }

    /// 获取多个设备回放权限描述发布者
    public static func getDevicesPlaybackPermissionPublisher(_ devices: [DeviceEntity]) -> AnyPublisher<[(deviceId: String, hasPlaybackPermission: Bool)], Swift.Error> {
        let pubs = devices.map { device in
            let devid = device.deviceId
            return device.getDevicePlaybackPermissionPublisher().map { (deviceId: devid, hasPlaybackPermission: $0) }
        }
        return pubs.combineLatest().eraseToAnyPublisher()
    }
}
