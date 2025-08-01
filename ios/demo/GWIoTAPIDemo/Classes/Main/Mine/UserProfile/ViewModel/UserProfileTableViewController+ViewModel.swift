//
//  UserProfileTableViewController+ViewModel.swift
//  Reoqoo
//
//  Created by xiaojuntao on 14/9/2023.
//

import Foundation
import IVAccountMgr
import RQCore

extension UserProfileTableViewController.ViewModel {

    enum Status {
        case idle
        case didCompleteModifyUserInfo(Result<Void, Swift.Error>)
    }

    enum Event {
        case modifyUserInfo(header: String?, nick: String?, oldPassword: String?, newPassword: String?)
    }
}

extension UserProfileTableViewController {

    class ViewModel {

        @DidSetPublished var status: Status = .idle

        var anyCancellables: Set<AnyCancellable> = []

        func processEvent(_ event: Event) {
            switch event {
            case let .modifyUserInfo(header, nick, oldPassword, newPassword):
                // 检查 昵称 格式是否正确
                AccountCenter.shared.currentUser?.modifyUserInfoPublisher(header: header, nick: nick, oldPassword: oldPassword, newPassword: newPassword)
                    .sink(receiveCompletion: { [weak self] in
                        guard case var .failure(err) = $0 else { return }
                        if (err as NSError).code == 10019 {
                            err = (ReoqooError.accountError(reason: .nickNameContainInvalidCharacter))
                        }
                        self?.status = .didCompleteModifyUserInfo(.failure(err))
                    }, receiveValue: { [weak self] profileInfo in
                        self?.status = .didCompleteModifyUserInfo(.success(()))
                    }).store(in: &self.anyCancellables)
            }
        }

        // MARK: 发布者封装

    }
}
