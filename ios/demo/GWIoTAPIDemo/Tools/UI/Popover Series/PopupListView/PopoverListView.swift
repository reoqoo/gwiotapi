//
//  PopupListView.swift
//  RQSDKDemo
//
//  Created by xiaojuntao on 19/4/2024.
//

import Foundation

extension PopoverListView {
    class Item {
        var image: UIImage?
        var title: String?
        var handler: (()->())?

        init(image: UIImage?, title: String?, handler: (()->())?) {
            self.image = image
            self.title = title
            self.handler = handler
        }
    }

    class Cell: UITableViewCell {
        var item: Item? {
            didSet {
                self.textLabel?.text = item?.title
                self.imageView?.image = item?.image
            }
        }
    }
}

class PopoverListView: UIView, Popoverable {

    // MARK: Popoverable
    var popoverOptions: [PopoverOption]
    
    // MARK: Property
    private(set) lazy var tableView: UITableView = .init(frame: .zero, style: .plain).then {
        $0.delegate = self
        $0.dataSource = self
        $0.register(Cell.self, forCellReuseIdentifier: String.init(describing: Cell.self))
        $0.separatorInset = .init(top: 0, left: 16, bottom: 0, right: 16)
        $0.separatorColor = R.color.lineSeparator()
        $0.showsHorizontalScrollIndicator = false
        $0.showsVerticalScrollIndicator = false
        $0.isScrollEnabled = false
    }
    
    var items: [Item]

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }
    override init(frame: CGRect) { fatalError("init(frame:) has not been implemented") }

    init(options: [PopoverOption], items: [Item], frame: CGRect?, rowHeight: CGFloat = 50) {
        self.items = items
        self.popoverOptions = options

        super.init(frame: .zero)
        self.addSubview(self.tableView)
        self.tableView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }

        self.frame = frame ?? .init(x: 0, y: 0, width: 200, height: 300)
        self.tableView.rowHeight = rowHeight

        self.tableView.tableHeaderView = .init(frame: .init(x: 0, y: 0, width: 0, height: 1))
        self.tableView.tableFooterView = .init(frame: .init(x: 0, y: 0, width: 0, height: 1))
    }

}

extension PopoverListView: UITableViewDataSource, UITableViewDelegate {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int { self.items.count }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: String.init(describing: Cell.self), for: indexPath) as! Cell
        cell.item = self.items[indexPath.row]
        let selectedBackgroundView = UIView.init()
        selectedBackgroundView.backgroundColor = .black.withAlphaComponent(0.1)
        cell.selectedBackgroundView = selectedBackgroundView
        return cell
    }

    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let item = self.items[safe_: indexPath.row]
        item?.handler?()
        self.popoverView.dismiss()
    }

    func tableView(_ tableView: UITableView, heightForFooterInSection section: Int) -> CGFloat { 0.1 }

    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat { 0.1 }
}
