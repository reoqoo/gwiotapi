//
//  EventContentView+EventTableViewCell.swift
//  Reoqoo
//
//  Created by xiaojuntao on 30/8/2023.
//

import Foundation

extension GuardianViewController.EventContentView {
    
    /// 事件 tableView cell
    /// 单设备风格
    /// 事件文案 + 事件图标
    class EventTableViewCellStyle0: UITableViewCell {
        var event: GuardianViewController.Event? {
            didSet {

                self.eventsStackView.removeAllSubviews()

                guard let event = event else { return }

                self.timeLabel.text = Date(timeIntervalSince1970: event.startTime).string(with: "HH:mm")
                self.eventImageView.kf.setImage(with: event.imageURL, placeholder: R.image.commonImageLoadingPlaceholder()!)
                self.eventDurationLabel.text = event.duration == 0 ? "" : event.duration.stringFormatted()
                
                // 事件图标
                event.alarmType.asArray.forEach {
                    let imageView: UIImageView = .init(image: $0.icon)
                    self.eventsStackView.addArrangedSubview(imageView)
                }
                // 事件描述
                self.eventDescriptionLabel.text = event.alarmType.description2
            }
        }

        lazy var timeLabel: UILabel = .init().then {
            $0.numberOfLines = 0
            $0.textAlignment = .left
            $0.font = .systemFont(ofSize: 16)
            $0.textColor = R.color.text_000000_90()
            $0.setContentHuggingPriority(.init(999), for: .horizontal)
            $0.setContentCompressionResistancePriority(.init(999), for: .horizontal)
        }

        lazy var eventsStackView: UIStackView = .init().then {
            $0.spacing = 4
            $0.axis = .horizontal
            $0.alignment = .center
            $0.distribution = .fillProportionally
        }

        lazy var eventDescriptionLabel: UILabel = .init().then {
            $0.textColor = R.color.text_000000_90()
            $0.font = .systemFont(ofSize: 14)
        }

        lazy var eventImageView: UIImageView = .init().then {
            $0.layer.cornerRadius = 4
            $0.layer.masksToBounds = true
            $0.contentMode = .scaleAspectFill
        }

