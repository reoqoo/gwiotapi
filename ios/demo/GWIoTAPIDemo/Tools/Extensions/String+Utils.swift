//
//  String+Utils.swift
//  GWIoTAPIDemo
//
//  Created by xiaojuntao on 27/2/2025.
//

import Foundation

extension String {
    /// 对字符串进行脱敏处理
    /// @Note:
    ///     ```
    ///     // 使用例子：
    ///     "18512345678".replacing(start: 3, end: 6) //185****5678
    ///     "18512345678".replacing(start: 3, end: 6, pad: "+") //185++++5678
    ///     "18512345678".replacing(start: 3, end: 6, pad: "$_$") //185$_$$5678
    ///     "18512345678".replacing(start: 3, end: 6, pad: "$_$", limitLength: 3*2) //185$_$$_$5678
    ///     "18512345678".replacing(start: 3, end: 6, limitLength: 2) //185**5678
    ///     ```
    ///
    /// - Parameters:
    ///   - start: 开始脱敏字符下标
    ///   - end: 结束脱敏字符下标
    ///   - pad: 替换字符，默认"*"字符
    ///   - limitLength: 脱敏后替换字符最大限制长度，默认0，即是按默认长度
    /// - Returns: 脱敏后字符串
    func replacing(start: Int, end: Int, pad: String = "*", limitLength: Int = 0) -> String {
        if true.isEqual(oneOf: start < 0, end < 0, self.count < start, start > end) {
            return self
        }
        let subStartIndex = index(startIndex, offsetBy: start)
        let subEndIndex   = index(startIndex, offsetBy: (count - 1 < end + 1 ? count - 1 : end + 1))
        let subString     = self[subStartIndex...subEndIndex]
        let maskString    = "".padding(toLength: (limitLength <= 0 ? subString.count : limitLength), withPad: pad, startingAt: 0)
        return self[startIndex..<subStartIndex] + String(maskString) + self[subEndIndex..<endIndex]
    }
}
