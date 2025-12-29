# App开发指南

GWIoTApi以KMP([Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform-intro.html))跨平台技术进行开发，提供统一的各平台应用层接口。

在Android端可以无缝集成使用，但是在iOS端有一些特别使用说明及注意事项，为了您愉快的进行编码，请先仔细阅读[iOS编码说明](../ios/docs/ios_coding_guide.md)。

以下对App开发使用SDK的主要模块功能进行介绍，给出代码均为简单示例，具体细节请参考各平台demo。

## 初始化
在App启动时，需要进行SDK初始化，才能正常使用SDK提供的功能。
```kotlin

val opts = InitOptions(AppConfig("appId", "appToken"))
opts.language = LanguageCode.EN // 按需设置语言, 默认跟随系统
opts.disableAccountService = true // true: 不使用技威账号服务, false: 使用技威账号服务

GWIoT.initialize(opts)
```

> 如果不使用技威账号账号服务，设备插件内通过账号分享设备功能默认进行了隐藏，如果需要显示该功能，请参考[非技威账号开启通过账号分享设备](app_share_by_account.md)。

## 访问认证
根据是否使用技威账号体系/服务，SDK登录认证可以分为两种情况。
### 不使用技威账号服务
如果不使用技威账号服务，那么需要通过云端对接方式获取SDK认证所需的`UserC2CInfo`信息，详见[云云对接](../cloud/客户云云对接.md)。

获取到`UserC2CInfo`后，通过`GWIoT.login()`方法进行认证。
```kotlin
val c2cInfo = UserC2CInfo("accessId", "accessToken", "expireTime", "terminalId", "expand")

val result = GWIoT.login(c2cInfo)

```

### 使用技威账号服务
如果使用技威账号服务，App需要通过GWIoT接口实现账号注册、登录、注销等功能。
#### 注册
注册相关接口为`IAccountRegisterComponent`，可通过手机号或者邮箱进行注册

1. 获取用户输入的手机号或者邮箱
2. 发送注册验证码
3. 获取用户输入的验证码并校验
4. 调用注册接口进行注册，注册成功后GWIoT将自动登录

```kotlin
val account = AccountType.email("test@example.com")  // or AccountType.mobile("1234567890", "+86"), user input

GWIoT.sendRegisterCode(account)

GWIoT.verifyCode(account, "123456")

val registerAndLoginResult = GWIoT.register(account, "CN", "passsword", "verifiedCode")
```
#### 找回密码
如果用户忘记密码，可以通过手机号或者邮箱进行找回密码。
1. 获取用户输入的手机号或者邮箱，发送验证码
2. 校验用户输入的验证码
3. 调用重置密码接口进行重置密码
```kotlin
GWIoT.sendResetPasswordCode(account)
GWIoT.verifyCode(account, "123456")
GWIoT.resetPassword(account, "newPassword", "verifiedCode")
```
#### 登录
登录相关接口为`IAccountManagerComponent`，通过手机号或者邮箱进行登录
```kotlin
val result = GWIoT.login(account, "password")
```

### 登录监听
通过`isLogin`,`user`属性监听登录状态和用户信息更新。
```kotlin
GWIoT.isLogin.observe(lifeCycle) {
    if (it) {
        // login
    } else {
        // logout
    }
}
GWIoT.user.observe(lifeCycle) {
    // user info updated
}
```

### 登出
无论是否使用技威账号服务，login后SDK都会自动缓存用户登录信息，下次启动App时会自动登录。
所以如果需要退出登录或者切换账号，需要调用GWIoT退出登录方法清除当前登录信息。
```kotlin
GWIoT.logout()
```
## 推送消息
如果App需要直接接收Gwell云的推送消息，并且使用GWIoT SDK内置的业务逻辑处理消息，如点击设备事件通知跳转到相关页面进行播放，则需要通过以下步骤进行调用。

### 上传推送Token
App需要在设备注册推送Token后，调用GWIoT SDK上传推送Token，以便Gwell云可以将推送消息发送到当前设备。如果用户未登录，则不需要上传Token。

