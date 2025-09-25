//
//  DophiGoHiLinkIV.swift
//  Reoqoo
//
//  Created by xiaojuntao on 31/8/2023.
//

import Foundation

extension DophiGoHiLinkIV.DITCloudEventType: Codable, CaseIterable {
    public static var allCases: [DITCloudEventType] {
        return [.keyCall, .face, .human, .cryDet, .petDet, .flameDet, .carDet, .linkage, .motion, .sound, .cloudAction, .video, .infrared, .bellPush, .keyPush, .emergency]
    }
}

extension DophiGoHiLinkIV.DITCloudEventType {

    /// 目前 app 支持的事件类型
    static var validityTypes: [DITCloudEventType] = [.keyCall, .face, .human, .cryDet, .petDet, .carDet, .flameDet, .linkage, .motion]

    /// 将 DITCloudEventType 拆解
    var asArray: [DITCloudEventType] {
        var tmp = self
        var res: [DITCloudEventType] = []
        Self.allCases.forEach {
            guard let i = tmp.remove($0) else { return }
            if !Self.validityTypes.contains(i) { return }
            res.append(i)
        }
        return res
    }

    /// icon 集合, 关联 Self.validityTypes
    fileprivate static var validityIcons: [UIImage] = [R.image.guardDeviceNotify()!, R.image.guardFace()!, R.image.guardEventHumanActivity()!, R.image.guardBabyCry()!, R.image.guardPet()!, R.image.guardEventCar()!, R.image.guardEventFire()!, R.image.guardEventChangjingliandong()!, R.image.guardEventMove()!]
    var icon: UIImage {
        guard let idx = Self.validityTypes.firstIndex(where: { self.rawValue & $0.rawValue > 0 }), let icon = Self.validityIcons[safe_: idx] else {
            return R.image.guardEventMove()!
        }
        return icon
    }

    /// 描述 集合, 关联 Self.validityTypes
    fileprivate static var validityDescriptions: [String] {
        [String.localization.localized("AA0363", note: "设备呼叫"), String.localization.localized("AA0364", note: "人脸识别"), String.localization.localized("AA0209", note: "有人活动"), String.localization.localized("AA0365", note: "宝宝哭声"), String.localization.localized("AA0366", note: "宠物活动"), String.localization.localized("AA0212", note: "车辆移动"), String.localization.localized("AA0512", note: "疑似火焰"), String.localization.localized("AA0211", note: "场景联动"), String.localization.localized("AA0210", note: "画面变化")]
    }
    var description: String {
        guard let idx = Self.validityTypes.firstIndex(where: { self.rawValue & $0.rawValue > 0 }), let description = Self.validityDescriptions[safe_: idx] else {
            return String.localization.localized("AA0210", note: "画面变化")
        }
        return description
    }

    /// 事件描述 + 动词 / 多个事件返回 "看到多个事件"
    fileprivate static var validityDescriptions2: [String] {
        [String.localization.localized("AA0363", note: "设备呼叫"),
         String.localization.localized("AA0584", note: "执行 %@", args: [String.localization.localized("AA0364", note: "人脸识别")]),
         String.localization.localized("AA0582", note: "看到 %@", args: [String.localization.localized("AA0209", note: "有人活动")]),
         String.localization.localized("AA0583", note: "听到 %@", args: [String.localization.localized("AA0365", note: "宝宝哭声")]),
         String.localization.localized("AA0582", note: "看到 %@", args: [String.localization.localized("AA0366", note: "宠物活动")]),
         String.localization.localized("AA0582", note: "看到 %@", args: [String.localization.localized("AA0212", note: "车辆移动")]),
         String.localization.localized("AA0582", note: "看到 %@", args: [String.localization.localized("AA0512", note: "疑似火焰")]),
         String.localization.localized("AA0584", note: "执行 %@", args: [String.localization.localized("AA0211", note: "场景联动")]),
         String.localization.localized("AA0582", note: "看到 %@", args: [String.localization.localized("AA0210", note: "画面变化")])]
    }
    
    var description2: String {
        if self.asArray.count > 1 {
            return String.localization.localized("AA0582", note: "看到 %@", args: [String.localization.localized("AA0600", note: "多个事件")])
        }
        guard let idx = Self.validityTypes.firstIndex(where: { self.rawValue & $0.rawValue > 0 }), let description = Self.validityDescriptions2[safe_: idx] else {
            return String.localization.localized("AA0582", note: "看到 %@", args: [String.localization.localized("AA0210", note: "画面变化")])
        }
        return description
    }

    /// DHAlarmType 集合, 关联 Self.validityTypes
    fileprivate static var validityDHAlarmTypes: [DHAlarmType] = [.typeKeyCall, .typeFace, .typeHuman, .typeCryDet, .typePetDet, .typeCarDet, .typeFlameDet, .typeLinkage, .typeMotion]
    func convert2DHAlarmType() -> DHAlarmType {
        guard let idx = Self.validityTypes.firstIndex(where: { self.rawValue & $0.rawValue > 0 }), let alarmType = Self.validityDHAlarmTypes[safe_: idx] else {
            return .typeMotion
        }
        return alarmType
    }
}
