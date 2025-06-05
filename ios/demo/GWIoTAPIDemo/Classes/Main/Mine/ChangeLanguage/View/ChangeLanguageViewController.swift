//
//  ChangeLanguageViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 11/9/2023.
//

import UIKit

extension ChangeLanguageViewController {

    class TableViewCell: UITableViewCell {

        var item: TableViewCellItem? {
            didSet {
                self.label.text = self.item?.description
            }
        }

        lazy var tick: UIImageView = .init().then {
            $0.image = R.image.commonTick()
            $0.isHidden = true
        }

        lazy var label: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 14)
            $0.textColor = R.color.text_000000_90()
        }

        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
            super.init(style: style, reuseIdentifier: reuseIdentifier)

            self.contentView.backgroundColor = R.color.background_FFFFFF_white()

            self.contentView.addSubview(self.label)
            self.label.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(12)
                make.centerY.equalToSuperview()
            }

            self.contentView.addSubview(self.tick)
            self.tick.snp.makeConstraints { make in
                make.trailing.equalToSuperview().offset(-12)
                make.centerY.equalToSuperview()
            }

            self.selectionStyle = .none
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

        override func setSelected(_ selected: Bool, animated: Bool) {
            super.setSelected(selected, animated: animated)
            self.tick.isHidden = !selected
        }
    }
}

class ChangeLanguageViewController: BaseViewController {

    let vm: ViewModel = .init()

    lazy var tableView: UITableView = .init(frame: .zero, style: .insetGrouped).then {
        $0.rowHeight = 56
        $0.delegate = self
        $0.dataSource = self
        $0.backgroundColor = .clear
        $0.showsVerticalScrollIndicator = false
        $0.separatorColor = R.color.text_000000_10()
        $0.separatorInset = .init(top: 0, left: 12, bottom: 0, right: 12)
        $0.register(TableViewCell.self, forCellReuseIdentifier: String.init(describing: TableViewCell.self))
    }

    lazy var saveBarButtonItem: UIBarButtonItem = .init(title: String.localization.localized("AA0273", note: "保存"), style: .plain, target: nil, action: nil).then {
        $0.isEnabled = true
    }

    var anyCancellables: Set<AnyCancellable> = []

    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        self.title = String.localization.localized("AA0221", note: "语言")
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        self.title = String.localization.localized("AA0221", note: "语言")
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        self.view.addSubview(self.tableView)
        self.tableView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        // 保存按钮点击
        self.saveBarButtonItem.tapPublisher.sink(receiveValue: { [weak self] _ in
            guard let indexPath = self?.tableView.indexPathForSelectedRow else { return }
            self?.vm.changeLanguage(at: indexPath.row)
        }).store(in: &self.anyCancellables)

        // 主动选中当前选择的语言
        self.tableView.selectRow(at: IndexPath.init(row: self.vm.selectedRow, section: 0), animated: false, scrollPosition: .none)
    }

}

extension ChangeLanguageViewController: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.vm.dataSource.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: TableViewCell.self), for: indexPath) as! TableViewCell
        cell.item = self.vm.dataSource[indexPath.row]
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        if indexPath.row == self.vm.selectedRow {
            self.navigationItem.rightBarButtonItem = nil
        }else{
            self.navigationItem.rightBarButtonItem = self.saveBarButtonItem
        }
    }
}
