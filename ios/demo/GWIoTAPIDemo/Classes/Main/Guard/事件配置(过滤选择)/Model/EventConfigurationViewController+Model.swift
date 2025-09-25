//
//  EventConfigurationViewController+Model.swift
//  Reoqoo
//
//  Created by xiaojuntao on 31/8/2023.
//

import Foundation
import RQCore

extension EventConfigurationViewController {

    class Section {
        var filterCase: FilterCase
        /// 是否可展开
        var isExpandable: Bool
        init(filterCase: FilterCase, isExpandable: Bool) {
            self.filterCase = filterCase
            self.isExpandable = isExpandable
        }
    }

    /// 过滤器类型描述
    enum FilterCase: Equatable {
        case event
        case device(isExpanded: Bool)

        var title: String {
            switch self {
            case .event:
                return String.localization.localized("AA0207", note: "事件")
            case .device:
                return String.localization.localized("AA0048", note: "设备")
            }
        }

        // 以便 遵循 Equatable 进行比对
        var rawValue: Int {
            switch self {
            case .event:
                return 0
            case .device:
                return 1
            }
        }

        static func == (lhs: Self, rhs: Self) -> Bool {
            return lhs.rawValue == rhs.rawValue
        }
    }

    /// collectionView 数据源模型
    class CollectionViewCellItem {

        enum ItemType: CustomStringConvertible, Equatable {
            case all    // 全部
            case event(DITCloudEventType)
            case device(DeviceEntity)

            var description: String {
                switch self {
                case .all:
                    return String.localization.localized("AA0208", note: "全部")
                case let .device(device):
                    return device.remarkName
                case let .event(eventType):
                    return eventType.description
                }
            }
            
            var icon: UIImage? {
                switch self {
                case let .event(eventType):
                    return eventType.icon
                default:
                    return nil
                }
            }

            static func == (lhs: Self, rhs: Self) -> Bool {
                if case .all = lhs, case .all = rhs {
                    return true
                }
                if case let .device(device_l) = lhs, case let .device(device_r) = rhs {
                    return device_l == device_r
                }
                if case let .event(event_l) = lhs, case let .event(event_r) = rhs {
                    return event_l == event_r
                }
                return false
            }
        }

        var type: ItemType
        var isSelected: Bool = false
        init(type: ItemType) {
            self.type = type
        }

    }
    
}

extension Array where Element == EventConfigurationViewController.Section {
    // 尝试获取索引
    func firstIndex(withFilterCase filterCase: EventConfigurationViewController.FilterCase) -> Int? {
        self.firstIndex { $0.filterCase == filterCase }
    }
}
