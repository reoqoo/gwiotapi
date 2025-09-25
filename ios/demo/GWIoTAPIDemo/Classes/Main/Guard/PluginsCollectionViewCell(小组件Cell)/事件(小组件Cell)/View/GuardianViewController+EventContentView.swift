//
//  GuardianViewController+EventListTableViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 30/8/2023.
//

import Foundation
import MJRefresh
import RQCore
import RQCoreUI

extension GuardianViewController {
    /// 显示内容:
    ///     - "最近7天" "更多按钮"
    ///     - 事件列表
    ///     - 查看更多
    class EventContentView: PluginCollectionViewCellContent {

        enum ControlEvent {
            /// "更多按钮" 点击
            case moreBtnClicked
            /// "折叠按钮" 点击
            case flodBtnClicked
            /// "展开按钮" 点击
            case expandBtnClicked
            /// "事件Cell" 点击
            case eventCellClicked(event: GuardianViewController.Event)
            /// "事件列表刷新完毕"
            case eventDidRefreshComplete
        }

        var controlEventObservable: Combine.PassthroughSubject<ControlEvent, Never> = .init()
        
        private let vm: EventCollectionCell.ViewModel
        
        /// "事件"
        private lazy var dateFilterLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 14)
            $0.textColor = R.color.text_000000_90()
            $0.text = String.localization.localized("AA0207", note: "事件")
        }

        private lazy var moreBtn: UIButton = .init(type: .system).then {
            $0.setImage(R.image.guardMore(), for: .normal)
            $0.tintColor = R.color.text_000000_90()!
        }

        private(set) lazy var topContainer: UIView = .init().then {
            $0.backgroundColor = .clear
        }

        /// "调整可见范围按钮"
        /// 当 vm 中的过滤条件非空时, 便展示此footer
        private(set) lazy var tableFooterViewBtn: UIButton = .init(type: .system).then {
            $0.tintColor = R.color.text_link_4A68A6()
            $0.setTitle(String.localization.localized("AA0205", note: "调整可见范围"), for: .normal)
            $0.titleLabel?.font = .systemFont(ofSize: 12)
        }
        private(set) lazy var tableFooterView: UIView = .init().then {
            $0.backgroundColor = R.color.background_FFFFFF_white()
            $0.addSubview(self.tableFooterViewBtn)
            self.tableFooterViewBtn.snp.makeConstraints { make in
                make.height.equalTo(46)
                make.top.equalToSuperview().offset(16)
                make.leading.trailing.bottom.equalToSuperview()
            }
            $0.frame = .init(x: 0, y: 0, width: 0, height: 62)
        }

        private(set) lazy var tableView: UITableView = .init(frame: .zero, style: .grouped).then {
            $0.delegate = self
            $0.dataSource = self
            $0.showsVerticalScrollIndicator = false
            $0.sectionHeaderHeight = 0.1
            $0.sectionFooterHeight = 0.1
            $0.tableFooterView = self.tableFooterView
            $0.tableHeaderView = .init(frame: .zero)
            $0.backgroundColor = R.color.background_FFFFFF_white()
            $0.separatorStyle = .none
            $0.register(EventTableViewCellStyle0.self, forCellReuseIdentifier: String.init(describing: EventTableViewCellStyle0.self))
            $0.register(EventTableViewCellStyle1.self, forCellReuseIdentifier: String.init(describing: EventTableViewCellStyle1.self))
            $0.register(EventTableViewHeader.self, forHeaderFooterViewReuseIdentifier: String.init(describing: EventTableViewHeader.self))
        }

        /// 查看更多按钮
        private(set) lazy var viewMoreBtn: UIButton = .init(type: .system).then {
            $0.tintColor = R.color.text_link_4A68A6()!
            $0.titleLabel?.font = .systemFont(ofSize: 14)
            $0.setTitle(String.localization.localized("AA0204", note: "查看更多"), for: .normal)
        }

        /// 空内容占位
        /// 此占位视图不能用 EmptyDataSet 展示了, 因为 EmptyDataSete SDK 会在不适当的时机重置 TableView 的 isScrollEnable 状态
        lazy var emptyEventPlaceholder: UIView = .init().then {
            let imageview = UIImageView.init(image: R.image.guardNoneEventPlaceholder())
            $0.backgroundColor = R.color.background_FFFFFF_white()
            $0.addSubview(imageview)
            imageview.snp.makeConstraints { make in
                make.top.leading.trailing.equalToSuperview()
            }

            let label = UILabel.init()
            label.text = String.localization.localized("AA0203", note: "这一天安安静静，什么情况也没有~")
            label.font = .systemFont(ofSize: 12)
            label.textColor = R.color.text_000000_60()
            $0.addSubview(label)
            label.snp.makeConstraints { make in
                make.top.equalTo(imageview.snp.bottom).offset(8)
                make.centerX.equalToSuperview()
                make.bottom.equalToSuperview()
            }

            $0.isHidden = true
        }
        
        /// tableView 数据源
        /// 在 self.vm.tableViewDataSources 发生变化时, 此属性会被赋值
        lazy var tableViewDataSources: [(TimeInterval, [GuardianViewController.Event])] = []

        override var expandable: GuardianViewController.PluginCollectionViewCell.Expandable {
            didSet {
                var isExpanded = false
                if case let .yes(flag) = expandable, flag { isExpanded = true }
                // 当展开时,禁止 contentView 上的用户操作交互
                self.tableView.isScrollEnabled = isExpanded
                // 展开后, 隐藏底部 "查看更多"(展开按钮)
                self.bottomArea.isHidden = isExpanded
                // 折叠后, tableview 滚回顶部
                if !isExpanded {
                    self.tableView.contentOffset = .zero
                }
            }
        }

        private var anyCancellables: Set<AnyCancellable> = []

        init(vm: EventCollectionCell.ViewModel) {
            self.vm = vm
            super.init(frame: .zero)

            self.backgroundColor = R.color.background_FFFFFF_white()

            self.topContainer.addSubview(self.dateFilterLabel)
            self.dateFilterLabel.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(12)
                make.bottom.equalToSuperview().offset(-12)
            }

            self.topContainer.addSubview(self.moreBtn)
            self.moreBtn.snp.makeConstraints { make in
                make.trailing.equalToSuperview().offset(-12)
                make.bottom.equalToSuperview()
                make.width.equalTo(24)
                make.height.equalTo(44)
            }

            self.setTopAccessoryView(self.topContainer)
            self.topContainer.snp.makeConstraints { make in
                make.height.equalTo(50)
            }

            self.contentView.addSubview(self.tableView)
            self.tableView.snp.makeConstraints { make in
                make.edges.equalToSuperview()
            }

            self.contentView.addSubview(self.emptyEventPlaceholder)
            self.emptyEventPlaceholder.snp.makeConstraints { make in
                make.center.equalToSuperview()
            }

            self.setBottomAccessoryView(self.viewMoreBtn)
            self.viewMoreBtn.snp.makeConstraints { make in
                make.top.bottom.equalToSuperview()
                make.centerX.equalToSuperview()
                make.height.equalTo(44)
            }

            self.flodBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
                self?.expandable = .yes(isExpanded: false)
                self?.controlEventObservable.send(.flodBtnClicked)
            }).store(in: &self.anyCancellables)

            self.viewMoreBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
                self?.expandable = .yes(isExpanded: true)
                self?.controlEventObservable.send(.expandBtnClicked)
            }).store(in: &self.anyCancellables)

            self.moreBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
                self?.controlEventObservable.send(.moreBtnClicked)
            }).store(in: &self.anyCancellables)

            self.tableFooterViewBtn.tapPublisher.sink(receiveValue: { [weak self] _ in
                self?.controlEventObservable.send(.moreBtnClicked)
            }).store(in: &self.anyCancellables)

            self.tableView.mj_footer = MJCommonFooter.init(frame: .zero)
            self.tableView.mj_footer?.refreshingBlock = { [weak self] in
                self?.vm.processEvent(.loadMore)
            }

            self.tableView.mj_header = MJCommonHeader.init()
            self.tableView.mj_header?.refreshingBlock = { [weak self] in
                self?.vm.processEvent(.refresh)
            }

            self.tableView.isScrollEnabled = false

            self.vm.$tableViewDataSources.sink(receiveValue: { [weak self] dataSouces in
                self?.tableViewDataSources = dataSouces.sorted { $0.0 > $1.0 }
                self?.tableView.reloadData()
            }).store(in: &self.anyCancellables)

            self.vm.$status.sink(receiveValue: { [weak self] status in
                switch status {
                case .refreshHasMoreDataStatus(let noMoreData, let err):
                    self?.tableView.mj_header?.endRefreshing()
                    if noMoreData {
                        // 重置 mjfooter 状态
                        self?.tableView.mj_footer?.endRefreshingWithNoMoreData()
                    }else{
                        self?.tableView.mj_footer?.endRefreshing()
                    }
                    self?.tableView.performBatchUpdates({})
                    // 更新 controlEventObservable
                    self?.controlEventObservable.send(.eventDidRefreshComplete)
                    // err
                    if let _ = err {
                        MBProgressHUD.showHUD_DispatchOnMainThread(text: String.localization.localized("AA0573", note: "网络异常"))
                    }
                default:
                    break
                }
            }).store(in: &self.anyCancellables)

            // 监听 GuardianViewController MJHeader 刷新事件
            NotificationCenter.default.publisher(for: GuardianViewController.headerRefreshingNotificationName).sink(receiveValue: { [weak self] notification in
                self?.vm.processEvent(.refresh)
            }).store(in: &self.anyCancellables)
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }
}

