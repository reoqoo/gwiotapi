## Enable Device Sharing by Account Feature

The SDK supports two device sharing methods: via QR code and via account. However, if the App does not use the Gwell account service and sets `disableAccountService = true` during initialization, the SDK will hide the device sharing by account feature by default. If you need to display this feature, the App needs to implement it according to the following steps.

Feature Preview:

![img.png](assets/device_sharing.png)

### Implementation Steps
#### Set Sharing Methods During Initialization
After setting `disableAccountService = true`, set `deviceShareOptions` to the sharing methods you want to display.

kotlin
```kotlin
val opts = InitOptions(AppConfig("appId", "appToken"))
opts.disableAccountService = true 
opts.deviceShareOptions = listOf(DeviceShareOption.QRCode, DeviceShareOption.Account) // Show QR code sharing and account sharing
GWIoT.initialize(opts)
```

swift
```swift
let opts = InitOptions(appConfig: AppConfig(appId: "appId", appToken: "appToken"))
opts.disableAccountService = true 
opts.deviceShareOptions = [.qrcode, .account] // Show QR code sharing and account sharing
GWIoT.shared.initialize(opts)
```


If the App needs to customize the placeholder text for the account sharing input box, it can be set through the `GWIoT.setUIConfiguration(_:)` method.

```kotlin
val cfg = UIConfiguration(
    theme = null,
    texts = AppTexts(
        appNamePlaceHolder = "App Name",  // null means SDK internally gets AppName
        accountSharingInputPlaceholder = "Please enter email/account",  // null uses SDK default text "Please enter $appName account"
    )
)
GWIoT.setUIConfiguration(cfg)
```

#### Implement and Register Interfaces
The SDK needs to query the App to confirm and display the account information of the shared user, such as nickname and avatar. There are two scenarios that require queries based on different parameters:
1. Device sharing process: Query account information through the account entered by the user
2. Display device shared user list: Query account information through Gwell accessId list

**These two interfaces need to be implemented by your App and Cloud, and the account information includes Gwell accessId. Therefore, when the App Cloud and Gwell Cloud perform account authentication docking, it is necessary to map and save the Gwell accessId with the App account.**

> Note: The SDK only uses the nickname and avatar for display in the UI and will not cache or upload them to the server. The App can desensitize the nickname before passing it to the SDK, such as `test***1`.

Detailed process is as follows:
![img.png](assets/share_flow.png)

##### Interface/Protocol Definition
```kotlin

/**
 * App registration account information service component
 */
interface IHostAccountServiceComponent: IComponent {
    /**
     * Register account information query service
     * @param service Object implementing the account information query service interface
     */
    fun registerHostAccountService(service: IHostAccountService)
}

/**
 *
 * Service interface for SDK to query account information from App when not using Gwell account service.
 */
interface IHostAccountService: IComponent {

    /**
     * Query App account information through the account entered by the user.
     *
     * Used in the device sharing process, through the account sharing function, to query users for sharing.
     */
    suspend fun onRequestAccountInfoByAccount(account: String): GWResult<HostAccountInfo>

    /**
     * Query App account information through Gwell account's accessId.
     *
     * Used in the device sharing function to query and display shared/shared user information.
     */
    suspend fun onRequestAccountInfoByAccessId(accessIds: List<String>): GWResult<List<HostAccountInfo>>
    
    
}

data class HostAccountInfo(

    /**
     * Gwell's accessId
     */
    val accessId: String,

    /**
     * User nickname
     *
     * Only used for displaying shared device user information, the App interface can desensitize the nickname before displaying it to other users.
     */
    val nickName: String,

    /**
     * User avatar url
     *
     * Used when displaying the avatar of shared/shared users in the device sharing function.
     */
    val avatarUrl: String? = null,
)
```
##### Code Examples

- Swift

```swift
/// Register account information service
GWIoT.registerHostAccountService(HostAccountService())

/// Implement IHostAccountService interface
class HostAccountService: IHostAccountService {
    func onRequestAccountInfoByAccessId(accessIds: [String], completionHandler: @escaping @Sendable (GWResult<NSArray>?, (any Error)?) -> Void) {
        
        /// Query account information from the cloud based on Gwell accessIds
        var mockInfos: [HostAccountInfo] = [
            .init(accessId: "123456", nickName: "test***1", avatarUrl: "https://example.com/example.jpg"),
            .init(accessId: "653421", nickName: "test***2", avatarUrl: "https://example.com/example.jpg"),
        ]
        gwiot_cb(completionHandler, mockInfos as NSArray, nil)
    }


    func onRequestAccountInfoByAccount(account: String, completionHandler: @escaping @Sendable (GWResult<HostAccountInfo>?, (any Error)?) -> Void) {
        
        // App judges whether the account format is legal by itself
        
        // Query account information from the cloud based on the account string, needs to be implemented by App cloud
        queryAccountInfoByAccount(account) { result in 
            switch result {
            case let .success(json):
                let info = HostAccountInfo(accessId: "gwellAccessId", nickName: "xia****com", avatarUrl: "https://example.com/example.jpg")
                gwiot_cb(completionHandler, info, nil)
            case let .failure(err):
                gwiot_cb(completionHandler, nil, err)
            }
        }
    }
}
```

- kotlin
```kotlin
    // Register account information service
    GWIoT.registerHostAccountService(object : IHostAccountService {
        override suspend fun onRequestAccountInfoByAccessId(accessIds: List<String>): GWResult<List<HostAccountInfo>> {
            // App needs to query from the cloud based on Gwell accessIds
            val mockInfos = listOf(
                HostAccountInfo(
                    accessId = "123456",
                    nickName = "test***1",
                    avatarUrl = "https://example.com/example.jpg"
                ),
                HostAccountInfo(
                    accessId = "653421",
                    nickName = "test***2",
                    avatarUrl = "https://example.com/example.jpg"
                )
            )
            return GWResult.Success(mockInfos)
        }

        override suspend fun onRequestAccountInfoByAccount(account: String): GWResult<HostAccountInfo> {
            return queryAccountInfoByAccount(account)
        }

        /**
         * Simulate querying account information from the cloud
         */
        private suspend fun queryAccountInfoByAccount(account: String): GWResult<HostAccountInfo> {
            return try {
                // Assume this is network request logic
                val info = HostAccountInfo(
                    accessId = "gwellAccessId",
                    nickName = "xia****com",
                    avatarUrl = "https://example.com/example.jpg"
                )
                GWResult.Success(info)
            } catch (err: Exception) {
                GWResult.Failure(err)
            }
        }

    })
```