# Changelog
> [!NOTE]
> 这里只记录需求迭代变更，修复bug的小版本不记录。
## 1.9.0
- 移动应用安全问题修复
- 支持[自定义插件云服务和4G流量页面跳转](app_custom_services_page.md)
- 支持自定义插件主题色
- 合并Yoosee 6.44基线功能
  - AOV 2.0功能
  - 增加哭声检测相关功能设置
  - 优化绑定后的设置名称及用途
- 其他体验优化和debug

## 1.8.0
- 新增扫码页蓝牙权限申请
- 双模设备联网方式切换优化


## 1.7.3
- 添加通过邀请链接分享设备功能
- 添加推送通知禁用配置选项
- 修复已知的Bugs

## 1.7.2
- 产品配置增加appFuncMask
- 移除无用的newSharePermissionMode
- 修复已知的Bugs

## 1.7.1
- 获取设备二维码，去掉solution参数
- 获取二维码字符串接口挪到IQRCodeComponent
- 公开Device.saas属性
- 新增获取设备二维码接口
- 修复已知的Bugs

## 1.7.0
- token过期/踢飞时登出SDK
- 增加OEM云云登录认证方法流程，由SDK决定调用时机
- 修复已知的Bugs

## 1.6.11
- 增加初始化参数enableEventReportOnlyMode
- 新增Google是否Google推送标识
- 修复已知的Bugs

## 1.6.10
- 修复登录后快速退出账号，可能导致退出后还上传pushToken的问题
- 修复已知的Bugs

## 1.6.9
- 增加消息数量和设备属性监听相关日志
- 更新消息中心接口
- 消息中心增加已读接口和本地未读消息数量接口
- MAVEN_URL_SNAPSHOT地址修改
- 打包和上传新增版本号校验，新增文件备份
- 新增SDK版本号打印
- 修复已知的Bugs

## 1.6.8
- 查询设备列表按绑定时间倒序返回
- 修复已知的Bugs

## 1.6.7
- 增加getLoginUser()接口
- 优化初始化流程
- 修复查询单个设备信息没有返回正确solution的问题
- 修复已知的Bugs

## 1.6.6
- 增加suspend云云对接登录方法
- 调整初始化本地登录顺序，避免缓存登录慢问题
- 修复已知的Bugs

## 1.6.5
- 合并YooseeKit 6.39
  - 适配格子电池、软云台等新设备特性，兼容多类型摄像头。
  - 监控、智能守护、回放新增竖屏全屏交互。
  - 云 / 卡回放事件时间点逻辑优化。
  - 设备支持 "云回放录像质量" 设置。
- 修复已知的Bugs

## 1.6.4
- 设备列表增加lastBindTime字段，排序改为此字段降序
- 修复已知的Bugs

## 1.6.3
- issueFeedbackBottomTips等非必要配置不放到默认构造参数
- UI配置增加issueFeedbackBottomTips
- 完善freeSecs解析异常处理
- 地区匹配兼容不区分大小写
- 配置文件增加免费云存视频时长
- 修复已知的Bugs

## 1.6.2
- 增加进入设备设置页面接口
- 优化插件接口的solution判断
- 修复已知的Bugs

## 1.6.1
- 存储UserConfig增加appId
- 注册HostAccountService时，先移除旧的
- 解决push消息存在具体solution时但注册组件的solution是null的问题
- 完善处理用户消息
- 实现进入设备首页拉取用户消息，并处理福利弹窗/跳转
- 增加页面生命周期定义、事件和组件
- 修复已知的Bugs

## 1.6.0
- 增加SDCard信息
- 内部监听设备变化刷新设备列表时，不使用2s内的请求缓存，避免这些场景没及时刷新
- 登录时清除uploadedPushToken，避免重新登录后没上传pushToken导致推送问题
- 修复已知的Bugs

## 1.5.11
- optimize import
- upsert device -> update device
- 查询单个设备信息，如果此设备不在设备列表中，不插入/更新到数据库
- 修复反馈问题接口固定输出了错误日志的问题
- 修复更新单个设备信息导致设备列表顺序发生变化的问题
- 修复已知的Bugs

