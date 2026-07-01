# Changelog

> [!NOTE]
> Only iterations of feature changes are recorded here, small versions for bug fixes are not recorded.

## 1.8.0
- Added Bluetooth permission request on scan page
- Optimized network switching for dual-module devices

## 1.7.1
- Low-power device power limitation acquisition model
- Added UI prompt for unopened cloud service in interval settings
- Replaced illustrations in area guard guide 2
- 4G device monitoring page/device list does not display "Cloud Service" icon entry
- Device security code
- Fixed some bugs

## 1.6.5
- Merged YooseeKit 6.39
  - Adapted new device features such as grid battery, soft PTZ, etc., compatible with multiple camera types.
  - Added vertical screen fullscreen interaction for monitoring, smart guard, and playback.
  - Optimized event time points for cloud/card playback.
  - Supported "Cloud Playback Video Quality" setting for devices.
- Fixed known bugs

## 1.2.0
- Added [Message Center Interface](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.cross_platform/-i-message-center-component/index.html)


## 1.1.7
- Added QR code related interfaces, including QR code recognition and entering scan page functionality, see [IQRCodeComponent](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.cross_platform/-i-q-r-code-component/index.html)
- Added [Accept Shared Device Interface](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.sub/-i-dev-share-component/index.html)
- Fixed known bugs


## 1.1.5
- Synchronized interface versions between Android and iOS
- Added `disableAccountService` to initialization parameters `InitOptions`, APPs that don't use Gwell Account service need to set it to `true`
- Added custom help page interface, see [IHelperPageComponent](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.cross_platform/-i-helper-page-component/index.html)
- Added interface for customizing some UI, see [IUIConfigurationComponent](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.sub/-i-u-i-configuration-component/index.html)
- Fixed known bugs



