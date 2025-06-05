//
//  CloseAccountFlowItem.swift
//  Reoqoo
//
//  Created by xiaojuntao on 18/9/2023.
//

import Foundation

extension CloseAccountReasonSelectionViewController {

    struct CloseAccountFlowItem {
        var reason: CloseAccountReason?
        var otherSuggestion: String?

    }

    enum CloseAccountReason: Int, CaseIterable, CustomStringConvertible {
        case deviceBroken = 1
        case appIsSuck = 2
        case muchAD = 4
        case hasOtherAccount = 8
        case other = 0

        var description: String {
            switch self {
            case .deviceBroken:
                return  String.localization.localized("AA0311", note: "设备坏了/不好用")
            case .appIsSuck:
                return  String.localization.localized("AA0312", note: "APP不好用")
            case .muchAD:
                return String.localization.localized("AA0313", note: "广告太多了")
            case .hasOtherAccount:
                return String.localization.localized("AA0314", note: "已有其他平台账号")
            case .other:
                return String.localization.localized("AA0315", note: "其他原因")
            }
        }
    }
}
