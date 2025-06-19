package com.gw.component_push.manager

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.google.gson.JsonObject
import com.gw.component_push.api.interfaces.INotifyServer
import com.gw.component_push.datastore.PushDataStore
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw.cp_config.api.IAppParamApi
import com.gw_reoqoo.lib_utils.ktx.getAppVersionName
import com.gw.player.entity.ErrorInfo
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.iotvideo.constant.IoTError
import com.jwkj.iotvideo.init.IoTVideoInitializer
import com.jwkj.iotvideo.player.api.IIoTCallback
import com.jwkj.lib_gpush.manager.GPushMgr
import com.yoosee.lib_gpush.GPushManager
import com.yoosee.lib_gpush.entity.PushChannel
import com.yoosee.lib_gpush.listener.IGPushNotificationCallback
import com.yoosee.lib_gpush.listener.IGPushResultListener
import com.yoosee.lib_gpush.strategy.SingleStrategy
import kotlinx.coroutines.runBlocking
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/7/20 14:02
 * Description: 推送消息管理类
 */
object GwPushManager {
    private const val TAG = "GwPushManager"

    /**
     * 操作系统类型,2是iOS,3是安卓
     */
    private const val OS_TYPE = 3

    private val notifyServers = mutableListOf<INotifyServer>()

    /**
     * 推送SDK是否初始化成功
     */
    private var isPushInitSuccess = false

