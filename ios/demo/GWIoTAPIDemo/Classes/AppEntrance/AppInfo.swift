//
//  AppInfo.swift
//  Reoqoo
//
//  Created by xiaojuntao on 27/5/2025.
//

import Foundation
import RQCore

extension UIApplication {
    static let appName = ""
    static let appID = ""
    static let appToken = ""

    static var apperanceConfiguration: RQCore.ApperanceConfiguration = {
        return RQCore.ApperanceConfiguration.init(themeColor: R.color.brand()!,
                                                  themeColorHighlight: R.color.brandHighlighted()!,
                                                  themeColorDisable: R.color.brandDisable()!,
                                                  themeColor2: R.color.brand()!,
                                                  themeColor2Highlight: R.color.brandHighlighted()!,
                                                  themeColor2Disable: R.color.brandDisable()!,
                                                  textColor: R.color.text_000000_90()!,
                                                  secondaryTextColor: R.color.text_000000_60()!,
                                                  tertiaryTextColor: R.color.text_000000_38()!,
                                                  linkTextColor: R.color.text_link_4280EF()!,
                                                  maskBackgroundColor: R.color.background_000000_40()!,
                                                  hudBackgroundColor: R.color.text_000000_75()!)
    }()
}
