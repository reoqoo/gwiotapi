//
//  PasswordSettingSucceedViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 31/7/2023.
//

import UIKit

class PasswordSettingSucceedViewController: BaseViewController {

    lazy var succeedLogoImageView: UIImageView = .init(image: R.image.commonOperationResultSucceed())

    lazy var succeedDescriptionLabel: UILabel = .init().then {
        $0.font = .systemFont(ofSize: 14)
        $0.textColor = R.color.text_000000_90()!
        $0.text = String.localization.localized("AA0037", note: "密码修改成功")
    }

    private lazy var go2LoginBtn: UIButton = .init(type: .custom).then {
        $0.setStyle_0()
        $0.titleLabel?.font = .systemFont(ofSize: 16)
        $0.setTitleColor(.white, for: .normal)
        $0.setTitle(String.localization.localized("AA0038", note: "去登录"), for: .normal)
        $0.layer.cornerRadius = 23
        $0.layer.masksToBounds = true
    }

    var anyCancellables: Set<AnyCancellable> = []

    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.setNavigationBarBackground(R.color.background_FFFFFF_white()!)
        self.view.backgroundColor = R.color.background_FFFFFF_white()
        
        self.view.addSubview(self.succeedLogoImageView)
        self.succeedLogoImageView.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(55)
            make.centerX.equalToSuperview()
            make.width.height.equalTo(84)
        }

        self.view.addSubview(self.succeedDescriptionLabel)
        self.succeedDescriptionLabel.snp.makeConstraints { make in
            make.top.equalTo(self.succeedLogoImageView.snp.bottom).offset(24)
            make.centerX.equalToSuperview()
        }

        self.view.addSubview(self.go2LoginBtn)
        self.go2LoginBtn.snp.makeConstraints { make in
            make.top.equalTo(self.succeedDescriptionLabel.snp.bottom).offset(84)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.height.equalTo(46)
        }

        self.go2LoginBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
            self?.navigationController?.popToRootViewController(animated: true)
        }).store(in: &self.anyCancellables)
    }

}
