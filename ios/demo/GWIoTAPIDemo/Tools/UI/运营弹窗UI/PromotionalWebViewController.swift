//
//  PromotionalWebViewController.swift
//  Reoqoo
//
//  Created by xiaojuntao on 14/12/2023.
//

import Foundation
import RQWebServices

class PromotionalWebViewController: WebViewController {
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
        self.modalPresentationStyle = .overFullScreen
        self.modalTransitionStyle = .crossDissolve
    }
    
    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    public let jumpBehaviorObservable: Combine.PassthroughSubject<URL, Never> = .init()

    override func viewDidLoad() {
        super.viewDidLoad()

        self.view.backgroundColor = .clear
        self.webView.isOpaque = false
        self.webView.backgroundColor = .clear
        self.webView.tintColor = .clear
        self.webView.scrollView.backgroundColor = .clear
        self.webView.scrollView.bounces = false
        self.webView.scrollView.alwaysBounceVertical = false
        self.webView.scrollView.contentInsetAdjustmentBehavior = .never
        
        if #available(iOS 15.0, *) {
            self.webView.underPageBackgroundColor = .clear
        }
        
        self.webView.snp.remakeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.progressView.removeFromSuperview()
    }

    override func openURL(_ url: URL?) {
        self.dismiss(animated: false, completion: {
            guard let url = url else { return }
            self.jumpBehaviorObservable.send(url)
        })
    }

    override func closeWebView() {
        self.dismiss(animated: true)
    }
}