iOS:
```swift
func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    let token = deviceToken.map({ String.init(format: "%02.2hhx", $0) }).joined()
    
    GWIoT.shared.uploadPushToken(token: token) { result, err in
        let swiftResult = gwiot_handleCb(result, err)
        print("uploadPushToken result: \(swiftResult)")
    }
}
```

Android:
```kotlin
// Android端SDK内部处理暂时忽略
```

### 处理推送消息
App在线收到推送消息或者用户点击通知后，调用GWIoT SDK方法，SDK会识别相关自定义内容进行处理。

iOS:
```swift
extension AppDelegate: UNUserNotificationCenterDelegate {
    // 收到通知
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([])
        let userInfo = notification.request.content.userInfo
        GWIoT.shared.receivePushNotification(noti: .init(userInfo: userInfo))
    }

    // 点击通知
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        completionHandler()
        let userInfo = response.notification.request.content.userInfo
        GWIoT.shared.clickPushNotification(noti: .init(userInfo: userInfo))
    }
}
```

Android:
    
```kotlin

// 点击通知

class SplashActivity : AppCompatActivity() {
    onCreate() {
        ...
        val oneObserver = object : Observer<Boolean?> {
            override fun onChanged(value: Boolean?) {
                if (value == true) {
                    GWIoT.clickPushNotification(PushNotification(intent = intent))
                    GWIoT.sdkInitFinish.removeObserver(this)
                }
            }
        }
        GWIoT.sdkInitFinish.observeForever(oneObserver)
    }
}



// 收到通知

class SplashActivity : AppCompatActivity() {
    onCreate() {
        ...
        val oneObserver = object : Observer<Boolean?> {
            override fun onChanged(value: Boolean?) {
                if (value == true) {
                    GWIoT.receivePushNotification(PushNotification(intent = intent))
                    GWIoT.sdkInitFinish.removeObserver(this)
                }
            }
        }
        GWIoT.sdkInitFinish.observeForever(oneObserver)
    }
}

```

## 绑定设备
GWIoT已经集成了添加绑定设备的UI组件，相关接口为`IBindComponent`，App可以直接调用进入。

目前支持多种方式进入绑定流程，

### 用户选择产品进入绑定
插件内实现了支持的产品列表页面，用户可选择产品进入绑定流程。

也可以通过SDK获取到支持的产品列表, 自行实现产品列表页面以及通过产品信息对象进入绑定流程。

```kotlin
// 直接进入产品列表页面
GWIoT.openBindableProductList()

// 获取缓存的产品列表
val products = GWIoT.productList.value
// 刷新产品列表
GWIoT.queryProductList()

// 通过产品信息对象进入绑定流程
GWIoT.openBind(product)
```

### 扫描二维码绑定

扫描设备上的二维码进入绑定流程。

SDK内置了扫码页面，也可以自行实现扫码功能，通过SDK接口识别二维码类型，自行设计后续交互。

```kotlin
// 直接进入扫码页面
GWIoT.openScanQRCodePage()

// 识别二维码内容
GWIoT.recognizeQRCode(qrCodeValue, enableBuiltInHandling)

// 判断是设备二维码后，通过二维码进入绑定
GWIoT.openBind(qrCodeValue)
```


## 设备管理
设备管理相关接口为`IDevMangerComponent`，包含支持的产品列表、设备列表、设备详情等接口，每次查询后除了当前方法返回结果，同时也会更新相关的缓存List。

```kotlin
GWIoT.queryProductList()
GWIoT.queryDeviceList()
GWIoT.queryDevice("deviceId")

GWIoT.productList.observe(lifeCycle) {
    // product list updated
}
GWIoT.deviceList.observe(lifeCycle) {
    // device list updated
}
```

至此，GWIoT接口及使用方式基本介绍完毕，其他更多及更详细的接口及参数说明可以通过[API文档](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)进行查询，后续会按需不断更新SDK，提供更多功能及接口。

如果您有任何问题或建议，欢迎联系我们。

