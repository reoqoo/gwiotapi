# Changelog

> [!NOTE]
> Only iterations of feature changes are recorded here, small versions for bug fixes are not recorded.

## 1.8.0
- Added Bluetooth permission request on scan page
- Optimized network switching for dual-module devices

## 1.7.3
- Added device sharing via invitation link feature
- Added push notification disable configuration option
- Updated Android fix version number
- Implemented BindType toInt method to convert BindType collection to integer bit mask
- Fixed known bugs

## 1.7.2
- Product configuration added appFuncMask
- Removed unused newSharePermissionMode
- Fixed known bugs

## 1.7.1
- Get device QR code without solution parameter
- Moved QR code string interface to IQRCodeComponent
- Exposed Device.saas property
- Feedback page no longer jumps when address is empty
- Help feedback page uses persistent storage
- Added new interface to get device QR code
- Fixed known bugs

## 1.7.0
- Logout SDK when token expires or is kicked out
- Added OEM cloud-cloud login authentication method flow, with SDK determining when to call
- Fixed known bugs

## 1.6.11
- Added initialization parameter enableEventReportOnlyMode
- Added Google push identifier
- Fixed known bugs

## 1.6.10
- Fixed issue where pushToken might still be uploaded after quick logout following login
- Fixed known bugs

## 1.6.9
- Added message count and device attribute listener related logs
- Updated message center interface
- Message center added read interface and local unread message count interface
- MAVEN_URL_SNAPSHOT address modified
- Added version number verification for packaging and uploading, added file backup
- Added SDK version number printing
- Fixed known bugs

## 1.6.8
- Device list sorted by binding time in descending order
- Fixed known bugs

## 1.6.7
- Added getLoginUser() interface
- Optimized initialization process
- Fixed issue where querying single device info did not return correct solution
- Fixed known bugs

## 1.6.6
- Added suspend cloud-cloud login method
- Adjusted local login initialization order to avoid slow cached login
- Fixed known bugs

## 1.6.5
- Merged YooseeKit 6.39
  - Adapted new device features such as grid battery, soft PTZ, etc., compatible with multiple camera types.
  - Added vertical screen fullscreen interaction for monitoring, smart guard, and playback.
  - Optimized event time points for cloud/card playback.
  - Supported "Cloud Playback Video Quality" setting for devices.
- Fixed known bugs

## 1.6.4
- Device list added lastBindTime field, sorting changed to descending order by this field
- Fixed known bugs

## 1.6.3
- Non-essential configurations like issueFeedbackBottomTips not placed in default constructor parameters
- UI configuration added issueFeedbackBottomTips
- Improved freeSecs parsing exception handling
- Region matching compatible with case-insensitive
- Configuration added free cloud storage video duration
- Fixed known bugs

## 1.6.2
- Added enter device settings page interface
- Optimized plugin interface solution judgment
- Fixed known bugs

## 1.6.1
- Stored UserConfig added appId
- When registering HostAccountService, remove old one first
- Fixed issue where push messages with specific solution could not find corresponding component when registered component's solution was null
- Improved user message handling
- Implemented pulling user messages on device homepage entry and handling welfare popups/jumps
- Added page lifecycle definitions, events, and components
- Fixed known bugs

## 1.6.0
- Added SDCard information
- When internally monitoring device changes to refresh device list, do not use request cache within 2s to avoid untimely refresh
- Clear uploadedPushToken during login to avoid push issues after re-login
- Fixed known bugs

## 1.5.11
- optimize import
- upsert device -> update device
- When querying single device info, if device is not in device list, do not insert/update to database
- Fixed issue where feedback problem interface output wrong log
- Fixed issue where updating single device info caused device list order change
- Fixed known bugs

## 1.5.10
- Notification processing needs to notify all corresponding components when there is a solution
- Added Beta environment configuration and clearing cache when switching environments
- Fixed known bugs

## 1.5.9
- Added beta domain configuration
- Optimized user login
- Fixed known bugs

## 1.5.8
- UI default set to Yoosee theme color
- Optimized default theme color
- Optimized device event handling
- Initialization language, UI, Host changed to synchronous call
- Full replacement of old product info when updating
- Fixed known bugs

## 1.5.7
- Added DeviceEvent, rename and delete device events
- Fixed known bugs

## 1.5.6
- GWIoTError implemented LocalizedError protocol
- Account sharing interface changed, no longer distinguishes account type
- Device added sort to avoid order changes when querying from database
- Fixed member status=0 parsing failure
- Fixed known bugs

## 1.5.5
- Improved HostAccountService logs
- Fixed lazy crash on iOS main thread
- Added check device upgrade interface
- Disabled mandatory use of login(c2c:) for Gwell accounts
- Fixed known bugs

## 1.5.4
- Optimized pushToken upload, if App layer calls when account is not logged in, SDK internally uploads after account login
- When new user logs in, if previous user didn't call logout in App layer, logout first
- Fixed known bugs

## 1.5.3
- Updated device sharing initialization parameters
- Renamed host account info related interfaces, removed userId/accessId conversion code
- Fixed known bugs

## 1.5.2
- Added member service related interfaces and model parsing
- Fixed member info parsing failure
- Fixed issue where list sorting changed after querying and updating single device info
- Fixed known bugs

## 1.5.1
- Judged whether observeForever in SDK uses main thread based on platform
- Added Android platform layer implementation
- Fixed known bugs