## 1.5.10
- 通知处理有solution时需要通知所有对应组件
- 增加Beta环境配置以及切换环境清除缓存
- 修复已知的Bugs

## 1.5.9
- 增加beta域名配置
- 优化用户登陆
- 修复已知的Bugs

## 1.5.8
- UI默认设置为Yoosee主题色
- 优化默认主题色
- 优化设备事件处理
- 初始化语言、UI、Host改为同步调用
- 更新产品信息时全量替换旧的
- 修复已知的Bugs

## 1.5.7
- 增加DeviceEvent，重命名和删除设备事件
- 修复已知的Bugs

## 1.5.6
- GWIoTError实现LocalizedError协议
- 账号分享接口变更，不区分账号类型
- Device增加sort，避免从数据库查询顺序发生变化
- 修复会员状态=0解析失败的问题
- 修复已知的Bugs

## 1.5.5
- 完善HostAccountService日志
- 修复lazy在iOS主线程闪退的问题
- 增加检查设备升级接口
- 禁用技威账号不强制使用login(c2c:)
- 修复已知的Bugs

## 1.5.4
- 优化上传pushToken，App层如果在账号没登陆时调用，SDK内部在账号登录后上传
- 新用户登录时，上个用户如果App层没调logout，先logout
- 修复已知的Bugs

## 1.5.3
- 更新设备分享方式初始化参数
- 宿主账号信息相关接口重命名，移除userId/accessId转换代码
- 修复已知的Bugs

## 1.5.2
- 增加会员服务相关接口及模型解析
- 修复会员信息解析失败的问题
- 修复查询更新单个设备信息后列表排序发生改变的问题
- 修复已知的Bugs

## 1.5.1
- 根据平台判断SDK内使用observeForever是否主线程
- 新增Android平台层的实现
- 修复已知的Bugs

## 1.5.0
- 产品配置表的平台配置加载/变更后通知插件
- 增加初始化时加载产品配置表失败重试机制
- 新增数据库表PlatformConfig；appName、mainVersion等平台字段从配置表获取
- 绑定成功刷新设备列表
- 修复已知的Bugs

## 1.4.9
- ProductInfo增加androidMinVersion和rawJson字段
- Product模型新增numberOfViews、iosminversion参数
- 修复已知的Bugs

## 1.4.8
- 兼容新产品配置表products参数
- 修复已知的Bugs

## 1.4.7
- 增加进入/提交问题反馈的接口
- 提交数据库schemas
- 修复已知的Bugs

## 1.4.6
- Device.Saas增加product_model字段
- ProductInfo增加bindSolution、bleMID和blePID字段
- 修复已知的Bugs

## 1.4.5
- 增加是否展示反馈历史的配置参数
- 修复已知的Bugs

## 1.4.4
- 上传pushToken接口增加clearOtherTerm参数
- 刷新getIoTProps成功后，触发propsChanged
- 修复已知的Bugs

## 1.4.3
- 初始化增加禁用多终端登录参数disableMultipleLogins
- iOS PlayerView的UIView从继承改为属性
- 修复已知的Bugs

## 1.4.2
- 针对安卓平台，初始化参数进行特性化
- 把Android的View设置为FrameLayout
- 把Android的View设置为可继承
- 修复获取的单个View可能为null的问题
- 修复已知的Bugs

## 1.4.1
- 修改语言配置
- 修复已知的Bugs

## 1.4.0
- 播放器接口定义调整
- 完善player协议
- 修复已知的Bugs

## 1.3.6
- 修改播放器的定义
- 修复已知的Bugs

## 1.3.5
- 新增设备（手机）内存提供组件
- rename phoneUUID -> phoneUniqueId
- 增加phoneUUID属性
- 修复已知的Bugs

## 1.3.4
- 新增智能守护
- 移除初始化参数resolutionByDefault
- 修复已知的Bugs

## 1.3.3
- 初始化新增soundOnByDefault、resolutionByDefault参数
- 修复已知的Bugs

