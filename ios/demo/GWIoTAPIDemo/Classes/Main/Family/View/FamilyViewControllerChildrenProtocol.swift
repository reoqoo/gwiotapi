//
//  FamilyViewControllerChildren.swift
//  Reoqoo
//
//  Created by xiaojuntao on 1/2/2024.
//

import Foundation

protocol FamilyViewControllerChildren: UIViewController {
    var mainScrollView: UIScrollView? { get }
    func pullToRefresh(completion: (()->())?)
}
