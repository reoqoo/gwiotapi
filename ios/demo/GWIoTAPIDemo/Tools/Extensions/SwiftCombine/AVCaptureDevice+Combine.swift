//
//  AVCaptureDevice+Combine.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 18/6/2024.
//

import Foundation

extension AVCaptureDevice {

    public static func authorizationRequestPublisher(_ type: AVMediaType) -> AnyPublisher<AVAuthorizationStatus, Swift.Error> {
        Future.init({ promise in
            AVCaptureDevice.requestAccess(for: type) { grated in
                promise(.success(Self.authorizationStatus(for: type)))
            }
        }).eraseToAnyPublisher()
    }

}
