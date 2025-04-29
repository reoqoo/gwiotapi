GWIoTApi is an app-side IoT device plug-in support SDK developed by Jiwei, which facilitates cooperative manufacturers to quickly develop and customize their own brand apps. By connecting to various product plug-ins that Jiwei has implemented, you can realize the functions of device live broadcast, playback, and settings.

Currently supports iOS and Android.

## 开发流程

1.  Apply for a developer account from our business or related interface person, log in to the developer platform to create and configure applications and products
2.  Integrate SDK according to the integration instructions of each platform

-   [iOS integration instructions](ios/README.md)
-   [Android Integration Instructions](android/README.md)

3.  Calling the SDK interface to realize the functions of each module

-   Before developing the app, please read[App Development Guide](docs/app_develop_guide.md), understand the implementation of key codes such as SDK initialization, login and registration, and refer to each demo to understand how SDK is used.
-   [iOS Demo](ios/demo)
-   [Android Demo](android/demo)

## Implemented functions

At present, the SDK has implemented various functions/interfaces required for developing an App, including but not limited to the following:

-   [x] Account interface, including login, registration, cancellation, password modification and other personal information, etc.
-   [x] Equipment interface, including device list, device details, device control, etc.
-   [x] Device plug-in, built-in complete device function UI components, including live broadcast, playback, settings and other functions
-   [x] Album component, built-in default device album UI component, manage device screenshots and video recordings
-   [x] ...
    > If you have any other requests/suggestions, you can contact our business or relevant interface person directly

## Interface Documentation

For the complete SDK interface documentation, please refer to:

[GWIOT API REFERENCE](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)
