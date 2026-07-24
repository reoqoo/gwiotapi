# Custom Cloud Service and Traffic Page Navigation

## Overview

The SDK provides built-in cloud service and traffic service pages by default. When the App needs to customize the navigation logic for these pages (for example, redirecting to the App's own H5 page, native page, or handling it in other ways), it can implement the `IHostCloudService` and `IHostTrafficService` interfaces to take over the page opening behavior.

## How It Works

When the SDK needs to open a cloud service page or traffic service page, it processes in the following priority order:

1. Check if the App has registered the corresponding custom service (`IHostCloudService` / `IHostTrafficService`)
2. If registered and `isCustomOpenCloudPage` / `isCustomOpenTrafficPage` returns `true`, the SDK calls the App's implemented `openCloudPage` / `openTrafficPage` methods
3. If not registered or returns `false`, the SDK uses the default page opening method

## Interface Definitions

### IHostCloudService

```kotlin
interface IHostCloudService : IComponent {

    /**
     * Whether to use a custom cloud service page
     */
    val isCustomOpenCloudPage: Boolean

    /**
     * Open the cloud service page
     *
     * @param deviceId Device ID. An empty string means entering the cloud service introduction page 
     *                 (with a device list to select and enter a single device page)
     */
    suspend fun openCloudPage(deviceId: String): GWResult<Unit>
}
```

### IHostTrafficService

```kotlin
interface IHostTrafficService : IComponent {

    /**
     * Whether to use a custom traffic page
     */
    val isCustomOpenTrafficPage: Boolean

    /**
     * Open the traffic page
     *
     * @param deviceId Device ID
     */
    suspend fun openTrafficPage(deviceId: String): GWResult<Unit>
}
```

## App Code Examples

### Swift

```swift
import GWIoTApi

// Register custom cloud service
GWIoT.shared.registerHostCloudService(HostCloudService())

// Register custom traffic service
GWIoT.shared.registerHostTrafficService(HostTrafficService())

/// Custom cloud service implementation
class HostCloudService: IHostCloudService {
    var isCustomOpenCloudPage: Bool { return true }

    func openCloudPage(deviceId: String, completionHandler: @escaping (GWResult<KotlinUnit>?, (any Error)?) -> Void) {
        // Custom logic for opening the cloud service page
        // When deviceId is empty, it means entering the cloud service introduction page
        if deviceId.isEmpty {
            // Open cloud service introduction page
        } else {
            // Open cloud service page for the specified device
        }
        completionHandler(GWResultSuccess(data: KotlinUnit()), nil)
    }
}

/// Custom traffic service implementation
class HostTrafficService: IHostTrafficService {
    var isCustomOpenTrafficPage: Bool { return true }

    func openTrafficPage(deviceId: String, completionHandler: @escaping (GWResult<KotlinUnit>?, (any Error)?) -> Void) {
        // Custom logic for opening the traffic page
        // For example, redirect to the App's own traffic top-up page
        completionHandler(GWResultSuccess(data: KotlinUnit()), nil)
    }
}
```

### Kotlin

```kotlin
// Register custom cloud service
GWIoT.registerHostCloudService(object : IHostCloudService {
    override val isCustomOpenCloudPage: Boolean = true

    override suspend fun openCloudPage(deviceId: String): GWResult<Unit> {
        // Custom logic for opening the cloud service page
        // When deviceId is empty, it means entering the cloud service introduction page
        return if (deviceId.isEmpty()) {
            // Open cloud service introduction page
            GWResult.success(Unit)
        } else {
            // Open cloud service page for the specified device
            GWResult.success(Unit)
        }
    }
})

// Register custom traffic service
GWIoT.registerHostTrafficService(object : IHostTrafficService {
    override val isCustomOpenTrafficPage: Boolean = true

    override suspend fun openTrafficPage(deviceId: String): GWResult<Unit> {
        // Custom logic for opening the traffic page
        // For example, redirect to the App's own traffic top-up page
        return GWResult.success(Unit)
    }
})
```

## Notes

- When `isCustomOpenCloudPage` and `isCustomOpenTrafficPage` are `true`, the SDK will call the custom page opening methods; when `false`, the SDK will still use the default pages
- When the `deviceId` parameter is an empty string, it means the user is entering from the cloud service/traffic service entry page. At this time, the corresponding service introduction or device list page should be displayed
- It is recommended to complete the registration of custom services after the SDK initialization is complete and before calling the related page opening methods
