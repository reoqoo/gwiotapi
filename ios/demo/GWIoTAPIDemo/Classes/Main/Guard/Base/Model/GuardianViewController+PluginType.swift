//
//  GuardianViewController+PluginType.swift
//  Reoqoo
//
//  Created by xiaojuntao on 31/8/2023.
//

import Foundation

extension GuardianViewController {
    enum PluginType {
        case notDefineYet
        case live
        case event

        var collectionCellClass: PluginCollectionViewCell.Type {
            switch self {
            case .notDefineYet:
                return PluginCollectionViewCell.self
            case .live:
                return LiveCollectionCell.self
            case .event:
                return EventCollectionCell.self
            }
        }
    }
}
