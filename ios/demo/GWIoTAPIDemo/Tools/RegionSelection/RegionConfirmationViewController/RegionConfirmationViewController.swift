//
//  RegionConfirmationViewController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 27/7/2023.
//

import UIKit
import GWIoTApi
import RQCore

/// 确认手机号码区号页面
class RegionConfirmationViewController: BaseViewController {

    lazy var titleLabel: UILabel = {
        let res = UILabel.init()
        res.textColor = R.color.text_000000_90()!
        res.font = .systemFont(ofSize: 26, weight: .medium)
        res.text = String.localization.localized("AA0017", note: "选择您的注册地")
        res.numberOfLines = 0
        return res
    }()

    lazy var introductionLabel: UILabel = {
        let res = UILabel.init()
        res.textColor = R.color.text_000000_60()!
        res.font = .systemFont(ofSize: 14)
        res.text = String.localization.localized("AA0015", note: "您的数据将存储在注册地的服务器上")
        res.numberOfLines = 0
        return res
    }()

    lazy var regionSelectionButton: RegionSelectionButton = .init()

    lazy var confirmButton: UIButton = {
        let res = UIButton.init(type: .custom)
        res.setStyle_0()
        res.layer.cornerRadius = 23
        res.layer.masksToBounds = true
        res.setTitle(String.localization.localized("AA0018", note: "确认"), for: .normal)
        res.addTarget(self, action: #selector(self.confirmBtnOnClick(sender:)), for: .touchUpInside)
        return res
    }()

    var anyCancellables: Set<AnyCancellable> = []

    override func viewDidLoad() {
        super.viewDidLoad()

        self.rq.setNavigationBarBackground(R.color.background_FFFFFF_white()!)
        self.view.backgroundColor = R.color.background_FFFFFF_white()
        
        self.view.addSubview(self.titleLabel)
        self.titleLabel.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(24)
            make.leading.equalToSuperview().offset(28)
        }

        self.view.addSubview(self.introductionLabel)
        self.introductionLabel.snp.makeConstraints { make in
            make.top.equalTo(self.titleLabel.snp.bottom).offset(8)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
        }

        self.view.addSubview(self.regionSelectionButton)
        self.regionSelectionButton.snp.makeConstraints { make in
            make.top.equalTo(self.introductionLabel.snp.bottom).offset(50)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.height.equalTo(56)
        }

        self.view.addSubview(self.confirmButton)
        self.confirmButton.snp.makeConstraints { make in
            make.top.equalTo(self.regionSelectionButton.snp.bottom).offset(45)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.height.equalTo(46)
        }

        RegionInfoProvider.shared.$selectedRegion.map({ $0.countryName }).sink(receiveValue: { [weak self] countryName in
            self?.regionSelectionButton.currentRegionLabel.text = countryName
        }).store(in: &self.anyCancellables)

        self.regionSelectionButton.tapPublisher
            .sink(receiveValue: { [weak self] _ in
                let vc = RegionSelectionViewController.init()
                self?.navigationController?.pushViewController(vc, animated: true)
            }).store(in: &self.anyCancellables)
    }

    @objc func confirmBtnOnClick(sender: UIButton) {
        let selected = RegionInfoProvider.shared.selectedRegion
        Task { [weak self] in
            let hud = MBProgressHUD.showLoadingHUD_DispatchOnMainThread()
            let res = try await GWIoT.shared.querySupportedSmsRegisterCountryCodes()
            hud.hide(animated: true)
            if case let .success(arr) = gwiot_swiftResult(of: res), let supported = arr as? [String] {
                let accountTypes: RequestOneTimeCodeViewController.AccountType = supported.contains(selected.districtCode) ? [.email, .telephone] : [.email]
                let vc = RequestOneTimeCodeViewController.init(accountType: accountTypes)
                self?.navigationController?.pushViewController(vc, animated: true)
            }
        }
    }
}
