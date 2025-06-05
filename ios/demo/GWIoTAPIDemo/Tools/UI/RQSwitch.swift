//
//  RQSwitch.swift
//  Reoqoo
//
//  Created by xiaojuntao on 10/7/2024.
//

import UIKit

/// 系统 UISwitch 的用户点击和状态强关联, 只要被点击就会改变状态, 因此写这个
/// 点击后不会马上改变状态, 需要手动调整 state
class RQSwitch: UIView {

    enum State {
        case on
        case off
        case wait
    }

    private(set) lazy var tapPublisher: Combine.PassthroughSubject<(State, Bool), Never> = .init()

    var state: State = .off {
        didSet {
            switch state {
            case .on:
                self.ass.stopAnimating()
                self.uiswitch.isHidden = false
                self.uiswitch.isOn = true
            case .off:
                self.ass.stopAnimating()
                self.uiswitch.isHidden = false
                self.uiswitch.isOn = false
            case .wait:
                self.uiswitch.isHidden = true
                self.ass.startAnimating()
            }
        }
    }

    var isEnabled: Bool = true {
        didSet {
            self.uiswitch.isEnabled = self.isEnabled
        }
    }

    private(set) lazy var uiswitch: UISwitch = .init().then {
        $0.isUserInteractionEnabled = false
    }

    private(set) lazy var button: UIButton = .init(type: .custom).then {
        $0.backgroundColor = .clear
    }

    private(set) lazy var ass: UIActivityIndicatorView = .init(style: .medium).then {
        $0.hidesWhenStopped = true
    }

    var anyCancellables: [AnyCancellable] = []

    override init(frame: CGRect) {
        super.init(frame: frame)
        self.addSubview(self.uiswitch)
        self.uiswitch.snp.makeConstraints { make in
            make.edges.equalTo(UIEdgeInsets(top: 12, left: 0, bottom: 12, right: 0))
        }

        self.addSubview(self.button)
        self.button.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.addSubview(self.ass)
        self.ass.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }

        self.uiswitch.isOn = false
        self.ass.stopAnimating()

        self.button.tapPublisher.sink { [weak self] in
            guard let self else { return }
            self.tapPublisher.send((self.state, self.isEnabled))
        }.store(in: &self.anyCancellables)
    }
    
    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    func setStateWithAnimate(_ state: State) {
        switch state {
        case .on:
            self.ass.stopAnimating()
            self.uiswitch.isHidden = false
            self.uiswitch.setOn(true, animated: true)
        case .off:
            self.ass.stopAnimating()
            self.uiswitch.isHidden = false
            self.uiswitch.setOn(false, animated: true)
        case .wait:
            self.uiswitch.isHidden = true
            self.ass.startAnimating()
        }
    }
}
