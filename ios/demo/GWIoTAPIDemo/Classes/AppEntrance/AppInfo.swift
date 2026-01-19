//
//  AppInfo.swift
//  Reoqoo
//
//  Created by xiaojuntao on 27/5/2025.
//

import Foundation
import GWIoTApi

extension UIApplication {
    static let appID = ""
    static let appToken = ""

    static var apperanceConfiguration: GWIoTApi.UIConfiguration = {
        let icons = GWIoTApi.Theme.Icons(
            app: nil,
            accountSharedIcon: nil
        )
        
        let colors = GWIoTApi.Theme.Colors(brand: "#279d44", brandHighlight: "#238b3c", brandDisable: "#dfe1e7", brand2: "#279d44", brand2Highlight: "#238b3c", brand2Disable: "#dfe1e7", text: nil, secondaryText: nil, tertiaryText: nil, lightText: nil, linkText: nil, maskBackground: nil, hudBackground: nil, inputLineDisable: nil, inputLineEnable: nil, separatorLine: nil, mainBackground: nil, secondaryBackground: nil, stateSafe: nil, stateWarning: nil, stateError: nil)


        return .init(
            theme: Theme(colors: colors, icons: nil),
            texts: AppTexts(appNamePlaceHolder: "YourApp")
        )

    }()
}