## 1.5.0
- Product configuration table platform config loaded/notified after change
- Added retry mechanism for loading product configuration table failure during initialization
- Added database table PlatformConfig; appName, mainVersion and other platform fields obtained from configuration table
- Refresh device list after successful binding
- Fixed known bugs

## 1.4.9
- ProductInfo added androidMinVersion and rawJson fields
- Product model added numberOfViews, iosminversion parameters
- Fixed known bugs

## 1.4.8
- Compatible with new product configuration table products parameter
- Fixed known bugs

## 1.4.7
- Added enter/submit issue feedback interface
- Submitted database schemas
- Fixed known bugs

## 1.4.6
- Device.Saas added product_model field
- ProductInfo added bindSolution, bleMID and blePID fields
- Fixed known bugs

## 1.4.5
- Added configuration parameter for whether to show feedback history
- Fixed known bugs

## 1.4.4
- uploadPushToken interface added clearOtherTerm parameter
- Trigger propsChanged after refreshing getIoTProps successfully
- Fixed known bugs

## 1.4.3
- Initialization added disableMultipleLogins parameter to disable multi-terminal login
- iOS PlayerView's UIView changed from inheritance to property
- Fixed known bugs

## 1.4.2
- Initialization parameters specialized for Android platform
- Android View set to FrameLayout
- Android View set to inheritable
- Fixed issue where single View obtained could be null
- Fixed known bugs

## 1.4.1
- Modified language configuration
- Fixed known bugs

## 1.4.0
- Player interface definition adjusted
- Improved player protocol
- Fixed known bugs

## 1.3.6
- Modified player definition
- Fixed known bugs

## 1.3.5
- Added device (phone) memory provision component
- rename phoneUUID -> phoneUniqueId
- Added phoneUUID property
- Fixed known bugs

## 1.3.4
- Added smart guard
- Removed initialization parameter resolutionByDefault
- Fixed known bugs

## 1.3.3
- Initialization added soundOnByDefault, resolutionByDefault parameters
- Fixed known bugs

## 1.3.2
- Initialization added sharing mode field
- Fixed known bugs

## 1.3.1
- uploadPushToken public interface removed termId parameter
- Modified sdkInitFinish type to MutableLiveData
- Fixed iOS initialization crash
- Modified same-screen interface
- Fixed known bugs

## 1.3.0
- Added same-screen feature switch
- Device added jsonString getter
- Added LiveData listener for initialization completion
- Fixed known bugs

## 1.2.13
- Initialization needs to use coroutine suspend to ensure complete initialization before proceeding
- Added device power supply info
- Fixed known bugs

## 1.2.12
- Adapted Android initialization parameters
- Fixed known bugs

## 1.2.11
- Added interface to enter bindable product list page
- Push adapted for Android platform
- Fixed known bugs

## 1.2.10
- Added method to get path of device's most recent screenshot
- Fixed known bugs

## 1.2.9
- Logout, not concerned with interface return result, continue with subsequent process
- Fixed known bugs

## 1.2.8
- Logout, adapted Android logic
- Fixed known bugs

## 1.2.7
- Scan callback can be empty
- Fixed known bugs

## 1.2.6
- Removed playback duration parameter
- Modified enter bind device interface, added entry through product info
- Fixed known bugs

## 1.2.5
- Scan QR code added callback to control closing page
- Fixed known bugs

## 1.2.4
- Added playback default time in initOption
- Fixed offline message parsing field issue
- Fixed known bugs

## 1.2.3
- Scan interface modified, added ScanQRCodeOptions parameter to define scan page title, description, and whether one-stop service is needed
- AlbumConfig iOS/Android configuration separated based on actual situation
- Fixed known bugs

## 1.2.2
- AlbumConfig iOS/Android configuration separated based on actual situation
- Fixed known bugs

## 1.2.1
- Added login using cloud-cloud user authentication info [UserC2CInfo]
- Fixed known bugs

## 1.2.0
- Added [Message Center Interface](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.cross_platform/-i-message-center-component/index.html)
- Fixed known bugs

## 1.1.8
- Fixed ShareDevice related properties to ensure they can be accessed
- LiveData Observer needs to be monitored in main thread
- Fixed known bugs

## 1.1.7
- Added QR code related interfaces, including QR code recognition and entering scan page functionality, see [IQRCodeComponent](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.cross_platform/-i-q-r-code-component/index.html)
- Added [Accept Shared Device Interface](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.sub/-i-dev-share-component/index.html)
- Fixed known bugs

## 1.1.6
- Fixed scope.launch crash issue on iOS
- Updated documentation comments
- Fixed known bugs

## 1.1.5
- Synchronized interface versions between Android and iOS
- Added `disableAccountService` to initialization parameters `InitOptions`, APPs that don't use Gwell Account service need to set it to `true`
- Added custom help page interface, see [IHelperPageComponent](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.cross_platform/-i-helper-page-component/index.html)
- Added interface for customizing some UI, see [IUIConfigurationComponent](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.sub/-i-u-i-configuration-component/index.html)
- Fixed known bugs

## 1.1.4
- Theme color and icon configuration changed to optional
- Fixed known bugs

## 1.1.3
- Fixed app layer enter HelperPage interface
- Fixed known bugs

## 1.1.2
- Modified cloud-assigned cid
- Added icon configuration
- Fixed known bugs

## 1.1.1
- Adjusted Theme structure and property naming
- Custom open help and feedback page
- Login update user info using postValue
- Fixed known bugs

## 1.1.0
- InitOptions added disableAccountService parameter
- Android added necessary platform parameters
- Modified dependency relationships
- Fixed known bugs
