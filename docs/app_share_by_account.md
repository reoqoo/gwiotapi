## 开启通过账号分享设备功能

SDK内分享设备支持两种方式，通过二维码和通过账号分享设备。但是如果App不使用技威账号服务，初始化时设置了`disableAccountService = true`，则SDK会默认隐藏通过账号分享设备功能。如果需要显示该功能，App需要按照以下步骤实现。

功能预览:

![img.png](assets/device_sharing.png)

### 实现步骤
#### 初始化时设置分享方式
在设置`disableAccountService = true`后，设置`deviceShareOptions`为需要显示的分享方式。

```kotlin
val opts = InitOptions(AppConfig("appId", "appToken"))
opts.disableAccountService = true 
opts.deviceShareOptions = listOf(DeviceShareOption.QRCode, DeviceShareOption.Account(inputPlaceholder = "请输入账号")) // 显示二维码分享和账号分享, inputPlaceholder为空则使用插件默认提示"请输入$appName账号"
GWIoT.initialize(opts)
```

#### 实现并注册接口
SDK需要通过App查询确认并显示被分享用户的账号信息，如昵称、头像，有两个场景需要根据不同的参数进行查询
1. 分享设备流程，通过用户输入的账号查询账号信息
2. 显示设备已分享用户列表，通过技威accessId列表查询账号信息

**这两个接口需要您的App和Cloud实现，账号信息包含技威accesId。 所以App Cloud和技威云进行账号认证对接时，需要将技威的accessId和App账号进行映射保存。**

> 注意，SDK内仅使用昵称、头像在UI上进行展示，不会缓存或者上传到服务器。 App可以对昵称进行脱敏处理再传给SDK，如`test***1`。

详细流程如下：
![img.png](assets/share_flow.png)

##### 接口/协议定义
```kotlin

/**
 * App注册账号信息服务组件
 */
interface IHostAccountServiceComponent: IComponent {
    /**
     * 注册账号信息查询服务
     * @param service 实现账号信息查询服务接口的对象
     */
    fun registerHostAccountService(service: IHostAccountService)
}

/**
 *
 * 不使用技威账号服务时，SDK向App查询账号信息的服务接口。
 */
interface IHostAccountService: IComponent {

    /**
     * 通过用户输入的账号查询App账号信息。
     *
     * 在分享设备，通过账号分享功能处，通过这个接口查询用户进行分享。
     */
    suspend fun onRequestAccountInfoByAccount(account: String): GWResult<HostAccountInfo>

    /**
     * 通过技威账号的accessId查询App账号信息。
     *
     * 在分享设备功能，通过这个接口查询展示分享/被分享用户信息。
     */
    suspend fun onRequestAccountInfoByAccessId(accessIds: List<String>): GWResult<List<HostAccountInfo>>
    
    
}

data class HostAccountInfo(

    /**
     * 技威的accessId
     */
    val accessId: String,

    /**
     * 用户昵称
     *
     * 仅用于分享设备用户信息展示，App接口可以对昵称进行脱敏处理，展示给其他用户。
     */
    val nickName: String,

    /**
     * 用户头像url
     *
     * 设备分享功能展示分享/被分享用户头像时使用。
     */
    val avatarUrl: String? = null,
)
```
##### 代码示例

- Swift

```swift
/// 注册账号信息服务
GWIoT.registerHostAccountService(HostAccountService())

/// 实现IHostAccountService接口
class HostAccountService: IHostAccountService {
    func onRequestAccountInfoByAccessId(accessIds: [String], completionHandler: @escaping @Sendable (GWResult<NSArray>?, (any Error)?) -> Void) {
        
        /// 根据gwell accessIds从云端查询账号信息
        var mockInfos: [HostAccountInfo] = [
            .init(accessId: "123456", nickName: "test***1", avatarUrl: "https://example.com/example.jpg"),
            .init(accessId: "653421", nickName: "test***2", avatarUrl: "https://example.com/example.jpg"),
        ]
        gwiot_cb(completionHandler, mockInfos as NSArray, nil)
    }


    func onRequestAccountInfoByAccount(account: String, completionHandler: @escaping @Sendable (GWResult<HostAccountInfo>?, (any Error)?) -> Void) {
        
        // App自行判断账号格式等是否合法
        
        // 根据账号字符串从云端查询账号信息，需要App cloud实现
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
    // 注册账号信息服务
    GWIoT.registerHostAccountService(object : IHostAccountService {
        override suspend fun onRequestAccountInfoByAccessId(accessIds: List<String>): GWResult<List<HostAccountInfo>> {
            // App需要根据gwell accessIds从云端查询
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
         * 模拟从云端查询账号信息
         */
        private suspend fun queryAccountInfoByAccount(account: String): GWResult<HostAccountInfo> {
            return try {
                // 假设这里是网络请求逻辑
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
