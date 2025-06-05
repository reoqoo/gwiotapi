//
//  Collection+.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 25/7/2023.
//

import Foundation

extension Collection where Indices.Iterator.Element == Index {
    // 为数组集合下标访问提供一个安全的方式
    // [1, 2][safe_: 3]  返回 nil
    public subscript(safe_ index: Index) -> Iterator.Element? {
        return (self.startIndex <= index && index < self.endIndex) ? self[index] : nil
    }
}
