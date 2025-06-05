//
//  FamilyViewController2+Model.swift
//  Reoqoo
//
//  Created by xiaojuntao on 10/3/2025.
//

import Foundation

extension FamilyViewController2 {
    struct ImportantNoticeItem {
        /// 标题
        var title: String = ""
        /// 标签
        var tag: String = ""
        /// url
        var url: String = ""
        /// 展示位置
        var showOpt: IVBBSMgr.ShowOption = .none
        /// 对应通知的标签
        var noticeTag: String = ""
    }
}
