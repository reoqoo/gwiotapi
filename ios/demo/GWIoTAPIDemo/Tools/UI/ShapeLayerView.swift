//
//  ShapeLayerView.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 7/8/2023.
//

import UIKit

class ShapeLayerView: UIView {

    var shapeLayer: CAShapeLayer {
        return self.layer as! CAShapeLayer
    }

    override class var layerClass: AnyClass {
        CAShapeLayer.self
    }

}
