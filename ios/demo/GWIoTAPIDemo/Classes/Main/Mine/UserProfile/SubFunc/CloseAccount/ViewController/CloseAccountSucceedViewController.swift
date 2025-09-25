//
//  CloseAccountSucceedViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 15/9/2023.
//

import UIKit
import RQCore
import GWIoTApi

class CloseAccountSucceedViewController: BaseViewController {

    var anyCancellables: Set<AnyCancellable> = []

    lazy var scrollView: UIScrollView = .init().then {
        $0.showsVerticalScrollIndicator = false
        $0.showsHorizontalScrollIndicator = false
        $0.alwaysBounceVertical = true
    }

    lazy var contentView: UIView = .init().then {
        $0.backgroundColor = R.color.background_FFFFFF_white()
        $0.layer.cornerRadius = 12
        $0.layer.masksToBounds = true
    }

    lazy var iconImageView: UIImageView = .init(image: R.image.mineCloseAccountSucceed())
    
    lazy var wranningLabel: UILabel = .init().then {
        $0.text = String.localization.localized("AA0330", note: "注销账户成功！感谢您的使用")
        $0.textColor = R.color.text_000000_90()
        $0.font = .systemFont(ofSize: 14)
        $0.textAlignment = .center
        $0.numberOfLines = 0
    }

    lazy var nextStepButton: UIButton = .init(type: .custom).then {
        $0.setStyle_0()
        $0.layer.cornerRadius = 23
        $0.layer.masksToBounds = true
        $0.titleLabel?.font = .systemFont(ofSize: 16)
        $0.setTitle(String.localization.localized("AA0334", note: "我知道了"), for: .normal)
        $0.setTitleColor(R.color.text_FFFFFF(), for: .normal)
    }

    lazy var stackView: UIStackView = .init().then {
        $0.axis = .vertical
        $0.spacing = 16
    }

    let contents: [NSMutableAttributedString] = {
        let title = String.localization.localized("AA0331", note: "注销后您需要知道的：")
        let detail_0 = "•" + String.localization.localized("AA0332", note: "无法使用原帐户绑定的手机号/邮箱，以及第三方帐号继续登录")
        let detail_1 = "•" + String.localization.localized("AA0333", note: "原账户绑定的设备已全部解绑。如需继续使用，请注册后重新添加")
        let title_attributedString = NSMutableAttributedString.init(string: title)
        let detail_0_attributedString = NSMutableAttributedString.init(string: detail_0)
        let detail_1_attributedString = NSMutableAttributedString.init(string: detail_1)
        title_attributedString.setAttributes([.foregroundColor: R.color.text_000000_90()!, .font: UIFont.systemFont(ofSize: 16, weight: .medium)], range: .init(location: 0, length: title_attributedString.length))
        detail_0_attributedString.setAttributes([.foregroundColor: R.color.text_000000_90()!, .font: UIFont.systemFont(ofSize: 16)], range: NSRange.init(location: 0, length: detail_0_attributedString.length))
        detail_1_attributedString.setAttributes([.foregroundColor: R.color.text_000000_90()!, .font: UIFont.systemFont(ofSize: 16)], range: NSRange.init(location: 0, length: detail_1_attributedString.length))
        return [title_attributedString, detail_0_attributedString, detail_1_attributedString]
    }()

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = String.localization.localized("AA0282", note: "注销账号")

        self.view.addSubview(self.nextStepButton)
        self.nextStepButton.snp.makeConstraints { make in
            make.height.equalTo(46)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom).offset(-12)
        }

        self.view.addSubview(self.scrollView)
        self.scrollView.snp.makeConstraints { make in
            make.top.leading.trailing.equalToSuperview()
            make.bottom.equalTo(self.nextStepButton.snp.top).offset(-8)
        }

        self.scrollView.addSubview(self.contentView)
        self.contentView.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(24)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
            make.width.equalTo(self.view.snp.width).offset(-32)
        }

        self.contentView.addSubview(self.iconImageView)
        self.iconImageView.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(40)
            make.centerX.equalToSuperview()
        }

        self.contentView.addSubview(self.wranningLabel)
        self.wranningLabel.snp.makeConstraints { make in
            make.top.equalTo(self.iconImageView.snp.bottom).offset(24)
            make.leading.equalToSuperview().offset(12)
            make.trailing.equalToSuperview().offset(-12)
            make.bottom.equalToSuperview().offset(-40)
        }

        self.scrollView.addSubview(self.stackView)
        self.stackView.snp.makeConstraints { make in
            make.top.equalTo(self.contentView.snp.bottom).offset(30)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
            make.bottom.equalToSuperview().offset(-16)
        }

        self.contents.forEach {
            let label = UILabel.init()
            label.attributedText = $0
            label.numberOfLines = 0
            self.stackView.addArrangedSubview(label)
        }

        self.nextStepButton.tapPublisher.sink(receiveValue: { _ in
            NotificationCenter.default.post(name: User.accountHasBeenDeletedNotification, object: nil, userInfo: nil)
        }).store(in: &self.anyCancellables)
    }
    
}
