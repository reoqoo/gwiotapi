//
//  FamilyHeaderView.swift
//  Reoqoo
//
//  Created by chenchangxin on 2023/8/9.
//

import Foundation

/// 家庭tab页的头部视图
class FamilyHeaderView: UIView {
    //MARK: public
    var title: String = "" {
        didSet {
            self.titleLabel.text = title
        }
    }
    
    //MARK: private
    private var anyCancellables: Set<AnyCancellable> = []
    
    private(set) lazy var titleLabel = UILabel().then {
        $0.font = .systemFont(ofSize: 26, weight: .medium)
        $0.textAlignment = .left
        $0.textColor = R.color.text_000000_90()
        $0.text = self.title
    }
    
    private(set) lazy var addButton = UIButton(type: .custom).then {
        $0.setImage(R.image.family_add(), for: .normal)
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func setupUI() {
        addSubview(self.titleLabel)
        addSubview(self.addButton)
        
        self.titleLabel.snp.makeConstraints { make in
            make.top.bottom.equalToSuperview()
            make.left.equalTo(16)
        }
        
        self.addButton.snp.makeConstraints { make in
            make.top.bottom.equalTo(self.titleLabel)
            make.right.equalToSuperview().offset(-16)
            make.width.equalTo(self.snp.height)
        }
    }
}
