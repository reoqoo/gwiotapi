//
//  VerificationCodeInputViewController+SubViews.swift
//  Reoqoo
//
//  Created by xiaojuntao on 28/7/2023.
//

import Foundation

extension OneTimeCodeInputViewController {

    class CodeView: UIView {

        var isActive: Bool = false {
            didSet {
                if self.isActive {
                    self.startCursorAnimate()
                }else{
                    self.stopCursorAnimate()
                }
            }
        }

        var character: Character? {
            didSet {
                self.label.text = self.character?.uppercased()
            }
        }

        private(set) lazy var label: UILabel = .init().then {
            $0.textAlignment = .center
            $0.font = .systemFont(ofSize: 26, weight: .semibold)
            $0.setContentCompressionResistancePriority(.init(99), for: .vertical)
        }

        /// 底部分割线
        lazy var separator: UIView = .init().then {
            $0.backgroundColor = R.color.lineInputDisable()!
        }

        /// 光标
        lazy var cursor: UIView = .init().then {
            $0.backgroundColor = R.color.text_000000_90()!
            $0.isHidden = true
        }

        /// 闪烁动画
        private lazy var opacityAnimation: CABasicAnimation = .init(keyPath: "opacity").then {
            // 属性初始值
            $0.fromValue = 1.0
            // 属性要到达的值
            $0.toValue = 0.0
            // 动画时间
            $0.duration = 0.9
            // 重复次数(无穷大)
            $0.repeatCount = Float.greatestFiniteMagnitude
            /*
             removedOnCompletion：默认为YES，代表动画执行完毕后就从图层上移除，图形会恢复到动画执行前的状态。如果想让图层保持显示动画执行后的状态，那就设置为NO，不过还要设置fillMode为kCAFillModeForwards
             */
            $0.isRemovedOnCompletion = true
            // 决定当前对象在非active时间段的行为。比如动画开始之前或者动画结束之后
            $0.fillMode = .forwards
            // 速度控制函数，控制动画运行的节奏
            /*
             kCAMediaTimingFunctionLinear（线性）：匀速，给你一个相对静态的感觉
             kCAMediaTimingFunctionEaseIn（渐进）：动画缓慢进入，然后加速离开
             kCAMediaTimingFunctionEaseOut（渐出）：动画全速进入，然后减速的到达目的地
             kCAMediaTimingFunctionEaseInEaseOut（渐进渐出）：动画缓慢的进入，中间加速，然后减速的到达目的地。这个是默认的动画行为。
             */
            $0.timingFunction = CAMediaTimingFunction(name: .easeIn)
        }

        override init(frame: CGRect) {
            super.init(frame: frame)
            self.addSubview(self.label)
            self.label.snp.makeConstraints { make in
                make.top.leading.trailing.bottom.equalToSuperview()
            }

            self.addSubview(self.separator)
            self.separator.snp.makeConstraints { make in
                make.leading.trailing.bottom.equalToSuperview()
                make.height.equalTo(1)
            }

            self.addSubview(self.cursor)
            self.cursor.snp.makeConstraints { make in
                make.center.equalToSuperview()
                make.top.equalToSuperview().offset(4)
                make.bottom.equalToSuperview().offset(-4)
                make.width.equalTo(1)
            }
        }

        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

        // MARK: Animate
        func startCursorAnimate() {
            self.cursor.isHidden = false
            self.cursor.layer.add(self.opacityAnimation, forKey: "kOpacityAnimation")
        }

        func stopCursorAnimate() {
            self.cursor.isHidden = true
            self.cursor.layer.removeAnimation(forKey: "kOpacityAnimation")
        }
    }
}
