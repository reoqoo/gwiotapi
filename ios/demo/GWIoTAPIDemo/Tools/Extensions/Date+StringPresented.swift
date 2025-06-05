//
//  Date+StringPresented.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 19/9/2023.
//

import Foundation

extension Date {

    /// 更友好的表示...时间
    /// 用例
    ///     - 某今日时间: HH:mm
    ///     - 某昨日时间: 昨日 HH:mm
    ///     - 某本周时间: 星期N HH:mm
    ///     - 某今年时间: MM/dd HH:mm
    ///     - 其他: yyyy/mm/dd HH:mm
    var friendlyPresented: String {
        let nowDate = Date()
        if self.isToday() {
            return self.string(with: "HH:mm")
        } else if self.isYesterday() {
            return String.localization.localized("AA0417", note: "昨天") + " " + self.string(with: "HH:mm")
        } else if self.weekOfYear == nowDate.weekOfYear, self.year == nowDate.year {
            let weekdaySymbol = Calendar.current.shortWeekdaySymbols[safe_: self.weekday - 1] ?? ""
            return weekdaySymbol + self.string(with: "HH:mm")
        } else if self.year == nowDate.year {
            return self.string(with: "MM/dd HH:mm")
        }
        return self.string(with: "yyyy/MM/dd HH:mm")
    }

}
