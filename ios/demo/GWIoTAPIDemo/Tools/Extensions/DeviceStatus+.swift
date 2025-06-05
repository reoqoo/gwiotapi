//
//  DeviceStatus+.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 16/1/2025.
//

import Foundation
import RQCore

extension RQCore.DeviceStatus {

    var description: String {
        switch self {
        case .offline:  return String.localization.localized("AA0054", note: "离线")
        case .online:   return String.localization.localized("AA0053", note: "在线")
        case .shutdown: return String.localization.localized("AA0502", note: "关机")
        case .turningOn: return String.localization.localized("AA0502", note: "关机")
        case .turningOff:   return String.localization.localized("AA0053", note: "在线")
        @unknown default:
            return ""
        }
    }

    var image: UIImage? {
        switch self {
        case .offline:  return R.image.family_offline()
        case .online:   return R.image.family_online()
        case .shutdown: return R.image.family_shutdown()
        default: return nil
        }
    }

    var color: UIColor? {
        switch self {
        case .offline:  return R.color.device_offline_000000()
        case .online:   return R.color.device_online_15F715()
        case .shutdown: return R.color.device_shutdown_FA2A2D()
        case .turningOn: return R.color.device_offline_000000()
        case .turningOff: return R.color.device_online_15F715()
        @unknown default:
            return .white
        }
    }
}
