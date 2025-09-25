//
//  LiveViewContainer+ViewModel.swift
//  Reoqoo
//
//  Created by xiaojuntao on 29/8/2023.
//

import Foundation
import RQCore

extension LiveViewContainer {

    class ViewModel {

        /// 布局模式
        @DidSetPublished var layoutMode: LayoutMode = .fourGird {
            didSet {
                self.dataSourceSetup()
                self.numberOfPages = self.caculateNumberOfPages()
            }
        }

        /// 有多少页
        @DidSetPublished var numberOfPages: Int = 0

        /// CollectionView 数据源
        @DidSetPublished var dataSources: [CollectionViewDataItem] = []

        var devices: [DeviceEntity] = [] {
            didSet {
                self.dataSourceSetup()
                self.numberOfPages = self.caculateNumberOfPages()
            }
        }

        // MARK: Helper
        // setup collectionView 数据源数据
        func dataSourceSetup() {

            // 组装 dataSource 数据
            self.dataSources = self.devices.map({ .device($0) })

            // 如果数据源只有一个项, 或者 布局方式 为 single 就不需要做补充操作了
            if self.dataSources.count == 1 || self.layoutMode == .single { return }

            // 求 dataSource.count 和 4 的最大公倍数, 用 .placeholder 补充尾部
            let lcm = Int(ceil(Double(self.dataSources.count) / 4)) * 4
            let supplements: [CollectionViewDataItem] = (0..<(lcm - self.dataSources.count)).map({ _ in .placeholder })
            self.dataSources.append(contentsOf: supplements)
        }

        /// 计算页数
        func caculateNumberOfPages() -> Int {
            if self.dataSources.count == 1 || self.layoutMode == .single {
                return self.dataSources.count
            }
            return self.dataSources.count / 4
        }
        
    }

}
