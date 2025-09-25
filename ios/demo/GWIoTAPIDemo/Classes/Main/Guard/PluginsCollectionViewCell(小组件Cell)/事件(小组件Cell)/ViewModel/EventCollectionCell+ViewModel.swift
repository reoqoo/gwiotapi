//
//  EventCollectionCell+ViewModel.swift
//  Reoqoo
//
//  Created by xiaojuntao on 30/8/2023.
//

import Foundation
import IVVAS
import RQCore

extension GuardianViewController.EventCollectionCell {
    
    class ViewModel {

        enum Event {
            case loadMore
            case refresh
        }

        enum Status {
            case idle
            // 刷新数据后, 通知视图是否还有更多数据, 以便UI更新上拉加载更多的控件
            case refreshHasMoreDataStatus(noMoreData: Bool, error: Swift.Error?)
        }
        
        /// 事件
        /// 当被修改后, 触发组建 tableViewDataSources 数据
        var events: [GuardianViewController.Event] = []
        /// TableView 数据源
        /// 数据结构: [2023-09-25: [Event]]
        @DidSetPublished var tableViewDataSources: [TimeInterval: [GuardianViewController.Event]] = [:]
        @DidSetPublished var status: Status = .idle

        // MARK: 事件过滤器
        /// 事件过滤
        @DidSetPublished var eventTypesFilter: DITCloudEventType?
        /// 设备过滤
        @DidSetPublished var devicesFilter: [DeviceEntity] = []

        /// 是否为单台设备
        var isSingleDevice: Bool {
            return self.devicesFilter.count == 1 || DeviceManager.shared.devices.count == 1
        }

        /// 时间过滤 (分页)
        typealias TimeFilter = (start: Date, end: Date)
        private var timeFilter: TimeFilter = (Date.yesterday(), Date())
        
        // 记录从 queryEventObservable 接口获取得到的 endTime 和 startTime, 以便 上啦加载更多 时作为参数
        lazy var loadMoreMarker: TimeFilter = self.timeFilter

        /// 计算属性, 快速获取 eventTypeFilter 以及 deviceFilter 两个过滤条件是否为空
        var isFiltersEmpty: Bool {
            self.eventTypesFilter == nil && self.devicesFilter.isEmpty
        }

        private var anyCancellables: Set<AnyCancellable> = []

        func processEvent(_ event: Event) {
            switch event {
            case .loadMore:
                self.loadData(isRefresh: false)
            case .refresh:
                self.loadData(isRefresh: true)
            }
        }

        init() {
            self.$eventTypesFilter.combineLatest(self.$devicesFilter).debounce(for: 0.1, scheduler: DispatchQueue.main).throttle(for: 0.1, scheduler: DispatchQueue.main, latest: true)
                .sink { [weak self] eventTypeFilter, deviceFilter in
                    self?.loadData(isRefresh: true)
                }.store(in: &self.anyCancellables)
        }