extension GuardianViewController.EventContentView: UITableViewDataSource, UITableViewDelegate {

    func numberOfSections(in tableView: UITableView) -> Int {
        let res = self.tableViewDataSources.count
        tableView.isHidden = res == 0
        self.emptyEventPlaceholder.isHidden = res != 0
        self.viewMoreBtn.isHidden = self.vm.events.count <= 3
        return res
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let events = self.tableViewDataSources[section].1
        return events.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let events = self.tableViewDataSources[indexPath.section].1
        if self.vm.isSingleDevice {
            let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: EventTableViewCellStyle0.self), for: indexPath) as! EventTableViewCellStyle0
            cell.event = events[indexPath.row]
            return cell
        }else{
            let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: EventTableViewCellStyle1.self), for: indexPath) as! EventTableViewCellStyle1
            cell.event = events[indexPath.row]
            return cell
        }
    }

    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let header = tableView.dequeueReusableHeaderFooterView(withIdentifier: String.init(describing: EventTableViewHeader.self)) as? EventTableViewHeader
        // 如果 section 为 0 且 当前设备只有一个, 显示设备名
        if section == 0 && self.vm.isSingleDevice {
            header?.timeLabel.text = self.vm.devicesFilter.first?.remarkName ?? DeviceManager.shared.devices.first?.remarkName
            return header
        }
        // 如果是 今天 的事件, 就不显示 header 了
        if Date.init(timeIntervalSince1970: self.tableViewDataSources[section].0).isToday() {
            return nil
        }
        // 显示昨天
        header?.timeLabel.text = String.localization.localized("AA0417", note: "昨天")
        return header
    }

    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat { 64 }

    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat { UITableView.automaticDimension }
    
    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat { 0.1 }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let events = self.tableViewDataSources[indexPath.section].1
        let event = events[indexPath.row]
        self.controlEventObservable.send(.eventCellClicked(event: event))
    }
}
