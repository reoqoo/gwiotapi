package com.gw.component_push.api.impl

import android.content.Context
import android.content.Intent
import com.gw.component_push.api.interfaces.INotifyServer
import com.gw.component_push.api.interfaces.IPushApi
import com.gw.component_push.datastore.PushDataStore
import com.gw.component_push.entity.AlarmPushEntity
import com.gw.component_push.entity.OfflinePushMsgEntity
import com.gw.component_push.manager.GwPushManager
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw.cp_config.api.IAppParamApi
import com.gw_reoqoo.lib_http.jsonToEntity
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/7/23 22:40
 * Description: PushApiImpl
 */
@Singleton
class PushApiImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appParamApi: IAppParamApi,
    private val dataStore: PushDataStore,
    private val iAccountApi: IAccountApi,
    private val eventApiImpl: AlarmEventApiImpl,
) : IPushApi {

    companion object {
        private const val TAG = "PushApiImpl"

        /**
         * 推送类型
         */
        private const val KEY_PUSH_TYPE = "push_type"

        /**
         * 推送数据
         */
        private const val KEY_PUSH_CONTENT = "content"

        /**
         * 推送数据
         */
        private const val KEY_PUSH_DATA = "push_data"

        /**
         * 报警离线推送
         */
        const val PUSH_TYPE_ALARM = "DevAlmTrg"

        /**
         * 报警离线推送(改)
         */
        const val PUSH_TYPE_SIMPLE_PUSH = "SimplePush"

        /**
         * 消息中心离线推送
         */
        const val PUSH_TYPE_MSG_CENTER = "MsgCenter"
    }

    override fun initPushServer() {
        GwPushManager.init(context, appParamApi, dataStore, iAccountApi)
    }

    /**
     * 注册推送服务
     */
    override fun registerPushServer() {
        GwPushManager.register(context, appParamApi, dataStore, iAccountApi)
    }

    /**
     * 注销推送服务
     */
    override fun unRegisterPushServer(terminalId: String) {
        GwPushManager.unRegisterPush(terminalId)
    }

    override fun getPushFromIntent(intent: Intent): String? {
        return GwPushManager.getIntentExtra(intent)
    }

    /**
     * 解析intent中的推送信息
     *
     * @param intent Intent 意图
     * @return Boolean 是否包含推送消息
     */
    override fun parsePushFromIntent(intent: Intent): Boolean {
        val pushInfo = getPushFromIntent(intent)
        GwellLogUtils.i(TAG, "parsePushFromIntent: pushInfo=$pushInfo")
        if (pushInfo.isNullOrEmpty()) {
            return false
        }
        handlingPush(pushInfo)
        return true
    }

    /**
     * 处理推送信息
     *
     * @param pushInfo String
     */
    private fun handlingPush(pushInfo: String) {
        val pushJson = JSONObject(pushInfo)
        if (pushJson.has(KEY_PUSH_TYPE)) {
            val pusType = pushJson.optString(KEY_PUSH_TYPE).replace("\"", "")
            GwellLogUtils.i(TAG, "parsePushFromIntent: push_type=$pusType")
            when (pusType) {
                PUSH_TYPE_ALARM -> {
                    GwellLogUtils.i(TAG, "PUSH_TYPE_ALARM: alarm push")
                    // 告警消息
                    val alarmMsg = try {
                        pushJson.optString(KEY_PUSH_DATA)
                    } catch (e: Exception) {
                        GwellLogUtils.e(TAG, "parsePushFromIntent: can not parse push_data", e)
                        null
                    }
                    if (alarmMsg.isNullOrEmpty()) {
                        GwellLogUtils.e(TAG, "parsePushFromIntent: alarmMsg = null")
                        return
                    }
                    GwellLogUtils.i(TAG, "parsePushFromIntent: alarmMsg=$alarmMsg")
                    alarmMsg.jsonToEntity<AlarmPushEntity>()?.let {
                        GwellLogUtils.i(TAG, "parsePushFromIntent: devAlmPushDataBean=$it")
                        if (it.deviceId == 0L) {
                            GwellLogUtils.e(TAG, "parsePushFromIntent: deviceId is null")
                            return
                        }
                        // 处理告警消息
                        eventApiImpl.analyticAlarmPushEvent(it)
                    } ?: let {
                        GwellLogUtils.e(TAG, "parsePushFromIntent: devAlmPushDataBean is null")
                    }
                }

                PUSH_TYPE_SIMPLE_PUSH -> {
                    GwellLogUtils.i(TAG, "PUSH_TYPE_SIMPLE_PUSH: alarm push")
                    // 告警消息
                    val alarmMsg = try {
                        pushJson.optString(KEY_PUSH_DATA)
                    } catch (e: Exception) {
                        GwellLogUtils.e(TAG, "PUSH_TYPE_SIMPLE_PUSH: can not parse push_data", e)
                        null
                    }
                    if (alarmMsg.isNullOrEmpty()) {
                        GwellLogUtils.e(TAG, "PUSH_TYPE_SIMPLE_PUSH: alarmMsg = null")
                        return
                    }
                    GwellLogUtils.i(TAG, "PUSH_TYPE_SIMPLE_PUSH: alarmMsg=$alarmMsg")
                    // TODO 因为后台p2p消息中的type和离线推送的Type 大小写不一样，所以先这样处理后续看如何处理
                    alarmMsg.jsonToEntity<OfflinePushMsgEntity>()?.let {
                        GwellLogUtils.i(TAG, "PUSH_TYPE_SIMPLE_PUSH: devAlmPushDataBean=$it")
                        if (it.deviceId.isEmpty()) {
                            GwellLogUtils.e(TAG, "PUSH_TYPE_SIMPLE_PUSH: deviceId is null")
                            return
                        }
                        // 处理告警消息
                        eventApiImpl.analyticAlarmPushEvent(
                            AlarmPushEntity(
                                deviceId = it.deviceId.toLong(),
                                evtId = it.pushContent.alarmId,
                                trgTime = it.pushTime,
                                trgType = it.pushContent.alarmType
                            )
                        )
                    } ?: let {
                        GwellLogUtils.e(TAG, "parsePushFromIntent: devAlmPushDataBean is null")
                    }
                }

                PUSH_TYPE_MSG_CENTER -> {
                    // 消息中心
                    GwellLogUtils.i(TAG, "parsePushFromIntent: msg center push")
                    val pushContent = pushJson.optString(KEY_PUSH_CONTENT)
                }


                else -> {
                    GwellLogUtils.e(TAG, "parsePushFromIntent：unknown push_type=$pusType")
                }
            }

        }
    }

    override fun addNotificationServer(server: INotifyServer) {
        GwPushManager.addNotifyServer(server)
    }
}