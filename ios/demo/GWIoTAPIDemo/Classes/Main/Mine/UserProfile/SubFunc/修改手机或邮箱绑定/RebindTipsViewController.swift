//
//  RebindTipsViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 15/9/2023.
//

import UIKit

extension RebindTipsViewController {
    enum BindType {
        case changeEmailBind
        case changeTelephoneBind

        var title: String {
            switch self {
            case .changeEmailBind:
                return String.localization.localized("AA0581", note: "绑定邮箱")
            case .changeTelephoneBind:
                return String.localization.localized("AA0580", note: "绑定手机")
            }
        }

        var rebindButtonTitle: String {
            switch self {
            case .changeEmailBind:
                return String.localization.localized("AA0304", note: "更换邮箱")
            case .changeTelephoneBind:
                return String.localization.localized("AA0298", note: "更换手机号")
            }
        }

        var iconImage: UIImage {
            switch self {
            case .changeEmailBind:
                return R.image.mineChangeBindingEmail()!
            case .changeTelephoneBind:
                return R.image.mineChangeBindingTelephone()!
            }
        }

        var descriptionLabelContent: String {
            switch self {
            case .changeEmailBind:
                return String.localization.localized("AA0297", note: "已绑定：") + (AccountCenter.shared.currentUser?.profileInfo?.email ?? "")
            case .changeTelephoneBind:
                return String.localization.localized("AA0297", note: "已绑定：") + (AccountCenter.shared.currentUser?.profileInfo?.mobile ?? "")
            }
        }
    }
}

/// 当用户已绑定 手机/邮箱 提示一下用户是否需要更换绑定的页面
class RebindTipsViewController: BaseViewController {

    let bindType: BindType
    init(bindType: BindType) {
        self.bindType = bindType
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    lazy var contentView: UIView = .init().then {
        $0.backgroundColor = R.color.background_FFFFFF_white()
        $0.layer.cornerRadius = 12
        $0.layer.masksToBounds = true
    }

    lazy var iconImageView: UIImageView = .init(image: self.bindType.iconImage)

    lazy var descriptionLabel: UILabel = .init().then {
        $0.textAlignment = .center
        $0.font = .systemFont(ofSize: 16)
        $0.textColor = R.color.text_000000_90()
        $0.numberOfLines = 0
    }

    lazy var changeBindingButton: UIButton = .init(type: .custom).then {
        $0.setStyle_0()
        $0.layer.cornerRadius = 23
        $0.layer.masksToBounds = true
        $0.titleLabel?.font = .systemFont(ofSize: 16)
        $0.setTitle(self.bindType.rebindButtonTitle, for: .normal)
        $0.setTitleColor(R.color.text_FFFFFF(), for: .normal)
    }

    var anyCancellables: Set<AnyCancellable> = []

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = self.bindType.title

        self.view.addSubview(self.contentView)
        self.contentView.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(24)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
        }

        self.contentView.addSubview(self.iconImageView)
        self.iconImageView.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(40)
            make.centerX.equalToSuperview()
        }

        self.contentView.addSubview(self.descriptionLabel)
        self.descriptionLabel.snp.makeConstraints { make in
            make.top.equalTo(self.iconImageView.snp.bottom).offset(24)
            make.leading.trailing.equalToSuperview()
        }

        self.contentView.addSubview(self.changeBindingButton)
        self.changeBindingButton.snp.makeConstraints { make in
            make.top.equalTo(self.descriptionLabel.snp.bottom).offset(40)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
            make.bottom.equalToSuperview().offset(-40)
            make.height.equalTo(46)
        }
        
        self.descriptionLabel.text = self.bindType.descriptionLabelContent

        self.changeBindingButton.tapPublisher
            .sink(receiveValue: { [weak self] _ in
                let bindType: RequestOneTimeCodeForBindingViewController.BindType = self?.bindType == .changeEmailBind ? .changeEmail : .changeTelephone
                let vc = RequestOneTimeCodeForBindingViewController(bindType: bindType)
                self?.navigationController?.pushViewController(vc, animated: true)
            }).store(in: &self.anyCancellables)
    }

}