## 1.3.2
- 初始化新增分享方式的字段
- 修复已知的Bugs

## 1.3.1
- uploadPushToken公开接口移除termId参数
- 修改sdkInitFinish的类型为MutableLiveData
- 修复iOS初始化crash的问题
- 修改同屏接口
- 修复已知的Bugs

## 1.3.0
- 增加同屏功能开关
- Device增加jsonString getter
- 初始化结束新增一个LiveData做监听
- 修复已知的Bugs

## 1.2.13
- 初始化需要使用协程suspend确保完全初始化完成才可以下一步
- 增加设备供电信息
- 修复已知的Bugs

## 1.2.12
- 适配Android端的初始化参数
- 修复已知的Bugs

## 1.2.11
- 增加进入可绑定产品列表页面的接口
- 推送适配Android平台
- 修复已知的Bugs

## 1.2.10
- 增加获取设备最近截图的路径的方法
- 修复已知的Bugs

## 1.2.9
- 退出登录，不关心接口的返回结果，继续走下面的流程
- 修复已知的Bugs

## 1.2.8
- 退出登录，适配安卓逻辑
- 修复已知的Bugs

## 1.2.7
- 扫码回调可以传空
- 修复已知的Bugs

## 1.2.6
- 移除回放时长的参数
- 进入绑定设备接口修改，增加通过产品信息进入
- 修复已知的Bugs

## 1.2.5
- 扫描二维码新增一个回调，用来控制关闭页面
- 修复已知的Bugs

## 1.2.4
- initOption里新增一个回放默认时间
- 修复离线消息解析字段的问题
- 修复已知的Bugs

## 1.2.3
- 扫码接口修改，新增ScanQRCodeOptions参数，可定义扫码页的title、描述，以及是否需要一条龙服务
- AlbumConfig iOS/Android配置按实际情况区分开来
- 修复已知的Bugs

## 1.2.2
- AlbumConfig iOS/Android配置按实际情况区分开来
- 修复已知的Bugs

## 1.2.1
- 增加使用云云对接的用户认证信息[UserC2CInfo]进行登录
- 修复已知的Bugs

## 1.2.0
- 增加[消息中心接口](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.cross_platform/-i-message-center-component/index.html)
- 修复已知的Bugs

## 1.1.8
- ShareDevice相关属性修复，确保可以被访问到
- LiveData的Observer需要在主线程中进行监听
- 修复已知的Bugs

## 1.1.7
- 增加二维码相关接口，包含识别二维码以及进入扫码页面功能，详见[IQRCodeComponent](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.cross_platform/-i-q-r-code-component/index.html)
- 增加[接受分享设备接口](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.sub/-i-dev-share-component/index.html)
- 修复已知的Bugs

## 1.1.6
- 修复scope.launch在iOS上会大概率出现崩溃的问题
- 更新文档注释
- 修复已知的Bugs

## 1.1.5
- Android、iOS同步接口版本
- 初始化参数`InitOptions`增加`disableAccountService`，不使用Gwell Account服务的APP需要配置为`true`.
- 增加自定义帮助页面接口，详见[IHelperPageComponent](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.cross_platform/-i-helper-page-component/index.html)
- 增加自定义部分UI的接口，详见[IUIConfigurationComponent](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi.components.sub/-i-u-i-configuration-component/index.html)
- 修复已知的Bugs

## 1.1.4
- 主题颜色、图标配置改为可选配置
- 修复已知的Bugs

## 1.1.3
- app层进入HelperPage接口修复
- 修复已知的Bugs

## 1.1.2
- 修改云端分配的cid
- 加入图标配置
- 修复已知的Bugs

## 1.1.1
- 调整Theme结构及属性命名
- 自定义打开帮助与反馈页面
- 登录更新数据用户信息使用postValue
- 修复已知的Bugs

## 1.1.0
- InitOptions增加参数disableAccountService
- Android端新增必要平台参数
- 修改依赖关系，各自依赖
- 修复已知的Bugs