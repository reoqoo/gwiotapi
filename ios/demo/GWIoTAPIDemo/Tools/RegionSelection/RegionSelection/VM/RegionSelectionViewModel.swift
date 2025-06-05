//
//  RegionSelectionViewModel.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 25/7/2023.
//

import Foundation
import RQCore

class RegionSelectionViewModel {
    
    @DidSetPublished var dataSource: [RegionInfo] = []

    // 设计此Subject 与 ViewController.textField.rx.text 绑定. 当 ViewController 中的搜索框输入时, 监听输入内容.
    @DidSetPublished var searchKeyword: String?

    private var anyCancellables: Set<AnyCancellable> = []

    init() {
        let allRegionsObservable: Combine.CurrentValueSubject<[RegionInfo], Never> = .init(RegionInfoProvider.allRegionInfos)
        allRegionsObservable.combineLatest(self.$searchKeyword).map { regions, searchKeyword -> [RegionInfo] in
            if let searchKeyword = searchKeyword, !searchKeyword.isEmpty {
                return regions.filter({ $0.countryName.contains(searchKeyword) })
            }else{
                return regions
            }
        }
        .receive(on: DispatchQueue.main)
        .sink { [weak self] regions in
                self?.dataSource = regions
        }.store(in: &self.anyCancellables)
    }
    
}