        lazy var eventDurationLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 10)
            $0.textColor = R.color.text_FFFFFF()
        }

        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {

            super.init(style: style, reuseIdentifier: reuseIdentifier)

            self.contentView.addSubview(self.timeLabel)
            self.timeLabel.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(12)
                make.centerY.equalToSuperview()
                // UI说没对齐, 所以加个宽度约束
                make.width.greaterThanOrEqualTo(45)
            }

            let container_devName_event: UIView = .init()
            container_devName_event.addSubview(self.eventDescriptionLabel)
            self.eventDescriptionLabel.snp.makeConstraints { make in
                make.leading.top.equalToSuperview()
                make.trailing.lessThanOrEqualToSuperview()
            }

            container_devName_event.addSubview(self.eventsStackView)
            self.eventsStackView.snp.makeConstraints { make in
                make.bottom.leading.equalToSuperview()
                make.trailing.lessThanOrEqualToSuperview()
                make.top.equalTo(self.eventDescriptionLabel.snp.bottom).offset(8)
            }

            self.contentView.addSubview(container_devName_event)
            container_devName_event.snp.makeConstraints { make in
                make.leading.equalTo(self.timeLabel.snp.trailing).offset(16)
                make.top.greaterThanOrEqualToSuperview()
                make.bottom.lessThanOrEqualToSuperview()
                make.centerY.equalToSuperview()
            }

            // 事件截图 + 时长label
            let container_eventImg_eventDuration: UIView = .init()
            container_eventImg_eventDuration.addSubview(self.eventImageView)
            self.eventImageView.snp.makeConstraints { make in
                make.edges.equalToSuperview()
                make.width.equalTo(88)
                make.height.equalTo(48)
            }

            container_eventImg_eventDuration.addSubview(self.eventDurationLabel)
            self.eventDurationLabel.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(4)
                make.bottom.equalToSuperview().offset(-4)
            }

            self.contentView.addSubview(container_eventImg_eventDuration)
            container_eventImg_eventDuration.snp.makeConstraints { make in
                make.trailing.equalToSuperview().offset(-12)
                make.centerY.equalToSuperview()
                make.leading.greaterThanOrEqualTo(container_devName_event.snp.trailing).offset(8)
            }

            let selectedBackgroundView = UIView.init(frame: self.bounds)
            selectedBackgroundView.backgroundColor = R.color.text_link_4A68A6()?.withAlphaComponent(0.1)
            self.selectedBackgroundView = selectedBackgroundView

            let separator = UIView.init()
            separator.backgroundColor = R.color.lineSeparator()!
            self.contentView.addSubview(separator)
            separator.snp.makeConstraints { make in
                make.bottom.equalToSuperview()
                make.leading.equalTo(12)
                make.trailing.equalTo(-12)
                make.height.equalTo(0.5)
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }

    /// 事件 tableView cell
    /// 多设备风格
    /// 事件文案 + 设备名称
    class EventTableViewCellStyle1: UITableViewCell {

        var event: GuardianViewController.Event? {
            didSet {

                self.eventsStackView.removeAllSubviews()
                
                guard let event = event else { return }

                self.timeLabel.text = Date(timeIntervalSince1970: event.startTime).string(with: "HH:mm")
                event.deviceNameObservable.sink(receiveValue: { [weak self] name in
                    self?.deviceNameLabel.text = name
                }).store(in: &self.anyCancellables)
                self.eventImageView.kf.setImage(with: event.imageURL, placeholder: R.image.commonImageLoadingPlaceholder()!)
                self.eventDurationLabel.text = event.duration == 0 ? "" : event.duration.stringFormatted()

                // 判断该显示 事件图标+label 或 事件图标们
                if event.alarmType.asArray.count > 1 {
                    self.eventIcon_eventDesLabel_container.isHidden = true
                    self.eventsStackView.isHidden = false
                    event.alarmType.asArray.forEach {
                        let imageView: UIImageView = .init(image: $0.icon)
                        self.eventsStackView.addArrangedSubview(imageView)
                    }
                }else{
                    self.eventIcon_eventDesLabel_container.isHidden = false
                    self.eventsStackView.isHidden = true
                    self.eventIconImageView.image = event.alarmType.icon
                    self.eventDescriptionLabel.text = event.alarmType.description2
                }
            }
        }

        lazy var timeLabel: UILabel = .init().then {
            $0.numberOfLines = 0
            $0.textAlignment = .left
            $0.font = .systemFont(ofSize: 16)
            $0.textColor = R.color.text_000000_90()
            $0.setContentHuggingPriority(.init(999), for: .horizontal)
            $0.setContentCompressionResistancePriority(.init(999), for: .horizontal)
        }

        lazy var deviceNameLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 12)
            $0.textColor = R.color.text_000000_50()
        }
        
        lazy var eventIconImageView: UIImageView = .init().then {
            $0.setContentHuggingPriority(.init(999), for: .horizontal)
        }

        lazy var eventDescriptionLabel: UILabel = .init().then {
            $0.textColor = R.color.text_000000_90()
            $0.font = .systemFont(ofSize: 14)
            $0.setContentCompressionResistancePriority(.init(199), for: .horizontal)
        }

        lazy var eventIcon_eventDesLabel_container: UIView = .init().then {
            $0.backgroundColor = .clear
        }

        lazy var eventImageView: UIImageView = .init().then {
            $0.layer.cornerRadius = 4
            $0.layer.masksToBounds = true
            $0.contentMode = .scaleAspectFill
        }

        lazy var eventDurationLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 10)
            $0.textColor = R.color.text_FFFFFF()
        }

        lazy var eventsStackView: UIStackView = .init().then {
            $0.spacing = 4
            $0.axis = .horizontal
            $0.alignment = .center
            $0.distribution = .fillProportionally
        }

        var anyCancellables: Set<AnyCancellable> = []
        
        override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {

            super.init(style: style, reuseIdentifier: reuseIdentifier)

            self.contentView.addSubview(self.timeLabel)
            self.timeLabel.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(12)
                make.centerY.equalToSuperview()
                // UI说没对齐, 所以加个宽度约束
                make.width.greaterThanOrEqualTo(45)
            }

            // 设备名称 + (事件icon + 事件描述)
            self.eventIcon_eventDesLabel_container.addSubview(self.eventIconImageView)
            self.eventIconImageView.snp.makeConstraints { make in
                make.leading.top.bottom.equalToSuperview()
            }

            // 事件icon + 事件描述
            self.eventIcon_eventDesLabel_container.addSubview(self.eventDescriptionLabel)
            self.eventDescriptionLabel.snp.makeConstraints { make in
                make.top.bottom.trailing.equalToSuperview()
                make.leading.equalTo(self.eventIconImageView.snp.trailing).offset(2)
            }

            let container_devName_event: UIView = .init()
            container_devName_event.addSubview(self.deviceNameLabel)
            self.deviceNameLabel.snp.makeConstraints { make in
                make.leading.bottom.equalToSuperview()
                make.trailing.lessThanOrEqualToSuperview()
            }

            container_devName_event.addSubview(self.eventIcon_eventDesLabel_container)
            self.eventIcon_eventDesLabel_container.snp.makeConstraints { make in
                make.top.leading.trailing.equalToSuperview()
                make.bottom.equalTo(self.deviceNameLabel.snp.top).offset(-8)
            }

            // 事件 icon stackView
            container_devName_event.addSubview(self.eventsStackView)
            self.eventsStackView.snp.makeConstraints { make in
                make.top.leading.equalToSuperview()
                make.trailing.lessThanOrEqualToSuperview()
                make.bottom.equalTo(self.deviceNameLabel.snp.top).offset(-8)
            }

            self.contentView.addSubview(container_devName_event)
            container_devName_event.snp.makeConstraints { make in
                make.leading.equalTo(self.timeLabel.snp.trailing).offset(16)
                make.top.greaterThanOrEqualToSuperview()
                make.bottom.lessThanOrEqualToSuperview()
                make.centerY.equalToSuperview()
            }

            // 事件截图 + 时长label
            let container_eventImg_eventDuration: UIView = .init()
            container_eventImg_eventDuration.addSubview(self.eventImageView)
            self.eventImageView.snp.makeConstraints { make in
                make.edges.equalToSuperview()
                make.width.equalTo(88)
                make.height.equalTo(48)
            }

            container_eventImg_eventDuration.addSubview(self.eventDurationLabel)
            self.eventDurationLabel.snp.makeConstraints { make in
                make.leading.equalToSuperview().offset(4)
                make.bottom.equalToSuperview().offset(-4)
            }

            self.contentView.addSubview(container_eventImg_eventDuration)
            container_eventImg_eventDuration.snp.makeConstraints { make in
                make.trailing.equalToSuperview().offset(-12)
                make.centerY.equalToSuperview()
                make.leading.greaterThanOrEqualTo(container_devName_event.snp.trailing).offset(8)
            }

            let selectedBackgroundView = UIView.init(frame: self.bounds)
            selectedBackgroundView.backgroundColor = R.color.text_link_4A68A6()?.withAlphaComponent(0.1)
            self.selectedBackgroundView = selectedBackgroundView

            let separator = UIView.init()
            separator.backgroundColor = R.color.lineSeparator()!
            self.contentView.addSubview(separator)
            separator.snp.makeConstraints { make in
                make.bottom.equalToSuperview()
                make.leading.equalTo(12)
                make.trailing.equalTo(-12)
                make.height.equalTo(0.5)
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
        
    }
    
    class EventTableViewHeader: UITableViewHeaderFooterView {

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
        
        lazy var timeLabel: UILabel = .init().then {
            $0.font = .systemFont(ofSize: 14, weight: .medium)
            $0.textColor = R.color.text_000000_90()
        }

        override init(reuseIdentifier: String?) {
            super.init(reuseIdentifier: reuseIdentifier)

            self.contentView.addSubview(self.timeLabel)
            self.timeLabel.snp.makeConstraints { make in
                make.top.equalToSuperview().offset(16)
                make.leading.equalToSuperview().offset(12)
                make.bottom.equalToSuperview().offset(-8)
                make.trailing.equalToSuperview().offset(-12)
            }
        }
    }
}
