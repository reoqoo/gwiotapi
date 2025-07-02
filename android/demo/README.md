[API Referfence](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)

# Android GWIot Demo项目
GWIot Demo项目，用于演示GWIot API的使用。

## 0.注意⚠️⚠️⚠️

当前版本为 ```1.1.5.0``` 后续版本更新，当前Readme为此版本的适配方案，后续版本更新后，会实时更新到当前README文档，以及demo项目

## 1.配置

1.1

[settings.gradle](settings.gradle) 文件中配置与Maven相关的仓库信息。


```gradle
maven {
    url 'https://nexus-sg.gwell.cc/nexus/repository/maven-releases/'
    credentials {
        username = 'iptime_eti_user'
        password = '6S1Moa^HFaL!rEqQC'
    }
    allowInsecureProtocol true
}
maven {
    url 'https://nexus-sg.gwell.cc/nexus/repository/maven-gwiot/'
    credentials {
        username = 'iptime_eti_user'
        password = '6S1Moa^HFaL!rEqQC'
    }
    allowInsecureProtocol true
}
maven {
    url 'https://mvn.zztfly.com/android'
    content {
        includeGroup "cn.fly"
        includeGroup "cn.fly.verify"
        includeGroup "cn.fly.verify.plugins"
    }
}
maven {
    url 'https://developer.huawei.com/repo/'
    content {
        includeGroupByRegex "com\\.huawei.*"
    }
}
maven {
    url "https://artifact.bytedance.com/repository/Volcengine/"
}
maven {
    url "https://artifact.bytedance.com/repository/pangle/"
}
```

1.2 

app模块下的[build.gradle](app/build.gradle)文件配置模块依赖。

```gradle
apply plugin: 'kotlin-kapt'

android {
    buildFeatures {
        dataBinding true
    }
    dataBinding {
        enabled = true
    }
    packagingOptions {
        pickFirst '**/libc++_shared.so'
        pickFirst '**/libgwmarsxlog.so'
        pickFirst '**/libavcodec.so'
        pickFirst '**/libavfilter.so'
        pickFirst '**/libavformat.so'
        pickFirst '**/libavutil.so'
        pickFirst '**/libcrypto.1.1.so'
        pickFirst '**/libgwbase.so'
        pickFirst '**/libssl.1.1.so'
        pickFirst '**/libswresample.so'
        pickFirst '**/libswscale.so'
        pickFirst '**/libxml2.so'
        pickFirst '**/libgwplayer.so'

        pickFirst '**/libaudiodsp_dynamic.so'
        pickFirst '**/libtxTraeVoip.so'
        pickFirst '**/libcurl.so'
        pickFirst '**/libijkffmpeg.so'
        pickFirst '**/libijkplayer.so'
        pickFirst '**/libijksdl.so'
        pickFirst '**/libbleconfig.so'
        pickFirst '**/libiotvideomulti.so'
        pickFirst '**/libmbedtls.so'
    }
}

dependencies { 
    implementation "com.gwell:gwiotapi:0.0.18.0"
    var yooseeKitVersion = "google-release-1.1.35"
    implementation("com.yoosee.gw_plugin_hub:impl_main:${yooseeKitVersion}") {
        exclude group: 'com.google.android.material'
        exclude(group: 'androidx.activity', module: 'activity-ktx')
        exclude(group: 'com.gwell', module: 'gwiotapi')
    }
}

```

## 2.初始化

2.1 在Application中初始化GWIot SDK。

```kotlin


override fun onCreate() {
    super.onCreate()
    val app = this

    // initSdk
    val option = InitOptions(
        app = this.application,
        versionName = "01.05.25",
        versionCode = 1087,
        appConfig = AppConfig(
            appId = "",
            appToken = "",
            appName = "",
            cId = "8.1"
        ),
    )
    GWIoT.initialize(option)
}

```

## 3.使用

3.1 登录SDK

```kotlin
val info = object : IUserAccessInfo {
    override var accessId: String = "123"
    override var accessToken: String = ""
    override var area: String = ""
    override var expireTime: String = ""
    override var regRegion: String = ""
    override var terminalId: String = ""
    override var userId: String = ""
}
GWIoT.login(info)
```
3.2 开启配网流程

```kotlin
val options = BindOptions()
GWIoT.openBind(options)
```

3.3 开启监控

```kotlin
val devRet = GWIoT.queryDeviceCacheFirst("devId")
var device: IDevice? = null
if (devRet is GWResult.Success) {
    device = devRet.data
}
device ?: throw RuntimeException("没有Device")
GWIoT.openHome(OpenPluginOption(device = device))
```

> 更多API的使用方法可查询[API Referfence](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)