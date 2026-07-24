# 自定义云服务与流量页面跳转

## 概述

SDK 默认提供内置的云服务页面和流量服务页面。当 App 需要自定义这两个页面的跳转逻辑时（例如：跳转到 App 自有的 H5 页面、Native 页面或通过其他方式处理），可以通过实现 `IHostCloudService` 和 `IHostTrafficService` 接口来接管页面打开行为。

## 工作原理

当 SDK 内部需要打开云服务页面或流量服务页面时，会按以下优先级处理：

1. 检查 App 是否注册了对应的自定义服务（`IHostCloudService` / `IHostTrafficService`）
2. 若已注册，且 `isCustomOpenCloudPage` / `isCustomOpenTrafficPage` 返回 `true`，则调用 App 实现的 `openCloudPage` / `openTrafficPage` 方法
3. 若未注册或返回 `false`，则使用 SDK 默认的页面打开方式

## 接口定义

### IHostCloudService

```kotlin
interface IHostCloudService : IComponent {

    /**
     * 是否自定义打开云服务页面
     */
    val isCustomOpenCloudPage: Boolean

    /**
     * 打开云服务页面
     *
     * @param deviceId 设备ID，为空字符串时表示进入云服务介绍页面（有设备列表可选进入单个设备页面)
     */
    suspend fun openCloudPage(deviceId: String): GWResult<Unit>
}
```

### IHostTrafficService

```kotlin
interface IHostTrafficService : IComponent {

    /**
     * 是否打开自定义流量页面
     */
    val isCustomOpenTrafficPage: Boolean

    /**
     * 打开流量页面
     *
     * @param deviceId 设备ID
     */
    suspend fun openTrafficPage(deviceId: String): GWResult<Unit>
}
```

## App 代码示例

### Swift

```swift
import GWIoTApi

// 注册自定义云服务
GWIoT.shared.registerHostCloudService(HostCloudService())

// 注册自定义流量服务
GWIoT.shared.registerHostTrafficService(HostTrafficService())

/// 自定义云服务实现
class HostCloudService: IHostCloudService {
    var isCustomOpenCloudPage: Bool { return true }

    func openCloudPage(deviceId: String, completionHandler: @escaping (GWResult<KotlinUnit>?, (any Error)?) -> Void) {
        // 自定义打开云服务页面逻辑
        // deviceId 为空时，表示进入云服务介绍页面
        if deviceId.isEmpty {
            // 打开云服务介绍页面
        } else {
            // 打开指定设备的云服务页面
        }
        completionHandler(GWResultSuccess(data: KotlinUnit()), nil)
    }
}

/// 自定义流量服务实现
class HostTrafficService: IHostTrafficService {
    var isCustomOpenTrafficPage: Bool { return true }

    func openTrafficPage(deviceId: String, completionHandler: @escaping (GWResult<KotlinUnit>?, (any Error)?) -> Void) {
        // 自定义打开流量页面逻辑
        // 例如：跳转到 App 自有的流量充值页面
        completionHandler(GWResultSuccess(data: KotlinUnit()), nil)
    }
}
```

### Kotlin

```kotlin
// 注册自定义云服务
GWIoT.registerHostCloudService(object : IHostCloudService {
    override val isCustomOpenCloudPage: Boolean = true

    override suspend fun openCloudPage(deviceId: String): GWResult<Unit> {
        // 自定义打开云服务页面逻辑
        // deviceId 为空时，表示进入云服务介绍页面
        return if (deviceId.isEmpty()) {
            // 打开云服务介绍页面
            GWResult.success(Unit)
        } else {
            // 打开指定设备的云服务页面
            GWResult.success(Unit)
        }
    }
})

// 注册自定义流量服务
GWIoT.registerHostTrafficService(object : IHostTrafficService {
    override val isCustomOpenTrafficPage: Boolean = true

    override suspend fun openTrafficPage(deviceId: String): GWResult<Unit> {
        // 自定义打开流量页面逻辑
        // 例如：跳转到 App 自有的流量充值页面
        return GWResult.success(Unit)
    }
})
```

## 注意事项

- `isCustomOpenCloudPage` 和 `isCustomOpenTrafficPage` 为 `true` 时，SDK 才会调用自定义的页面打开方法；返回 `false` 时仍会使用 SDK 默认页面
- `deviceId` 参数为空字符串时，表示用户从云服务/流量服务的入口页面进入，此时应展示对应的服务介绍或设备列表页面
- 建议在 SDK 初始化完成后、调用相关页面打开方法之前完成自定义服务的注册
