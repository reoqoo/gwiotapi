<div align="right">
  <a href="./README.md" style="display: inline-block; padding: 6px 12px; background-color: #0366d6; color: white; text-decoration: none; border-radius: 4px; font-weight: bold;">中文</a>
</div>

GWIoTApi is an App-side IoT device plug-in support SDK developed by Gwell, which facilitates partner manufacturers to quickly develop and customize their own branded Apps. By integrating Gwell's already implemented product plugins for various models, you can achieve functions such as device live streaming, playback, and settings.

Currently supports iOS and Android platforms.

## Development Process
1. Apply for a developer account from our business or relevant contact person, log in to the developer platform to create and configure applications and products
2. Integrate the SDK according to each platform's integration instructions
- [iOS Integration Instructions](ios/demo/README.en.md)
- [Android Integration Instructions](android/demo/README.en.md)
3. Call SDK interfaces to implement various module functions
- Before developing the App, please read the [App Development Guide](docs/app_develop_guide.en.md) to understand key code implementations such as SDK initialization, login and registration, and refer to the demos of each platform to understand how to use the SDK.
- [iOS Demo](ios/demo)
- [Android Demo](android/demo)

## Implemented Functions
Currently, the SDK has implemented various functions/interfaces required for App development, including but not limited to the following:
- [x] Account interfaces, including login, registration, logout, password modification, and other personal information management
- [x] Device interfaces, including device list, device details, device control, etc.
- [x] Device plugins, built-in complete device function UI components, including live streaming, playback, settings, etc.
- [x] Album component, built-in default device album UI component for managing device screenshots and recordings
- [x] ...
> If you have other requirements/suggestions, please contact our business or relevant contact person directly

## API Documentation
For the complete SDK API documentation, please refer to:

[GWIoTApi Reference](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)

[Update Log](docs/api_change_log.en.md)