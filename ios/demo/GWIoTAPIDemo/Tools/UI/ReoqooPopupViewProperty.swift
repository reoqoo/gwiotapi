//
//  RQSDKDemoPopupViewProperty.swift
//  RQSDKDemo
//
//  Created by chenchangxin on 2023/8/17.
//

import Foundation

/// `IVPopupView`的可配置参数`ReoqooPopupViewProperty`
class ReoqooPopupViewProperty: IVPopupViewProperty {
    
    override init() {
        super.init()
        
        maskColor = R.color.text_000000_40()!
        cornerRadius = 16
        contentInsets = .init(top: 0, left: 20, bottom: 16, right: 20)
        position = .bottom
        separatorStyle = .middle
        separatorColor = R.color.text_000000_10()!
    }
}
