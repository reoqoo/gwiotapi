//
//  UICollectionView+.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 1/9/2023.
//

import Foundation

extension UICollectionView {
    /// 当使用自带的 `selectItem(at: indexPath, animated: animated, scrollPosition: scrollPosition)` 方法试图选择一个超过界限的 indexPath 会报错崩溃
    /// 此方法先判断是否越界, 在决定是否做选择操作
    func safeSelectItem(at indexPath: IndexPath, animated: Bool, scrollPosition: ScrollPosition) {
        let numberOfSections = self.numberOfSections
        if !(0..<numberOfSections).contains(indexPath.section) { return }
        let numberOfItemsInTargetSection = self.numberOfItems(inSection: indexPath.section)
        if !(0..<numberOfItemsInTargetSection).contains(indexPath.item) { return }
        self.selectItem(at: indexPath, animated: animated, scrollPosition: scrollPosition)
    }

    /// 带 reloadData 完成事件
    func reloadData(completion: @escaping (() -> Void)) {
        self.reloadData()
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
            completion()
        }
    }
}
