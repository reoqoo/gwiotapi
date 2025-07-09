//
//  RegionSelectionViewController.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 24/7/2023.
//

import UIKit
import RQCore

class RegionSelectionViewController: BaseViewController, ScrollBaseViewAndKeyboardMatchable {

    // ScrollBaseViewAndKeyboardMatchable
    var scrollable: UIScrollView { self.tableView }
    var anyCancellables: Set<AnyCancellable> = []

    let vm: RegionSelectionViewModel = .init()

    lazy var introduceLabel: UILabel = .init().then {
        $0.font = UIFont.systemFont(ofSize: 12, weight: .regular)
        $0.textColor = .black
        $0.numberOfLines = 0
        $0.text = String.localization.localized("AA0015", note: "您的数据将存储在注册地的服务器上")
    }

    lazy var filterTextField: UITextField = .init().then {
        $0.returnKeyType = .done
        $0.delegate = self
        $0.placeholder = String.localization.localized("AA0014", note: "国家/地区")
        $0.backgroundColor = .systemGroupedBackground
        $0.layer.cornerRadius = 21
        $0.layer.masksToBounds = true
        $0.clearButtonMode = .whileEditing
        $0.leftViewMode = .always
        $0.leftView = UIButton.init(type: .custom).then {
            $0.isUserInteractionEnabled = false
            $0.setImage(R.image.commonSearch()!, for: .normal)
            $0.contentEdgeInsets = .init(top: 0, left: 12, bottom: 0, right: 8)
        }
    }

    lazy var currentSelectedCountryNameLabel: UILabel = .init().then {
        $0.font = .systemFont(ofSize: 16, weight: .semibold)
        $0.textColor = R.color.text_link_4A68A6()
    }

    lazy var currentSelectedCountryCodeLabel: UILabel = .init().then {
        $0.font = .systemFont(ofSize: 14, weight: .semibold)
        $0.textColor = R.color.text_link_4A68A6()
    }

    // currentSelectedCountryNameLabel + currentSelectedCountryCodeLabel
    lazy var currentSelectedStateContainer: UIView = .init().then {
        $0.addSubview(self.currentSelectedCountryNameLabel)
        self.currentSelectedCountryNameLabel.snp.makeConstraints { make in
            make.top.bottom.leading.equalToSuperview()
        }
        $0.addSubview(self.currentSelectedCountryCodeLabel)
        self.currentSelectedCountryCodeLabel.snp.makeConstraints { make in
            make.top.bottom.trailing.equalToSuperview()
        }
    }

    lazy var tableView: UITableView = .init(frame: .zero, style: .plain).then {
        $0.keyboardDismissMode = .onDrag
        $0.delegate = self
        $0.dataSource = self
        $0.emptyDataSetSource = self
        $0.emptyDataSetDelegate = self
        $0.separatorInset = .init(top: 0, left: 28, bottom: 0, right: 28)
        $0.separatorColor = R.color.lineSeparator()
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.title = String.localization.localized("AA0014", note: "国家/地区")

        self.rq.setNavigationBarBackground(R.color.background_FFFFFF_white()!)
        self.view.backgroundColor = R.color.background_FFFFFF_white()
        
        self.view.addSubview(self.introduceLabel)
        self.introduceLabel.snp.makeConstraints { make in
            make.top.equalTo(self.view.safeAreaLayoutGuide.snp.top).offset(24)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
        }

        self.view.addSubview(self.filterTextField)
        self.filterTextField.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.top.equalTo(self.introduceLabel.snp.bottom).offset(24)
            make.height.equalTo(42)
        }

        self.view.addSubview(self.currentSelectedStateContainer)
        self.currentSelectedStateContainer.snp.makeConstraints { make in
            make.top.equalTo(self.filterTextField.snp.bottom).offset(24)
            make.leading.equalToSuperview().offset(28)
            make.trailing.equalToSuperview().offset(-28)
            make.height.equalTo(44)
        }

        self.view.addSubview(self.tableView)
        self.tableView.snp.makeConstraints { make in
            make.top.equalTo(self.currentSelectedStateContainer.snp.bottom).offset(12)
            make.leading.trailing.bottom.equalToSuperview()
        }

        self.tableView.register(RegionSelectionViewController.TableViewCell.self, forCellReuseIdentifier: String.init(describing: RegionSelectionViewController.TableViewCell.self))

        // 当键盘出现时调整 tableView 的 contentInset.bottom
        self.adjustScrollViewContentInsetWhenKeyboardFrameChanged()

        // 对当前选中的地区进行监听
        RegionInfoProvider.shared.$selectedRegion.map({ $0.countryName }).sink(receiveValue: { [weak self] countryName in
            self?.currentSelectedCountryNameLabel.text = countryName
        }).store(in: &self.anyCancellables)

        RegionInfoProvider.shared.$selectedRegion.map({ "+" + $0.countryCode }).sink(receiveValue: { [weak self] countryCode in
            self?.currentSelectedCountryCodeLabel.text = countryCode
        }).store(in: &self.anyCancellables)

        // 使 vm 监听 filterTextField 输入事件. debounce / throttle 以节约性能
        self.filterTextField.textPublisher
            .debounce(for: 0.3, scheduler: DispatchQueue.main)
            .throttle(for: 0.3, scheduler: DispatchQueue.main, latest: true)
            .sink { [weak self] text in
                self?.vm.searchKeyword = text
            }.store(in: &self.anyCancellables)

        // 监听 vm.$dataSource 发布者以更新 tableView
        self.vm.$dataSource.sink(receiveValue: { [weak self] regionInfos in
            self?.tableView.reloadData()
        }).store(in: &self.anyCancellables)
    }

}

extension RegionSelectionViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        self.view.endEditing(true)
        return true
    }
}

extension RegionSelectionViewController: UITableViewDataSource, UITableViewDelegate {

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.vm.dataSource.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: RegionSelectionViewController.TableViewCell.self), for: indexPath)
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        RegionInfoProvider.shared.selectedRegion = self.vm.dataSource[indexPath.row]
        self.navigationController?.popViewController(animated: true)
    }
    
    func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        guard let cell = cell as? RegionSelectionViewController.TableViewCell else { return }
        cell.countryInfo = self.vm.dataSource[indexPath.row]
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat { 56 }

}

extension RegionSelectionViewController: EmptyDataSetSource, EmptyDataSetDelegate {
    // TODO: 数据为空占位
}
