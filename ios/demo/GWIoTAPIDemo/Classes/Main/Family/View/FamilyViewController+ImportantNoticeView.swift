//
//  ImportantNoticeView.swift
//  Reoqoo
//
//  Created by xiaojuntao on 7/3/2025.
//

import Foundation

extension FamilyViewController2 {
    class ImportantNoticeView: UIView {
        
        public var item: FamilyViewController2.ImportantNoticeItem? {
            didSet {
                self.theLabel.text = self.item?.title
            }
        }
        
        public lazy var tapClosePublisher: AnyPublisher<Void, Never> = self.closeBtn.tapPublisher.eraseToAnyPublisher()
        public lazy var tapOnLabelPublisher: AnyPublisher<Void, Never> = self.tapOnLabelGesture.tapPublisher.map({ _ in () }).eraseToAnyPublisher()

        private lazy var trumpetIcon: UIImageView = .init(image: R.image.family_importantNotice_trumpet()).then {
            $0.contentMode = .center
        }
        
        private lazy var closeBtn: UIButton = .init(type: .custom).then {
            $0.setImage(R.image.family_importantNotice_close(), for: .normal)
        }
        
        private lazy var theLabel: UILabel = .init(frame: .zero).then {
            $0.textColor = R.color.text_FF7500_90()
            $0.font = .systemFont(ofSize: 12, weight: .regular)
            $0.isUserInteractionEnabled = true
        }

        private lazy var tapOnLabelGesture: UITapGestureRecognizer = .init()

        override init(frame: CGRect) {
            super.init(frame: frame)
            
            self.backgroundColor = R.color.background_FFF4EB()
            
            self.addSubview(self.trumpetIcon)
            self.trumpetIcon.snp.makeConstraints { make in
                make.leading.top.bottom.equalToSuperview()
                make.width.equalTo(40)
            }
            
            self.addSubview(self.theLabel)
            self.theLabel.snp.makeConstraints { make in
                make.leading.equalTo(self.trumpetIcon.snp.trailing)
                make.top.bottom.equalToSuperview()
            }
            
            self.addSubview(self.closeBtn)
            self.closeBtn.snp.makeConstraints { make in
                make.leading.equalTo(self.theLabel.snp.trailing)
                make.top.bottom.trailing.equalToSuperview()
                make.width.equalTo(40)
            }

            self.theLabel.addGestureRecognizer(self.tapOnLabelGesture)
        }
        
        required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    }
}
