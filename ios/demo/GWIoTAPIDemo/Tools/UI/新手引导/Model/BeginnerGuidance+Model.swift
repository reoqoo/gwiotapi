//
//  BeginnerGuidance+Model.swift
//  Reoqoo
//
//  Created by xiaojuntao on 25/9/2023.
//

import Foundation

extension BeginnerGuidance {

    enum Scenes: String, Codable {
        case guardina_moreBtn
        case homePage_addBtn
        case homePage_deviceCardOwner
        case homePage_deviceCardVisitor
    }
    
    /// 此模型存储在 UserDefaults 中, 记录查看过新手引导的信息
    struct ShowRecordInfo: Codable {
        var appVersion: String = Bundle.majorVersion
        var scene: Scenes

        func fetchRecords() -> [ShowRecordInfo] {
            guard let guidanceInfosData = AccountCenter.shared.currentUser?.userDefault?.object(forKey: UserDefaults.UserKey.Reoqoo_BeginnerGuidanceInfo.rawValue) as? Data, let guidanceInfos = try? JSON.init(data: guidanceInfosData).decoded(as: [BeginnerGuidance.ShowRecordInfo].self) else { return [] }
            return guidanceInfos
        }

        func storeRecord() {
            // 读取
            var guidanceInfos = self.fetchRecords()
            // 插入
            guidanceInfos.append(self)
            // 持久化
            let data = try? guidanceInfos.encoded()
            AccountCenter.shared.currentUser?.userDefault?.set(data, forKey: UserDefaults.UserKey.Reoqoo_BeginnerGuidanceInfo.rawValue)
            AccountCenter.shared.currentUser?.userDefault?.synchronize()
        }
    }

}

extension BeginnerGuidance {
    class Item {

        var content: String
        var contentColor: UIColor = R.color.text_FFFFFF()!
        var contentFont: UIFont = .systemFont(ofSize: 14)

        var actionTitle: String
        var actionColor: UIColor = R.color.text_link_4280EF()!
        var actionFont: UIFont = .systemFont(ofSize: 14)
        
        var backgroundColor: UIColor = .black.withAlphaComponent(0.75)
        
        /// 引导指示要显示在哪个目标下
        weak var target: UIView?
        
        /// 指示器展示于哪个SuperView
        weak var inView: UIView?

        var scene: Scenes

        var showAfterDelay: TimeInterval

        init(content: String, actionTitle: String, scene: Scenes, target: UIView? = nil, showAfterDelay: TimeInterval = 0) {
            self.content = content
            self.actionTitle = actionTitle
            self.scene = scene
            self.target = target
            self.showAfterDelay = showAfterDelay
        }
    }
}

// MARK: BeginnerGuidance.Item Const
extension BeginnerGuidance.Item {

    /// 看家更多按钮引导
    static let GuidanceItem0 = BeginnerGuidance.Item.init(content: String.localization.localized("AA0339", note: "点击此处可编辑小卡片"), actionTitle: String.localization.localized("AA0131", note: "知道了"), scene: .guardina_moreBtn)
    /// 首页添加按钮引导
    static let GuidanceItem1 = BeginnerGuidance.Item.init(content: String.localization.localized("AA0336", note: "点击此处可快速添加设备或分享设备或扫一扫"), actionTitle: String.localization.localized("AA0131", note: "知道了"), scene: .homePage_addBtn)
    /// 首页设备卡片引导 (主人)
    static let GuidanceItem2 = BeginnerGuidance.Item.init(content: String.localization.localized("AA0337", note: "长按设备卡片可分享设备或删除设备，拖动设备卡片可进行排序"), actionTitle: String.localization.localized("AA0131", note: "知道了"), scene: .homePage_deviceCardOwner)
    /// 首页设备卡片引导 (访客)
    static let GuidanceItem3 = BeginnerGuidance.Item.init(content: String.localization.localized("AA0338", note: "长按设备卡片可删除设备，拖动设备卡片可进行排序"), actionTitle: String.localization.localized("AA0131", note: "知道了"), scene: .homePage_deviceCardVisitor)
    
    static let scene_guidanceItem_mapping: [BeginnerGuidance.Scenes: BeginnerGuidance.Item] = [.guardina_moreBtn: .GuidanceItem0, .homePage_addBtn: .GuidanceItem1, .homePage_deviceCardOwner: .GuidanceItem2, .homePage_deviceCardVisitor: .GuidanceItem3]
}