    /**
     * Push服务初始化
     *
     * @param context Context 上下文
     */
    fun init(
        context: Context,
        appParamApi: IAppParamApi,
        dataStore: PushDataStore,
        iAccountApi: IAccountApi
    ) {
        val appContext = context.applicationContext
        val userId = runBlocking { iAccountApi.getAsyncUserId() }
        GwellLogUtils.i(TAG, "init-userId:$userId")
        if (userId.isNullOrEmpty()) {
            return
        }

        // 优先注册通知的监听器
        registerNotifyListener()
        val singleStrategy = SingleStrategy(40000, PushChannel.BRAND_FCM)
        GPushManager.init(appContext, singleStrategy, object : IGPushResultListener {
            override fun onFailure(errCode: String, errString: String) {
                GwellLogUtils.e(TAG, "init failure: errCode=$errCode, errString=$errString")
                isPushInitSuccess = false
                register(context, appParamApi, dataStore, iAccountApi)
            }

            override fun onSuccess(channel: String, token: String) {
                GwellLogUtils.i(TAG, "init success: channel=$channel, token=$token")
                isPushInitSuccess = true
                runBlocking { dataStore.saveToken(userId, token) }
                register(context, appParamApi, dataStore, iAccountApi)
            }
        })
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    /**
     * 向服务器注册推送服务
     *
     * @param context Context          上下文
     * @param dataStore PushDataStore  推送数据存储
     * @param iAccountApi IAccountApi  账号api
     * @param appParamApi IAppParamApi 应用参数api
     */
    fun register(
        context: Context,
        appParamApi: IAppParamApi,
        dataStore: PushDataStore,
        iAccountApi: IAccountApi
    ) {
        if (!isPushInitSuccess) {
            GwellLogUtils.i(TAG, "register: isPushInitSuccess false")
            init(context, appParamApi, dataStore, iAccountApi)
            return
        }
        val userId = runBlocking { iAccountApi.getAsyncUserId() }
        GwellLogUtils.i(TAG, "init-userId:$userId")
        if (userId.isNullOrEmpty()) {
            return
        }
        val token = runBlocking { dataStore.getToken(userId) }
        GwellLogUtils.i(TAG, "init-token:$token")

        if (token.isNullOrEmpty()) {
            GwellLogUtils.i(TAG, "init token is null")
            return
        }

        handler.let {
            it.removeCallbacksAndMessages(null)
            it.postDelayed({
                registerPush(userId, token, appParamApi, dataStore, context)
            }, 1000)
        }

    }

    /**
     * 注销 设备推送
     *
     * @param terminalId String 设备ID
     */
    fun unRegisterPush(terminalId: String) {
        IoTAlarmPushManager.instance().unRegisterPush(terminalId, true)
    }

    private fun registerPush(
        userId: String,
        token: String,
        appParamApi: IAppParamApi,
        dataStore: PushDataStore,
        context: Context
    ) {
        val channel = "GOOGLE_FCM"
        // 终端ID
        val termId = IoTVideoInitializer.p2pAlgorithm.getTerminalId().toString()
        // 系统类型
        val osType = OS_TYPE
        // 时区（单位秒）
        val timeZone = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000
        // 语言
        val language = Locale.getDefault().language
        // appId
        val appId = appParamApi.getAppID()
        // 手机唯一Id
        val phoneId = getPhoneUniqueId()
        // 极光推送Id
        val jPushId = ""
        // 系统版本
        val osVer = Build.VERSION.RELEASE
        // sdk版本
        val sdkVer = "1.0"
        // app版本
        val appVer = context.getAppVersionName()
        // 系统推送Id
        val osPushId = ""
        // 制造商推送Id
        val mfrPushId = token
        // 制造商名称
        val mfrName = channel
        // 手机型号
        val mfrDevModel = Build.MODEL
        GwellLogUtils.i(TAG, "registerPush -> $userId, $token, $channel")
        IoTAlarmPushManager.instance().registerIoTPush(
            termId,
            osType,
            timeZone,
            language,
            appId,
            phoneId,
            jPushId,
            osVer,
            sdkVer,
            appVer,
            osPushId,
            mfrPushId,
            mfrName,
            mfrDevModel,
            object : IIoTCallback<String> {
                override fun onStart() {
                    GwellLogUtils.i(TAG, "registerPush onStart...")
                }

                override fun onSuccess(data: String) {
                    super.onSuccess(data)
                    GwellLogUtils.i(TAG, "registerPush onSuccess -> $data")
                    runBlocking { dataStore.saveTokenPushStatus(userId, token, true) }
                    GwellLogUtils.i(TAG, "registerPush onSuccess -> saveToken")
                }

                override fun onError(error: IoTError) {
                    super.onError(error)
                    GwellLogUtils.e(TAG, "registerPush onError IoTError:$error")
                    runBlocking { dataStore.saveTokenPushStatus(userId, token, false) }
                }

                override fun onError(error: ErrorInfo?) {
                    super.onError(error)
                    GwellLogUtils.e(TAG, "registerPush onError ErrorInfo:$error")
                    runBlocking { dataStore.saveTokenPushStatus(userId, token, false) }
                }
            }
        )
    }


    // 硬件信息拼接获取设备唯一Id(无需权限)
// TODO 这个函数 可以 优化 一下, 有 两个方面:
//              1) 只有 第一次 才进行 字串的拼接, 然后 缓存起来. 后继 去 获取 时, 直接 返回 所缓存的值
//              2) 要 考虑 多进程情况, 不同的进程所返回的值 是不是 一样的. 当然, 从 函数的代码来看, 不同的进程 所返回的值 是 一样的.
    private fun getPhoneUniqueId(): String {
        val serial = "serial"
        // 硬件信息拼接
        val deviceIdSplit =
            Build.BOARD + Build.BRAND + Build.DEVICE + Build.MANUFACTURER + Build.PRODUCT + Build.SERIAL
        return UUID(deviceIdSplit.hashCode().toLong(), serial.hashCode().toLong()).toString()
    }

    /**
     * 解析intent中的消息
     *
     * @param intent Intent 意图
     * @return String 推送消息
     */
    fun getIntentExtra(intent: Intent): String {
        val extra = GPushMgr.getInstance().getPushExtrasData(intent)
        GwellLogUtils.i(TAG, "getIntentExtra: GPushMgr=$GPushMgr, intent=$intent, extra=$extra")
        return extra
    }

    /**
     * 注册消息通知的处理监听
     */
    private fun registerNotifyListener() {
        GPushManager.registerNotifyListener(object : IGPushNotificationCallback {

            override fun notifyClick(channel: PushChannel, msg: String) {
                GwellLogUtils.i(TAG, "notifyClick: channel=$channel, msg=$msg")
                for (notifyServer in notifyServers) {
                    notifyServer.onNotifyMsgClick(channel.channel, msg)
                }
            }

            override fun notifyReceiver(channel: PushChannel, msg: String) {
                GwellLogUtils.i(TAG, "notifyReceiver: channel=$channel, msg=$msg")
            }

        })
    }

    /**
     * 添加通知的订阅服务
     *
     * @param server INotifyServer 通知服务
     */
    fun addNotifyServer(server: INotifyServer) {
        if (!notifyServers.contains(server)) {
            notifyServers.add(server)
        }
    }

    /**
     * 清空通知消息的服务
     */
    fun cleanNotifyServer() {
        notifyServers.clear()
    }

}