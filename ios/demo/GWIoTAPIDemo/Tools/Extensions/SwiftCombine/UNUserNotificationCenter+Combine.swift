//
//  UNUserNotificationCenter+Rx.swift
//  Reoqoo
//
//  Created by xiaojuntao on 19/9/2023.
//

import Foundation

extension UNUserNotificationCenter {
    public static func getNotificationSettingsPublisher() -> AnyPublisher<UNNotificationSettings, Never> {
        Deferred {
            Future<UNNotificationSettings, Never> { promise in
                UNUserNotificationCenter.current().getNotificationSettings { settings in
                    promise(.success(settings))
                }
            }
        }
        .receive(on: DispatchQueue.main)
        .eraseToAnyPublisher()
    }
}
