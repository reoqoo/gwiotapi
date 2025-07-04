//
//  UserDefaults+Key.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 25/7/2023.
//

import Foundation

extension UserDefaults {
    // UserDefaults 全局 Key
    enum GlobalKey: String {

        /// 强开云服务入口
        case Reoqoo_IsForceOpenVasEntrance
        /// 强开4G流量入口
        case Reoqoo_IsForceOpen4GFluxEntrance

        /// 强开设备权限管理功能
        case Reoqoo_IsSharePermissionConfigurationSupport

        // 用户选择的地区信息 Dictionary<String, String>
        case Reoqoo_UserSelectedRegionInfo
        // wifi 连接信息 Dictionary<String, String>
        case Reoqoo_WiFiConnectionInfo
        // 首次使用 app 需要弹出用户协议弹框, 记录一下同意协议的版本号 String
        case Reoqoo_AgreeToUsageAgreementOnAppVersion
        /// 指定的语言( 写在 NSBundle+Language.m 中 ), swift 代码没有直接使用此值, 此处声明仅做备忘用
        case Reoqoo_AssignLanguage

        /// 最近执行迁移操作的版本
        case Reoqoo_MigrationRecord
    }

    // UserDefaults 用户相关信息 Key, 跟用户绑定
    // 每个 User 对象下有专属 UserDefaults.init(suiteName: User.id) 对象用户存储相关数据, 换言之, 每个 User 下的 UserDefaults 存储的数据都是独立的
    enum UserKey: String {

        /// 记录上次 AccessToken 更新时间 Double
        case Reoqoo_LatestUpdateAccessTokenTime

        /// 固件升级任务持久化
        /// 存储 某设备的所有升级记录
        /// - key: `Reoqoo_FirmwareUpgradeTasks
        /// - value:`[Device.FirmwareUpgradeTask]` (数组) json `<Data> 类型`
        case Reoqoo_FirmwareUpgradeTasks

        /// 消息中心 APP 新版本消息
        /// 数据: `[String: MessageCenter.FirstLevelMessageItem]`
        /// key 为 新版本号, value 为 `MessageCenter.FirstLevelMessageItem`
        case Reoqoo_NewVersionMessage

        /// 消息中心 固件新版本消息
        /// 数据 [MessageCenter.FirmwareUpdateMessageRecord]
        case Reoqoo_NewFirmwareMessage
        
        /// 首次查看看家新手引导数据记录 Data 类型的: [BeginnerGuidance.ShowRecordInfo] 数据
        case Reoqoo_BeginnerGuidanceInfo

        /// 看家直播画面布局方式, 类型为 Int, 映射枚举类型 `LiveViewContainer.LayoutMode`
        case Reoqoo_LiveViewLayoutMode

        /// 看家直播功能中, 多画面设备于单画面显示时所选中的画面
        /// 这是键的前缀, 具体存的键为 "Reoqoo_LiveViewMulitCameraSelectedIndexKeyPrefix_" + "DeviceId"
        case Reoqoo_LiveViewMulitCameraSelectedIndexKeyPrefix
    }
}
