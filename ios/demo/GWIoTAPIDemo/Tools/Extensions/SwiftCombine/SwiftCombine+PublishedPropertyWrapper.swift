//
//  SwiftCombine+PublishedPropertyWrapper.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 17/6/2024.
//

import Foundation

@propertyWrapper
class DidSetPublished<T> {

    var wrappedValue: T {
        didSet {
            // 当 wrappedValue 发生变化, 发布者发布事件
            self.projectedValue.send(self.wrappedValue)
        }
    }

    lazy var projectedValue: Combine.CurrentValueSubject<T, Never> = .init(self.wrappedValue)

    init(wrappedValue: T) {
        self.wrappedValue = wrappedValue
    }

}
