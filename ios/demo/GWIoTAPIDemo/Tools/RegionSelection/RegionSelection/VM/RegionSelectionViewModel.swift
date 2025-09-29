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
        let allRegionsObservable: AnyPublisher<[RegionInfo], Never> = CurrentValueSubject.init(RegionInfoProvider.allRegionInfos).map {
            // 按首字母排序
            var afterSorted = $0.sorted {
                let chineseLocale = Locale.init(identifier: "zh_CN")
                // 按拼音排序
                if RQCore.Agent.shared.language == .CN {
                    return $0.countryNameOfAllVersions.simplifiedChineseVersion.localizedCompare($1.countryNameOfAllVersions.simplifiedChineseVersion) == .orderedAscending
                }
                // 繁体也按拼音排序
                if RQCore.Agent.shared.language == .TC {
                    return $0.countryNameOfAllVersions.traditionalChineseVersion.compare($1.countryNameOfAllVersions.traditionalChineseVersion, locale: chineseLocale) == .orderedAscending
                }
                return $0.countryNameOfAllVersions.englishVersion < $1.countryNameOfAllVersions.englishVersion
            }

            // 取出 "其他", 排到最后去
            let others = afterSorted.filter { $0.countryCode.isEmpty && $0.regionCode.isEmpty }
            afterSorted.removeAll { others.contains($0) }
            afterSorted.append(contentsOf: others)

            return afterSorted
        }.eraseToAnyPublisher()

        allRegionsObservable.combineLatest(self.$searchKeyword)
            .throttle(for: 0.3, scheduler: DispatchQueue.main, latest: true)
            .map { regions, searchKeyword -> [RegionInfo] in
                if let searchKeyword = searchKeyword, !searchKeyword.isEmpty {
                    return regions.filter({
                        // 匹配所有语言的名称
                        if $0.countryNameOfAllVersions.englishVersion.lowercased().contains(searchKeyword.lowercased()) {
                            return true
                        }
                        if $0.countryNameOfAllVersions.simplifiedChineseVersion.lowercased().contains(searchKeyword.lowercased()) {
                            return true
                        }
                        if $0.countryNameOfAllVersions.traditionalChineseVersion.lowercased().contains(searchKeyword.lowercased()) {
                            return true
                        }
                        // 匹配国家码 +86 ...
                        if $0.countryCode.contains(searchKeyword.lowercased()) {
                            return true
                        }
                        // 匹配国家
                        if $0.regionCode.lowercased().contains(searchKeyword.lowercased()) {
                            return true
                        }
                        return false
                    })
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
