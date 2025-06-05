//
//  ViewModel.swift
//  Reoqoo
//
//  Created by xiaojuntao on 12/9/2023.
//

import Foundation
import GWIoTApi

extension ChangeLanguageViewController {

    enum TableViewCellItem: CustomStringConvertible, Equatable {
        case baseOnSystem
        case assign(GWIoTApi.LanguageCode)

        var description: String {
            switch self {
            case .baseOnSystem:
                return String.localization.localized("AA0272", note: "跟随系统语言")
            case let .assign(langCode):
                return langCode.translate
            }
        }

        static func == (lhs: Self, rhs: Self) -> Bool {
            if case .baseOnSystem = lhs, case .baseOnSystem = rhs {
                return true
            }

            if case let .assign(langCode_lhs) = lhs, case let .assign(langCode_rhs) = rhs {
                return langCode_lhs.apiValue == langCode_rhs.apiValue
            }

            return false
        }

        static func from(langCode: LanguageCode?) -> Self {
            guard let langCode = langCode else { return .baseOnSystem }
            return .assign(langCode)
        }
    }

    class ViewModel {

        lazy var selectedRow: Int = {
            guard let assignLang = LanguageCode.assignedLanguageCode else {
                return 0
            }
            let code = LanguageCode.from(nanoCode2: assignLang)
            let item = TableViewCellItem.from(langCode: code)
            return self.dataSource.firstIndex(where: { item == $0 }) ?? 0
        }()

        let dataSource: [TableViewCellItem] = [.baseOnSystem, .assign(.zhHans), .assign(.zhHant), .assign(.en), .assign(.th), .assign(.vi), .assign(.ja), .assign(.ko), .assign(.id), .assign(.ms)]

        func changeLanguage(at: Int) {
            let item = self.dataSource[at]
            if case .baseOnSystem = item {
                LanguageCode.assignedLanguageCode = nil
            }
            if case let .assign(ivCode) = item {
                LanguageCode.assignedLanguageCode = ivCode.isoCode
            }
            let gwLanguageCode = LanguageCode.current
            GWIoT.shared.setLanguage(code: gwLanguageCode)
        }
    }

}
