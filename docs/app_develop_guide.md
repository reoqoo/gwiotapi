# App开发指南

GWIoTApi以KMP([Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform-intro.html))跨平台技术进行开发，提供统一的各平台应用层接口。

在Android端可以无缝集成使用，但是在iOS端有一些特别使用说明及注意事项，为了您愉快的进行编码，请先仔细阅读[iOS编码说明](../ios/docs/ios_coding_guide.md)。

以下对App开发使用SDK的主要模块功能进行介绍，给出代码均为简单示例，具体细节请参考各平台demo。

## 初始化
在App启动时，需要进行SDK初始化，才能正常使用SDK提供的功能。
```kotlin

val opts = InitOptions(AppConfig("appId", "appToken", "appName"))
opts.language = LanguageCode.EN // 按需设置语言
GWIoT.initialize(opts)
```

## 访问认证
根据是否使用技威账号体系/服务，SDK登录认证可以分为两种情况。
### 不使用技威账号服务
如果不使用技威账号服务，那么需要通过云端对接方式获取SDK认证所需的`IUserAccessInfo`信息，详见[云云对接](https://note.youdao.com/coshare/index.html?token=EA4BCC59DE664ACCBA3AD7723D0B5B89&gid=108651055&_time=1745378888893#/1425034038)。

获取到`IUserAccessInfo`后，通过`GWIoT.login()`方法进行认证。
```kotlin
val userAccessInfo = UserAccessInfo() // impl IUserAccessInfo

val result = GWIoT.login(userAccessInfo)

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

## 绑定设备
GWIoT已经集成了添加绑定设备的UI组件，相关接口为`IBindComponent`，App可以直接调用进入。

目前仅支持通过设备二维码进入绑定流程，

如果App自行实现扫码并传入二维码字符串，则SDK内识别二维码内容进入绑定页面；

如果不传，则SDK进入扫码页面。

绑定成功会返回`IDevice`信息。
```kotlin
val opts = BindOptions(qrcoeValue = null)
val deviceResult = GWIoT.openBind(opts)
```

## 设备管理
设备管理相关接口为`IDevMangerComponent`，包含支持的产品列表、设备列表、设备详情等接口，每次查询后会更新缓存并LiveData通知。

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



