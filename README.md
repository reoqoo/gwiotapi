GWIoTApi是技威开发的App端物联网设备插件化支持SDK，方便合作厂商快速开发定制自有品牌的App，通过接入技威已实现的各型号产品插件，即可实现设备直播、回放、设置等功能。

目前支持iOS、Android。

## 开发流程
1. 向我司商务或者相关接口人申请开发者账号，登录开发者平台创建及配置应用、产品
2. 按各平台集成说明集成SDK
- [iOS集成说明](ios/demo/README.md)
- [Android集成说明](android/demo/README.md)
3. 调用SDK接口实现各模块功能
- 开发App之前，请阅读[App开发指南](docs/app_develop_guide.md)，了解SDK初始化、登录注册等关键代码实现，并参考各端demo了解SDK的使用方式。
- [iOS Demo](ios/demo)
- [Android Demo](android/demo)

## 已实现功能
目前，SDK已实现开发App所需的各项功能/接口，包括但不限于以下列出的：
- [x] 账号接口，包括登录、注册、注销、修改密码及其他个人信息等
- [x] 设备接口，包括设备列表、设备详情、设备控制等
- [x] 设备插件，内置的完整设备功能UI组件，包括直播、回放、设置等功能
- [x] 相册组件，内置默认的设备相册UI组件，管理设备截图、录像
- [x] ...
> 如有其他需要求/建议，可直接联系我司商务或者相关接口人

## 接口文档
SDK完整接口文档请查阅:

[GWIoTApi Referfence](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)

[更新日志](docs/api_change_log.md)
