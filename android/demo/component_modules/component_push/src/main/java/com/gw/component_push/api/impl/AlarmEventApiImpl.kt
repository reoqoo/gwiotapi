package com.gw.component_push.api.impl

import com.gw.component_plugin_service.api.IPluginManager
import com.gw.component_push.api.interfaces.AlarmEventType
import com.gw.component_push.api.interfaces.IAlarmEventApi
import com.gw.component_push.entity.AlarmEventData
import com.gw.component_push.entity.AlarmEventEntity
import com.gw.component_push.entity.AlarmPushEntity
import com.gw.component_push.entity.PushMsgContentEntity
import com.gw.component_push.entity.PushMsgType
import com.gw_reoqoo.component_family.api.interfaces.ILocalDeviceApi
import com.gw_reoqoo.lib_iotvideo.EventMessage
import com.gw_reoqoo.lib_iotvideo.EventTopicType
import com.gw_reoqoo.lib_utils.ktx.bitAt
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.utils.JSONUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/12/18 15:35
 * Description: 告警事件的实现类
 */
@Singleton
class AlarmEventApiImpl @Inject constructor(
    private val devCallApiImpl: DevCallApiImpl,
    private val pluginManager: IPluginManager,
    private val localDeviceApiImpl: ILocalDeviceApi
) : IAlarmEventApi {

    companion object {
        private const val TAG = "AlarmEventApiImpl"

        /**
         * 呼叫事件超时时间(ms)
         */
        private const val CALL_EVENT_OVER_TIME = 32 * 1000

    }

    private val scope = MainScope()

    /**
     * 解析告警事件
     *
     * @param eventMessage EventMessage
     */
    override fun analyticAlarmEvent(eventMessage: EventMessage) {
        GwellLogUtils.i(TAG, "eventMessage $eventMessage")
        if (eventMessage.topic == EventTopicType.NOTIFY_PUSH) {
            // 推送消息过来的事件
            val pushMsgContentEntity = JSONUtils.JsonToEntity(
                eventMessage.event, PushMsgContentEntity::class.java
            )
            if (pushMsgContentEntity?.data != null) {
                val pushMsgEntity = pushMsgContentEntity.data
                try {
                    val pushType = pushMsgEntity.pushType
                    if (pushType == 1L shl AlarmEventType.ALARM_VIDEO_BIT) {
                        GwellLogUtils.i(TAG, "video bit alarm,need return")
                        return
                    }
                    scope.launch(Dispatchers.Main) {
                        GwellLogUtils.i(TAG, "pushMsgEntity: $pushMsgEntity")
                        // 设备关机
                        if (pushType.bitAt(AlarmEventType.ALARM_DEVICE_SHUTDOWN) == 1L) {
                            GwellLogUtils.i(TAG, "pushType == ALARM_DEVICE_SHUTDOWN")
                            localDeviceApiImpl.updateDevicePowerOn(
                                pushMsgEntity.deviceId,
                                false
                            )
                        // 设备开机
                        } else if (pushType.bitAt(AlarmEventType.ALARM_DEVICE_POWER_ON) == 1L) {
                            GwellLogUtils.i(TAG, "pushType == ALARM_DEVICE_POWER_ON")
                            localDeviceApiImpl.updateDevicePowerOn(
                                pushMsgEntity.deviceId,
                                true
                            )
                        // 通用推送类型
                        } else if (pushType.bitAt(AlarmEventType.ALARM_COMMON_PUSH_TYPE) == 1L) {
                            if (pushMsgEntity.pushContent.type == PushMsgType.EVENT.type) {
                                val isTimeout =
                                    pushMsgEntity.pushTime < System.currentTimeMillis() - CALL_EVENT_OVER_TIME
                                GwellLogUtils.i(
                                    TAG,
                                    "pushTime: ${pushMsgEntity.pushTime}, currentTime: ${System.currentTimeMillis()}"
                                )
                                val isCallEvent =
                                    pushMsgEntity.pushContent.alarmType.bitAt(AlarmEventType.ALARM_PRESS_CALL)
                                GwellLogUtils.i(TAG, "isCallEvent: $isCallEvent, isTimeout: $isTimeout")
                                if (isCallEvent == 1 && !isTimeout) {
                                    // 一键呼叫
                                    GwellLogUtils.i(TAG, "receive call event")
                                    devCallApiImpl.receiveCallEvent(pushMsgContentEntity)
                                } else {
                                    getAlarmEventDetail(
                                        isP2pMsg = true,
                                        tid = pushMsgEntity.deviceId,
                                        alarmId = pushMsgEntity.pushContent.alarmId,
                                        alarmType = pushMsgEntity.pushContent.alarmType
                                    )
                                }
                            }
                        }
                    }
                } catch (exception: Exception) {
                    GwellLogUtils.e(TAG, "analyticAlarmEvent error:" + exception.message)
                }
            }
        } else if (eventMessage.topic == EventTopicType.NOTIFY_ALARM_CHANGE_V2) {
            // 报警推送
            val alarmEventEntity = JSONUtils.JsonToEntity(
                eventMessage.event, AlarmEventEntity::class.java
            )
            if (alarmEventEntity?.data != null) {
                val alarmEventData = alarmEventEntity.data
                try {
                    val alarmType: Int = alarmEventData.alarmType.toInt()
                    if (alarmType == 1 shl AlarmEventType.ALARM_VIDEO_BIT) {
                        GwellLogUtils.i(TAG, "video bit alarm,need return")
                        return
                    }
                    scope.launch(Dispatchers.Main) {
                        GwellLogUtils.i(TAG, "alarmEventData: $alarmEventData")
                        val isCallEvent = alarmType.bitAt(AlarmEventType.ALARM_PRESS_CALL) == 1
                        // 由于推送消息的时间有可能大于手机时间，所以决定大于当前手机时间的话，也按照未超时处理
                        val isTimeout =
                            alarmEventData.triTime < System.currentTimeMillis() - CALL_EVENT_OVER_TIME
                        if (isCallEvent && !isTimeout) {
                            GwellLogUtils.i(TAG, "receive call event")
                            devCallApiImpl.receiveCallEvent(alarmEventEntity)
                        } else {
                            getAlarmEventDetail(
                                isP2pMsg = true,
                                tid = alarmEventData.deviceId,
                                alarmId = alarmEventData.alarmId,
                                alarmType = alarmType
                            )
                        }
                    }

                } catch (exception: Exception) {
                    GwellLogUtils.e(TAG, "analyticAlarmEvent error:" + exception.message)
                }
            }
        } else {
            GwellLogUtils.e(
                TAG,
                "eventMessage error: topic-${eventMessage.topic}, event-${eventMessage.event}"
            )
        }
    }

    /**
     * 处理告警推送事件
     *
     * @param alarmPushEntity AlarmPushEntity
     */
    fun analyticAlarmPushEvent(alarmPushEntity: AlarmPushEntity) {
        GwellLogUtils.i(TAG, "analyticAlarmPushEvent: alarmPushEntity=$alarmPushEntity")
        if (alarmPushEntity.deviceId != 0L && alarmPushEntity.evtId.isNotEmpty()) {
            val isTimeout =
                alarmPushEntity.trgTime < System.currentTimeMillis() - CALL_EVENT_OVER_TIME
            scope.launch(Dispatchers.IO) {
                delay(1000)
                withContext(Dispatchers.Main) {
                    val isCallEvent =
                        alarmPushEntity.trgType.bitAt(AlarmEventType.ALARM_PRESS_CALL) == 1
                    if (isCallEvent && !isTimeout) {
                        GwellLogUtils.i(TAG, "receive call event")
                        val alarmEventData = AlarmEventData(
                            deviceId = alarmPushEntity.deviceId.toString(),
                            alarmId = alarmPushEntity.evtId,
                            alarmType = alarmPushEntity.trgType.toString(),
                            triTime = alarmPushEntity.trgTime,
                            picPath = null,
                            bucketName = null,
                            pkgType = null,
                            endTime = null
                        )
                        devCallApiImpl.receiveCallEvent(
                            AlarmEventEntity(
                                uuid = null,
                                origin = null,
                                topic = null,
                                t = null,
                                data = alarmEventData
                            )
                        )
                    } else {
                        getAlarmEventDetail(
                            isP2pMsg = false,
                            tid = alarmPushEntity.deviceId.toString(),
                            alarmId = alarmPushEntity.evtId,
                            alarmType = alarmPushEntity.trgType
                        )
                    }
                }
            }
        }
    }

    /**
     * 获取告警事件详情
     *
     * @param tid         告警事件id
     * @param alarmId     告警事件id
     * @param alarmType   告警事件类型
     */
    private fun getAlarmEventDetail(
        isP2pMsg: Boolean? = true, tid: String, alarmId: String, alarmType: Int
    ) {
        val isCallEvent = alarmType.bitAt(AlarmEventType.ALARM_PRESS_CALL) == 1
        // 一键呼叫在线p2p告警消息超时，不做处理
        if (isP2pMsg == true) {
            // TODO p2p在线消息暂不处理（除设备呼叫以外）的其他告警事件
            GwellLogUtils.i(TAG, "tid: $tid, alarmId: $alarmId, alarmType: $alarmType")
            return
        }
        pluginManager.startPlaybackActivity(
            deviceId = tid,
            alarmId = alarmId,
        )
    }

}