        func loadData(isRefresh: Bool) {
            // 时间过滤(分页)配置
            var timeFilter: TimeFilter = (Date.yesterday(), Date())
            if !isRefresh {
                // start time 不变, 只取24小时内的数据
                timeFilter = (self.timeFilter.start, self.loadMoreMarker.end)
                self.timeFilter = timeFilter
            }
            // 设备过滤配置
            let devices: [DeviceEntity] = self.devicesFilter.isEmpty ? DeviceManager.shared.devices : self.devicesFilter

            // 事件类型配置
            let eventTypes: UInt = self.eventTypesFilter?.rawValue ?? 0

            // 检查设备是否具备回访查看功能权限
            let checkPlaybackPermissionObservable = DeviceEntity.getDevicesPlaybackPermissionPublisher(devices)

            let requestPermissionObservable: AnyPublisher<QueryEventResult, Swift.Error> = checkPlaybackPermissionObservable.flatMap { (situations: [(deviceId: String, hasPlaybackPermission: Bool)]) in
                // 取出具备查看回放权限的设备
                let compact_devs = situations.compactMap { $1 ? $0 : nil }
                logInfo("[\(GuardianViewController.self)]", compact_devs)
                // 请求事件发布者
                let queryEventsObserveble = Self.queryEventObservable(deviceIds: compact_devs, startTime: timeFilter.start.timeIntervalSince1970, endTime: timeFilter.end.timeIntervalSince1970, eventTypes: eventTypes)
                return queryEventsObserveble
            }.eraseToAnyPublisher()

            // 发起请求
            requestPermissionObservable.map({
                    // 移除 events 中没有图片的事件
                    let events = $1.filter { !($0.imgUrl?.isEmpty ?? true) }
                    return ($0, events)
                })
            .map({ [weak self] (timeFilter: TimeFilter, events: [GuardianViewController.Event]) -> (TimeFilter, [TimeInterval: [GuardianViewController.Event]], Bool) in
                    if isRefresh {
                        self?.events = []
                    }

                    let isEmpty = events.isEmpty

                    self?.events.append(contentsOf: events)

                    // 将 self.events 拷贝出来
                    let events = self?.events ?? []

                    // 将 events 进行以 day 为单位的分组
                    // 例如 [2023-09-25: [Event]]

                    // 将 days 整理出来
                    let days: [TimeInterval] = events.reduce(into: Set<TimeInterval>(), { partialResult, event in
                        partialResult.insert(event.startTimeDayTimeInterval)
                    }).sorted(by: { $0 > $1 })

                    // 遍历 days, 组建 [TimeInterval: [GuardianViewController.Event]]
                    var time_event_mapping: [TimeInterval: [GuardianViewController.Event]] = [:]
                    days.forEach { dayTimeInterval in
                        // 从 events 中筛选符合条件的 event
                        time_event_mapping[dayTimeInterval] = events.filter { $0.startTimeDayTimeInterval == dayTimeInterval }.sorted(by: { $0.startTime > $1.startTime })
                    }

                    return (timeFilter, time_event_mapping, isEmpty)
            })
            .sink(receiveCompletion: { [weak self] completion in
                guard case let .failure(err) = completion else { return }
                self?.status = .refreshHasMoreDataStatus(noMoreData: false, error: err)
            }, receiveValue: { [weak self] timeFilter, time_event_mapping, isEmpty in
                self?.loadMoreMarker.end = timeFilter.start
                self?.tableViewDataSources = time_event_mapping
                self?.status = .refreshHasMoreDataStatus(noMoreData: isEmpty, error: nil)
            }).store(in: &self.anyCancellables)
        }

        // MARK: 发布者
        typealias QueryEventResult = (timeFilter: TimeFilter, events: [GuardianViewController.Event])
        /// 接口参数 startTIme 和 endTime 说明:
        /// 作用: 分页
        /// 用例:
        ///     - 参数:
        ///         - startTime: 昨日现时
        ///         - endTime: 现时
        ///     - 返回:
        ///         - 最多15条事件
        ///         - startTime: 昨日现时
        ///         - endTime: 15条数据中最新的一条的时间
        ///     - load more 操作
        ///         - startTime 参数不变
        ///         - endTime 以上次返回的 response 中的 startTime 填充
        ///
        /// 接口参数 eventTypes 说明
        /// 如需要过滤出有人活动 或 画面变化的事件,  有人活动为 2, 画面变化为 1, 应该传入按位与的结果, 所以为 3
        /// 如需过滤出 有人活动 且 画面变化的事件, 应该传入 [1,2]. 但是目前需求只考虑 "或" 的情况, 所以此处传入按位与的结果即可
        static func queryEventObservable(deviceIds: [String], startTime: TimeInterval, endTime: TimeInterval, eventTypes: UInt) -> AnyPublisher<QueryEventResult, Swift.Error> {
            let eventTypes = [NSNumber.init(value: eventTypes)]
            guard let accessId = AccountCenter.shared.currentUser?.basicInfo.accessId else {
                return Fail(error: ReoqooError.generalError(reason: ReoqooError.GeneralErrorReason.userIsLogout)).eraseToAnyPublisher()
            }
            return Deferred {
                Future<JSON, Swift.Error> { promise in
                    RQCore.Agent.shared.ivVasMgr.queryEvent(ofDevices: deviceIds, accessId: accessId, startTime: startTime, endTime: endTime, alarmTypeMasks: eventTypes, validCloudStorage: false, serviceId: nil, detail: 0, faceOpt: 0) {
                        let result = ResponseHandler.responseHandling(jsonStr: $0, error: $1)
                        promise(result)
                    }
                }.tryMap { json in
                    let events = try json["data"]["list"].decoded(as: [GuardianViewController.Event].self)
                    let startTime = json["data"]["startTime"].doubleValue
                    let endTime = json["data"]["endTime"].doubleValue
                    let timeFilter = TimeFilter(start: Date.init(timeIntervalSince1970: startTime), end: Date.init(timeIntervalSince1970: endTime))
                    return (timeFilter, events)
                }
            }
            .eraseToAnyPublisher()
        }
    }
    
}
