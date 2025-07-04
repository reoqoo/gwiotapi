//
//  AppInfo.swift
//  Reoqoo
//
//  Created by xiaojuntao on 27/5/2025.
//

import Foundation
import GWIoTApi

extension UIApplication {
    static let appName = ""
    static let appID = ""
    static let appToken = ""
    static let cid = ""

    static var apperanceConfiguration: GWIoTApi.UIConfiguration = {
        let icons = GWIoTApi.Theme.Icons(
            app: nil,
            accountSharedIcon: nil
        )

        return .init(
            theme: Theme(colors: nil, icons: nil),
            texts: AppTexts(appNamePlaceHolder: "Defender ClearVu")
        )

    }()
}
