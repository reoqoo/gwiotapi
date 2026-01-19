# App Development Guide

GWIoTApi is developed using KMP ([Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform-intro.html)) cross-platform technology, providing unified application-layer interfaces for each platform.

It can be seamlessly integrated and used on the Android platform, but there are some special usage instructions and considerations on the iOS platform. To ensure a pleasant coding experience, please carefully read the [iOS Coding Instructions](../ios/docs/ios_coding_guide.en.md) first.

The following introduces the main module functions of the SDK for App development, and the code provided is a simple example. For specific details, please refer to the demos of each platform.

## Initialization
When the App starts, SDK initialization is required to use the functions provided by the SDK normally.
```kotlin

val opts = InitOptions(AppConfig("appId", "appToken"))
opts.language = LanguageCode.EN // Set language as needed, default follows the system
opts.disableAccountService = true // true: Do not use Gwell account service, false: Use Gwell account service

GWIoT.initialize(opts)
```

> If you do not use the Gwell account service, the device sharing function within the device plugin is hidden by default. If you need to display this function, please refer to [Enable Device Sharing by Account for Non-Gwell Accounts](app_share_by_account.en.md).

## Access Authentication
Depending on whether to use the Gwell account system/service, SDK login authentication can be divided into two cases.
### Not Using Gwell Account Service
If you do not use the Gwell account service, you need to obtain the `UserC2CInfo` information required for SDK authentication through cloud-to-cloud docking. For details, see [Cloud-to-Cloud Docking](../cloud/客户云云对接.en.md).

After obtaining `UserC2CInfo`, authenticate through the `GWIoT.login()` method.
```kotlin
val c2cInfo = UserC2CInfo("accessId", "accessToken", "expireTime", "terminalId", "expand")

val result = GWIoT.login(c2cInfo)

```

### Using Gwell Account Service
If you use the Gwell account service, the App needs to implement account registration, login, logout and other functions through the GWIoT interface.
#### Registration
The registration-related interface is `IAccountRegisterComponent`, which supports registration via mobile phone number or email.

1. Get the mobile phone number or email entered by the user
2. Send the registration verification code
3. Get and verify the verification code entered by the user
4. Call the registration interface to register, and GWIoT will automatically log in after successful registration

```kotlin
val account = AccountType.email("test@example.com")  // or AccountType.mobile("1234567890", "+86"), user input

GWIoT.sendRegisterCode(account)

GWIoT.verifyCode(account, "123456")

val registerAndLoginResult = GWIoT.register(account, "CN", "passsword", "verifiedCode")
```
#### Password Retrieval
If the user forgets the password, they can retrieve it via mobile phone number or email.
1. Get the mobile phone number or email entered by the user, and send a verification code
2. Verify the verification code entered by the user
3. Call the reset password interface to reset the password
```kotlin
GWIoT.sendResetPasswordCode(account)
GWIoT.verifyCode(account, "123456")
GWIoT.resetPassword(account, "newPassword", "verifiedCode")
```
#### Login
The login-related interface is `IAccountManagerComponent`, which supports login via mobile phone number or email.
```kotlin
val result = GWIoT.login(account, "password")
```

### Login Monitoring
Monitor login status and user information updates through the `isLogin` and `user` properties.
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

### Logout
Regardless of whether the Gwell account service is used, the SDK will automatically cache user login information after login, and will automatically log in the next time the App starts.
Therefore, if you need to log out or switch accounts, you need to call the GWIoT logout method to clear the current login information.
```kotlin
GWIoT.logout()
```
## Push Messages
If the App needs to directly receive push messages from Gwell Cloud and use the built-in business logic of the GWIoT SDK to process messages, such as jumping to the relevant page for playback when clicking a device event notification, it needs to be called through the following steps.

### Upload Push Token
After the device registers the push token, the App needs to call the GWIoT SDK to upload the push token so that Gwell Cloud can send push messages to the current device. If the user is not logged in, there is no need to upload the token.

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
// Internal processing on Android SDK is temporarily ignored
```

### Process Push Messages
When the App receives a push message online or the user clicks a notification, call the GWIoT SDK method, and the SDK will identify the relevant custom content for processing.

iOS:
```swift
extension AppDelegate: UNUserNotificationCenterDelegate {
    // Receive notification
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        
        let userInfo = notification.request.content.userInfo
        GWIoT.shared.receivePushNotification(noti: .init(userInfo: userInfo))

        /// Display notifications received when the App is in the foreground according to App requirements
        /// .sound: Play prompt sound
        /// .banner: Display at the top of the App as a Banner
        /// .list: Display in the system notification bar list
        /// .badge: Update the number on the App icon
        completionHandler([.sound, .banner, .list, .badge])
    }

    // Click notification
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        completionHandler()
        let userInfo = response.notification.request.content.userInfo
        GWIoT.shared.clickPushNotification(noti: .init(userInfo: userInfo))
    }
}
```

Android:
    
```kotlin

// Click notification

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



// Receive notification

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

## Device Binding
GWIoT has integrated UI components for adding and binding devices. The related interface is `IBindComponent`, which can be directly called by the App.

Currently, it supports multiple ways to enter the binding process.

### User Selects Product to Enter Binding
The plugin implements a page for the list of supported products, and users can select a product to enter the binding process.

You can also obtain the list of supported products through the SDK, implement the product list page yourself, and enter the binding process through the product information object.

```kotlin
// Directly enter the product list page
GWIoT.openBindableProductList()

// Get the cached product list
val products = GWIoT.productList.value
// Refresh the product list
GWIoT.queryProductList()

// Enter the binding process through the product information object
GWIoT.openBind(product)
```

### Scan QR Code to Bind

Scan the QR code on the device to enter the binding process.

The SDK has a built-in QR code scanning page, or you can implement the QR code scanning function yourself, identify the QR code type through the SDK interface, and design the subsequent interaction yourself.

```kotlin
// Directly enter the QR code scanning page
GWIoT.openScanQRCodePage()

// Identify QR code content
GWIoT.recognizeQRCode(qrCodeValue, enableBuiltInHandling)

// After determining it is a device QR code, enter binding through the QR code
GWIoT.openBind(qrCodeValue)
```


## Device Management
The device management-related interface is `IDevMangerComponent`, which includes interfaces for supported product lists, device lists, device details, etc. After each query, in addition to returning results from the current method, it will also update the relevant cache List.

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

So far, the basic introduction to the GWIoT interface and usage has been completed. For more detailed interfaces and parameter descriptions, please refer to the [API Documentation](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html). The SDK will be continuously updated to provide more functions and interfaces as needed.

If you have any questions or suggestions, please feel free to contact us.