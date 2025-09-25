//
//  Event.swift
//  Reoqoo
//
//  Created by xiaojuntao on 30/8/2023.
//

import Foundation
import RQCore

extension GuardianViewController {
    /// 事件模型
    class Event: Codable {
        let devId: Int
        let alarmId: String
        let alarmType: DophiGoHiLinkIV.DITCloudEventType
        let startTime: TimeInterval
        let endTime: TimeInterval
        let duration: TimeInterval
        let imgUrl: String?
        let thumbUrlSuffix: String?

        /// 返回 startTime 当日日期的字符串描述
        lazy var startTimeDayStr: String = {
            return Date.init(timeIntervalSince1970: self.startTime).string(with: "yyyy-MM-dd")
        }()
        
        /// 返回 startTime 当日日期0时的时间戳
        lazy var startTimeDayTimeInterval: TimeInterval = {
            return Date.init(string: self.startTimeDayStr, dateFormat: "yyyy-MM-dd")?.timeIntervalSince1970 ?? Date().timeIntervalSince1970
        }()

        // 设备名称发布者
        lazy var deviceNameObservable: AnyPublisher<String, Never> = {
            let devId = String(self.devId)
            return DeviceManager.fetchDevice(devId)?.publisher(\.remarkName, whenErrorOccur: "") ?? Just<String>("").eraseToAnyPublisher()
        }()

        var imageURL: URL? {
            guard let path = self.imgUrl else { return nil }
            return .init(string: path)
        }
    }
}
