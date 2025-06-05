//
//  AppentranceManager+KeyboardStatus.swift
//  Reoqoo
//
//  Created by xiaojuntao on 1/8/2023.
//

import Foundation

extension AppEntranceManager {

    // 键盘状态信息记录
    struct KeyboardStatus {
        let frame: CGRect
        let isShow: Bool
    }
    
    enum ApplicationState {
        case didEnterBackground
        case willEnterForeground
        case didFinishLaunching
        case didBecomeActive
        case willResignActive
        case willTerminate
    }
}
