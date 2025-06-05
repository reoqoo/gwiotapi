//
//  CloseAccountIntroductionViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 15/9/2023.
//

import UIKit

class CloseAccountIntroductionViewController: BaseViewController {

    let flowItem: CloseAccountReasonSelectionViewController.CloseAccountFlowItem
    init(flowItem: CloseAccountReasonSelectionViewController.CloseAccountFlowItem) {
        self.flowItem = flowItem
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    let contents: [String] = {
        let contents: [String] = [
            String.localization.localized("AA0319", note: "您保存在云端的全部个人资料和历史信息将无法找回；"),
            String.localization.localized("AA0320", note: "您的购买记录都将被清空，且无法恢复，不予退款；"),
            String.localization.localized("AA0321", note: "任何您之前累计的等级、积分、权益等都将作废且无法恢复；"),
            String.localization.localized("AA0322", note: "您将无法使用绑定的手机号/邮箱，以及第三方账户继续登录；"),
            String.localization.localized("AA0323", note: "您绑定的设备将全部解绑。如需继续使用，请注册后重新添加。"),
        ]
        return contents.map { "•" + $0 }
    }()

    lazy var scrollView: UIScrollView = .init().then {
        $0.showsVerticalScrollIndicator = false
        $0.showsHorizontalScrollIndicator = false
        $0.alwaysBounceVertical = true
    }

    lazy var contentLabels: [UILabel] = []

    lazy var stackView: UIStackView = .init().then {
        $0.axis = .vertical
        $0.spacing = 16
    }

    lazy var contentView: UIView = .init().then {
        $0.backgroundColor = R.color.background_FFFFFF_white()
        $0.layer.cornerRadius = 12
        $0.layer.masksToBounds = true
    }

    lazy var iconImageView: UIImageView = .init(image: R.image.mineCloseAccountWarning())

    lazy var wranningLabel: UILabel = .init().then {
        $0.text = String.localization.localized("AA0318", note: "注销后，将放弃以下资产和权益")
        $0.textAlignment = .center
        $0.textColor = R.color.text_FF582A()!
        $0.font = .systemFont(ofSize: 18, weight: .medium)
        $0.numberOfLines = 0
    }

    lazy var nextStepButton: UIButton = .init(type: .custom).then {
        $0.setStyle_0()
        $0.layer.cornerRadius = 23
        $0.layer.masksToBounds = true
        $0.titleLabel?.font = .systemFont(ofSize: 16)
        $0.setTitle(String.localization.localized("AA0377", note: "下一步"), for: .normal)
        $0.setTitleColor(R.color.text_FFFFFF(), for: .normal)
    }

    var anyCancellables: Set<AnyCancellable> = []

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = String.localization.localized("AA0282", note: "注销账号")
        
        self.view.addSubview(self.nextStepButton)
        self.nextStepButton.snp.makeConstraints { make in
            make.bottom.equalTo(self.view.safeAreaLayoutGuide.snp.bottom).offset(-16)
            make.leading.equalToSuperview().offset(16)
            make.trailing.equalToSuperview().offset(-16)
            make.height.equalTo(46)
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
            make.trailing.equalToSuperview().offset(-12)
            make.leading.equalToSuperview().offset(12)
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
            label.text = $0
            label.font = .systemFont(ofSize: 14)
            label.textColor = R.color.text_000000_90()
            label.numberOfLines = 0
            self.stackView.addArrangedSubview(label)
        }

        self.nextStepButton.tapPublisher
            .sink(receiveValue: { [weak self] _ in
                guard let flowItem = self?.flowItem else { return }
                let vc = CloseAccountPasswordInputViewController.init(flowItem: flowItem)
                self?.navigationController?.pushViewController(vc, animated: true)
            }).store(in: &self.anyCancellables)
    }
    
}
