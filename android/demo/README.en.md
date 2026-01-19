[API Reference](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)

# Android GWIot Demo Project
GWIot Demo project, used to demonstrate the usage of GWIot API.

## 0. Notice⚠️⚠️⚠️

The current version is ```1.2.12.0```. For subsequent version updates, the current Readme is the adaptation solution for this version. After subsequent version updates, it will be updated to the current README document and demo project in real-time.

## 1. Configuration

1.1

Configure Maven-related repository information in the [settings.gradle](settings.gradle) file.


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

Configure module dependencies in the [build.gradle](app/build.gradle) file under the app module.

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
    implementation "com.gwell:gwiotapi:1.1.9.0"
    implementation("com.yoosee.gw_plugin_hub:impl_main:google-release-6.32.2.0.10") {
        exclude group: 'com.google.android.material'
        exclude(group: 'com.yoosee.gw_plugin_hub', module: 'liblog_release')
        exclude(group: 'com.gwell', module: 'iotvideo-multiplatform')
        exclude(group: 'com.gwell', module: 'cloud_player')
        exclude(group: 'androidx.activity', module: 'activity-ktx')
        exclude(group: 'com.gwell', module: 'gwiotapi')
        exclude(group: 'com.tencentcs', module: 'txtraevoip')
    }
    
    
    def reoqooV = "google-release-01.05.25.0.10"
    implementation "com.reoqoo.gw_plugin_hub:main:$reoqooV"
}

```

## 2. Initialization

2.1 Initialize the GWIot SDK in the Application.

```kotlin


override fun onCreate() {
    super.onCreate()

    val option = InitOptions(
        app = this,
        versionName = BuildConfig.GWIOT_VERSION_NAME,
        versionCode = BuildConfig.GWIOT_VERSION_CODE,
        appConfig = AppConfig(
            appId = BuildConfig.GWIOT_APP_ID,
            appToken = BuildConfig.GWIOT_APP_TOKEN,
            appName = BuildConfig.GWIOT_APP_NAME,
            cId = BuildConfig.GWIOT_CID,
        ),
        mainActvityKlass = MainActivity::class.java as Class<Activity>,
    )
    option.brandDomain = BuildConfig.GWIOT_BRAND_DOMAIN
    option.disableAccountService = true
    val snapshotDir = "${this.getExternalFilesDir(null)}${File.separator}iotplugin${File.separator}ScreenShots"
    val recordDir = "${this.getExternalFilesDir(null)}${File.separator}iotplugin${File.separator}RecordVideo"
    option.albumConfig = AlbumConfig(
        snapshotDir = snapshotDir,
        recordDir = recordDir,
        watermarkConfig = null
    )
    GWIoT.initialize(option)
}

```

## 3. Usage

3.1 Login to SDK

```kotlin
val info = UserC2CInfo(
    accessId = TODO(),
    accessToken = TODO(),
    expireTime = TODO(),
    terminalId = TODO(),
    expend = TODO()
)
GWIoT.login(info)
```
3.2 Start Network Configuration Process

```kotlin
val options = BindOptions()
GWIoT.openBind(options)
```

3.3 Start Monitoring

```kotlin
val devRet = GWIoT.queryDeviceCacheFirst("devId")
var device: IDevice? = null
if (devRet is GWResult.Success) {
    device = devRet.data
}
device ?: throw RuntimeException("No Device")
GWIoT.openHome(OpenPluginOption(device = device))
```

3.4 Theme Settings

```xml
<application
    android:theme="@style/CustomDarkModeTheme">
</application>
```

3.4 Push Processing

```kotlin
class LauncherActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Offline push processing
        runOnUiThread {
            GWIoT.receivePushNotification(PushNotification(intent = intent))
        }
    }
}

```

> For more API usage methods, please refer to [API Reference](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)