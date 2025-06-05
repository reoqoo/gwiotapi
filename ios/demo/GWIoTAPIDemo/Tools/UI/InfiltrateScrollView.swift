//
//  InfiltrateScrollView.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 2/2/2024.
//

import Foundation

/// 为了使手势可穿透, 定义新的 Collectionview 类型
/// 在 FamilyViewController 嵌套双 ScrollView 设计中有用到
class InfiltrateCollectionView: UICollectionView {
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool { true }
}

/// 同上
class InfiltrateTableView: UITableView {
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool { true }
}
