//
//  LiveViewCollectionViewFlowLayout.swift
//  Reoqoo
//
//  Created by xiaojuntao on 14/11/2023.
//

import Foundation

/// 由于 FlowLayout 横向滑动布局是纵向布局, 所以重写 UICollectionViewFlowLayout, 在 layoutAttributesForElements(in rect: CGRect) 方法中, 交换 UICollectionViewLayoutAttributes 的 frame
class LiveViewCollectionViewFlowLayout: UICollectionViewFlowLayout {
    
    var layoutMode: LiveViewContainer.LayoutMode = .fourGird

    override func layoutAttributesForElements(in rect: CGRect) -> [UICollectionViewLayoutAttributes]? {

        if self.layoutMode == .single { return super.layoutAttributesForElements(in: rect) }

        let numberOfRow = self.layoutMode.numOfRow
        let numberOfColumn = self.layoutMode.numOfColumn
        let numberOfItemsPerPage = numberOfRow * numberOfColumn
        
        guard let attributes = super.layoutAttributesForElements(in: rect)?.compactMap({ $0.copy() as? UICollectionViewLayoutAttributes }) else { return nil }

        // 纵向排列索引到横向排列的行列转换公式:
        // j = (i / (行数 * 页数)) * 行数 + (i % 行数) * 页数 + i / 行数
        
        // 根据上面 行列转换公式 赋值 attributes元素 中的 .frame 值
        attributes.forEach({
            let i = $0.indexPath.item
            let j = i / numberOfItemsPerPage * numberOfRow + (i % numberOfRow) * numberOfColumn + i / numberOfColumn
            let targetIndexPath = IndexPath.init(item: j, section: $0.indexPath.section)
            if let targetAttribute = super.layoutAttributesForItem(at: targetIndexPath) {
                $0.frame = targetAttribute.frame
            }
        })
        return attributes
    }

}
