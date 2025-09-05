//
//  AppentranceManager+KeyboardStatus.swift
//  Reoqoo
//
//  Created by xiaojuntao on 1/8/2023.
//

import Foundation

extension AppEntranceManager {
    
    enum ApplicationState {
        case didEnterBackground
        case willEnterForeground
        case didFinishLaunching
        case didBecomeActive
        case willResignActive
        case willTerminate
    }
}
