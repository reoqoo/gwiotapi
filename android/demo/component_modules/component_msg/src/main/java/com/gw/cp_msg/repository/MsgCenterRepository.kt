package com.gw.cp_msg.repository

import android.app.Application
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw.component_plugin_service.api.IPluginManager
import com.gw.component_plugin_service.api.IPluginManager.Result.ON_FAILURE
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw.cp_msg.datasource.RemoteMsgDataSource
import com.gw.cp_msg.datastore.MsgDataStore
import com.gw.cp_msg.entity.SimplePushDataBean
import com.gw.cp_msg.entity.http.MsgDetailEntity
import com.gw.cp_msg.entity.http.MsgListEntity
import com.gw.cp_msg.entity.http.MsgReadEntity
import com.gw_reoqoo.lib_http.entities.VersionInfoEntity
import com.gw_reoqoo.lib_http.RespResult
import com.gw_reoqoo.lib_http.entities.AlarmEvent
import com.gw_reoqoo.lib_http.entities.AppUpgradeEntity
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.str_utils.GwStringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/18 21:39
 * Description: MsgCenterRepository
 */
class MsgCenterRepository @Inject constructor(
    private val app: Application,
    private val remoteMsgDataSource: RemoteMsgDataSource,
    private val dataStore: MsgDataStore,
    private val familyModeApi: FamilyModeApi,
    private val accountApi: IAccountApi,
    private val iPluginManager: IPluginManager
) {

    companion object {
        private const val TAG = "MsgCenterRepository"
    }

    suspend fun getMsgList(): RespResult<MsgListEntity> {
        return remoteMsgDataSource.getMsgList()
    }

    suspend fun getAlarmEventMsg(): MsgDetailEntity? {
        val result = Channel<MsgDetailEntity?>(1)
        remoteMsgDataSource.loadLastAlarmEvent()
            .onSuccess {
                this?.run {
                    if (isNotEmpty()) {
                        val alarmEvent = this[0]
                        val haveReadMsgIdAndDevId = dataStore.getAlarmEventRead()
                        val currentAlarm = buildString {
                            append(alarmEvent.deviceId)
                            append(alarmEvent.alarmId)
                            append(alarmEvent.eventType)
                        }
                        // 当该事件未被标记时设置有已读消息
                        val unReadCount = if (haveReadMsgIdAndDevId == currentAlarm) 0 else 1
                        val entity = MsgDetailEntity(
                            MsgDetailEntity.TAG_MSG_CENTER_ALARM_EVENT,
                            alarmEvent.deviceId,
                            alarmEvent.alarmTime / 1000,
                            false,
                            app.getString(RR.string.AA0465),
                            getEventSummary(alarmEvent),
                            unReadCount,
                            "",
                            alarmEvent.eventType,
                            alarmEvent.alarmId
                        )
                        result.trySend(entity)
                    } else {
                        result.trySend(null)
                    }
                }
            }
            .onServerError { code, msg ->
                GwellLogUtils.e(TAG, "getAlarmEventMsg error, code $code, msg $msg")
                result.trySend(null)
            }
            .onLocalError {
                GwellLogUtils.e(TAG, "getAlarmEventMsg error, throwable ${it.message}")
                result.trySend(null)
            }
        return result.receive()
    }

    suspend fun getAppUpdateMsg(): RespResult<AppUpgradeEntity> {
        return remoteMsgDataSource.getAppUpdate()
    }

    /**
     * 获取设备升级的数据
     *
     * @return List<VersionInfoEntity>?
     */
    suspend fun getUpdateDeviceMsg(): List<VersionInfoEntity>? {
        val version = Channel<List<VersionInfoEntity>>(1)
        withContext(Dispatchers.IO) {
            val versionList = mutableListOf<VersionInfoEntity>()
            accountApi.getAsyncUserId()?.run {
                familyModeApi.getDeviceList(this).mapNotNull { iDevice ->
                    if (iDevice.isMaster && iDevice.isOnline) {
                        val curVersion = getDevVersionForPlugin(iDevice.deviceId)
                        if (curVersion.isNullOrEmpty()) {
                            return@mapNotNull null
                        }
                        remoteMsgDataSource.getDevUpdateMsg(iDevice.deviceId, curVersion)
                            .onSuccess {
                                this?.run {
                                    this.deviceId = iDevice.deviceId
                                    this.devName = iDevice.remarkName
                                    versionList.add(this)
                                } ?: GwellLogUtils.e(TAG, "version is null")
                            }
                            .onServerError { code, msg ->
                                GwellLogUtils.e(TAG, "onServerError: code $code, msg $msg")
                            }
                            .onLocalError {
                                GwellLogUtils.e(TAG, "onLocalError: throwable $it")
                            }
                    } else {
                        null
                    }
                }
                version.trySend(versionList)
            }
        }
        return version.receive()
    }

    suspend fun readMsg(tag: String?, deviceId: Long?): RespResult<MsgReadEntity> {
        return remoteMsgDataSource.readSystemMsg(tag, deviceId)
    }

    fun getDevInfoById(deviceId: String): IDevice? {
        return familyModeApi.deviceInfo(deviceId)
    }

    /**
     * 初始化报警事件消息类型
     *  // TODO 消息中心暂不显示报警信息
     *
     * @param alarmEvent 报警事件
     * @return String   消息类型文案
     */
    private fun getEventSummary(alarmEvent: AlarmEvent): String {
        return when (alarmEvent.eventType) {
            SimplePushDataBean.PushType.DEVICE_OFF_LINE -> {
                // 设备离线
                GwStringUtils.formatStr(
                    app.getString(RR.string.AA0466),
                    alarmEvent.deviceId
                )
            }

            SimplePushDataBean.PushType.LOW_POWER -> {
                // 暂无定义
                ""
            }

            SimplePushDataBean.PushType.CHANGE_POWER_SAVING -> {
                app.getString(RR.string.AA0467)
            }

            SimplePushDataBean.PushType.CHANGE_NOT_SLEEP -> {
                app.getString(RR.string.AA0468)
            }

            else -> {
                // TODO 需要从物模型获取报警类型，放到二期来处理
                ""
//                val alarmType = if (IoTDeviceUtils.isIoTDevice(alarmEvent.deviceId.toString())) {
//                    GwCompoMediator.g().getCompoApi(IotAlarmUtilsApi::class.java)
//                        ?.getAlarmType(alarmEvent.eventType.toInt())
//                } else {
//                    GwCompoMediator.g().getCompoApi(IDevVasAndCloudApi::class.java)
//                        ?.getGDevEventType(alarmEvent.eventType.toInt())
//                }
//                when (alarmType) {
//                    P2PValue.AlarmType.ALARM_TYPE_ONE_TOUCH_CALL -> {
//                        GwStringUtils.formatStr(
//                            AppEnv.APP.resources.getString(R.string.AA2521),
//                            device?.contactName ?: alarmEvent.deviceId
//                        )
//                    }
//
//                    P2PValue.AlarmType.ALARM_TYPE_HUMANOID_DETECTION -> {
//                        GwStringUtils.formatStr(
//                            AppEnv.APP.resources.getString(R.string.AA2518),
//                            device?.contactName ?: alarmEvent.deviceId
//                        )
//                    }
//
//                    P2PValue.AlarmType.ALARM_PET -> {
//                        GwStringUtils.formatStr(
//                            AppEnv.APP.resources.getString(R.string.AA2520),
//                            device?.contactName ?: alarmEvent.deviceId
//                        )
//                    }
//
//                    P2PValue.AlarmType.ALARM_CAR -> {
//                        GwStringUtils.formatStr(
//                            AppEnv.APP.resources.getString(R.string.AA2519),
//                            device?.contactName ?: alarmEvent.deviceId
//                        )
//                    }
//
//                    P2PValue.AlarmType.ALARM_TYPE_SMOKE_ALARM -> {
//                        GwStringUtils.formatStr(
//                            AppEnv.APP.resources.getString(R.string.AA2670),
//                            device?.contactName ?: alarmEvent.deviceId
//                        )
//                    }
//
//                    else -> {
//                        GwStringUtils.formatStr(
//                            AppEnv.APP.resources.getString(R.string.AA2517),
//                            device?.contactName ?: alarmEvent.deviceId
//                        )
//                    }
//                }
            }
        }
    }

    /**
     * 通过插件的接口查询当前设备的版本号
     */
    private suspend fun getDevVersionForPlugin(devID: String): String? {
        val version = iPluginManager.queryDevVersion(devID)
        if (version.isEmpty() || version.keys.first() == ON_FAILURE) {
            return null
        }
        return version.values.first()
    }

}