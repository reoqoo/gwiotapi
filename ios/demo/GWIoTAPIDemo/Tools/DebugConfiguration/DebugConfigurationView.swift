//
//  DebugConfigurationView.swift
//  RQCore
//
//  Created by xiaojuntao on 22/11/2024.
//

import SwiftUI

struct DebugConfigurationView: View {

    @ObservedObject var vm: DebugConfigurationViewModel
    @Environment(\.presentationMode) var dismiss

    @State private var showingAlert_0 = false
    @State private var showingAlert_1 = false

    @State private var appName = ""
    @State private var appPkgName = ""
    @State private var appID = ""
    @State private var appToken = ""
    @State private var userAgreementURL = ""
    @State private var privacyPolicyURL = ""
    @State private var env: DebugConfigurationViewModel.Environment = {
        if UIApplication.UserDefaults.isTestEnv {
            return .debug
        }
        return .release
    }()

    init(vm: DebugConfigurationViewModel) {
        self.vm = vm
    }

    var body: some View {
        NavigationView {
            List {
                HStack {
                    Toggle(LocalizedStringKey.init("开启Debug Assistant"), isOn: self.$vm.openDebugAssistant)
                }
                HStack {
                    Toggle(LocalizedStringKey.init("开启H5 Debug Mode"), isOn: self.$vm.h5DebugMode)
                }
                Section.init(header: Text("reoqoo 配置项"), content: {
                    // Saas地址配置
                    VStack(alignment: .leading) {
                        Text("请求环境").foregroundColor(.gray)
                        Picker.init("请求环境", selection: self.$env) {
                            ForEach(DebugConfigurationViewModel.Environment.allCases) { env in
                                Text(env.rawValue).tag(env)
                            }
                        }.pickerStyle(.segmented)
                    }

                    // AppName
                    VStack(alignment: .leading) {
                        Text("AppName").foregroundColor(.gray)
                        TextField("输入AppName", text: self.$appName).multilineTextAlignment(.trailing)
                        Text("目前:\(self.vm.appName)").foregroundColor(.gray)
                    }

                    // AppPkgName
                    VStack(alignment: .leading) {
                        Text("AppPkgName").foregroundColor(.gray)
                        TextField("输入AppPkgName", text: self.$appPkgName).multilineTextAlignment(.trailing)
                        Text("目前:\(self.vm.appPkgName)").foregroundColor(.gray)
                    }

                    // AppID
                    VStack(alignment: .leading) {
                        Text("AppID").foregroundColor(.gray)
                        TextField("输入AppID", text: self.$appID).multilineTextAlignment(.trailing)
                        Text("目前:\(self.vm.appID)").foregroundColor(.gray)
                    }

                    // AppToken
                    VStack(alignment: .leading) {
                        Text("AppToken").foregroundColor(.gray)
                        TextField("输入AppToken", text: self.$appToken).multilineTextAlignment(.trailing)
                        Text("目前:\(self.vm.appToken)").foregroundColor(.gray)
                    }

                    // 保存上述配置操作按钮, 带文字提示: 会把现有配置表干掉, 重启App后新配置生效
                    Button("保存上面配置") {
                        showingAlert_0 = true
                    }
                    .alert(isPresented: $showingAlert_0) {
                        Alert(title: Text("保存上面配置"), message: Text("保存后,会删除当前已有配置表,请重启App.\n请确保配置内容正确, 否则只能重装App"), primaryButton: .default(Text("確定")) {
                            // 執行確定操作
                            self.vm.saveConfiguration(appName: self.appName, appPkgName: self.appPkgName, appID: self.appID, appToken: self.appToken, isTestEnv: self.env == .debug)
                        }, secondaryButton: .cancel())
                    }
                    .foregroundColor(.blue)

                    // 清除配置按钮, 带文字提示: 会把现有配置表干掉, 重启App后新配置生效
                    Button("还原配置并保存") {
                        showingAlert_1 = true
                    }
                    .alert(isPresented: $showingAlert_1) {
                        Alert(title: Text("将指定的配置清除并保存"), message: Text("保存后,会删除当前已有配置表,请重启App.\n请确保配置内容正确, 否则只能重装App"), primaryButton: .default(Text("確定")) {
                            // 執行確定操作
                            self.vm.restoreConfiguration()
                        }, secondaryButton: .cancel())
                    }
                    .foregroundColor(.blue)
                })
            }
            .navigationBarTitle(Text("DEBUG Configuration"), displayMode: .inline)
            .navigationBarItems(leading: Button("关闭") { dismiss.wrappedValue.dismiss() })
        }
        Text("Version: \(self.vm.sdkVersion)").multilineTextAlignment(.center).font(Font.footnote)
    }
}

#Preview {
    DebugConfigurationView(vm: .init())
}
