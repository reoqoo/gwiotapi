//
//  AppFeatureConfiguration.swift
//  Reoqoo
//
//  宿主 App 级「产品 / 功能」开关，供看家、我的等模块共享同一配置源。
//

import Foundation

enum AppFeatureConfiguration {

    /// 云录制解耦模式（与 `DHReoqooLaunchPrm.enableCloudRecordDecoupleMode` 对应）。
    static let enableCloudRecordDecoupleMode = true
}
