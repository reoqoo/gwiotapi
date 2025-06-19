package com.gw.cp_msg.ui.fragment.system_msg.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw.component_webview.api.interfaces.IWebViewApi
import com.gw.cp_mine.api.kapi.IMineModuleApi
import com.gw.cp_msg.api.interfaces.ILocalMsgApi
import com.gw.cp_msg.datastore.IMsgDataStoreApi
import com.gw.cp_msg.entity.ParamConstant
import com.gw.cp_msg.entity.http.MsgDetailEntity
import com.gw.cp_msg.repository.MsgCenterRepository
import com.gw.cp_msg.utils.PushUtils
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.protocol.IGwBaseVm
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.toJson
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.version.Version
import com.gwell.loglibs.GwellLogUtils
import com.therouter.TheRouter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/18 11:16
 * Description: SystemMsgVM
 */
@HiltViewModel
class SystemMsgVM @Inject constructor(
    private val app: Application,
    private val msgApi: ILocalMsgApi,
    private val webViewApi: IWebViewApi,
    private val repository: MsgCenterRepository,
    private val msgDataStore: IMsgDataStoreApi,
    private val mineApi: IMineModuleApi
) : ABaseVM() {

    companion object {
        private const val TAG = "SystemMsgVM"

        /**
         * 已读的消息
         */
        const val KEY_READ_MSG = "key_read_msg"

        /**
         * 已读的消息数量
         */
        const val KEY_READ_MSG_COUNT = "key_read_msg_count"

        /**
         * 已读消息的index
         */
        const val KEY_READ_MSG_POSITION = "key_read_msg_position"
    }

    /**
     * 推送通知状态
     */
    val notificationStatus = MutableLiveData(false)

    /**
     * 获取系统消息
     */
    val msgListEvent: MutableLiveData<MutableList<MsgDetailEntity>?> = MutableLiveData()

    /**
     * 已读消息
     */
    val readMsgEvent: MutableLiveData<Map<String, Any?>> = MutableLiveData()

    /**
     * 更新指定item的数据
     */
    val itemUpdateFromPos: MutableLiveData<Int> = MutableLiveData<Int>()

    /**
     * 是否开启推送通知
     *
     * @return Boolean true：开启， false：未开启
     */
    fun isNotificationEnabled() {
        notificationStatus.postValue(PushUtils.isNotificationEnabled(app))
    }

    /**
     * 拉取系统消息列表
     */
    fun loadSystemMsgList() {
        loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
        msgApi.initMsgList(onResult = {
            loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
            msgListEvent.postValue(it?.toMutableList())
        })
    }

    /**
     * 清除所有未读消息
     */
    fun cleanUnreadMsg() {
        msgListEvent.value?.mapIndexed { index, msgDetailEntity ->
            if (msgDetailEntity.unreadCnt > 0) {
                msgDetailEntity.unreadCnt = 0
            }
            GwellLogUtils.i(TAG, "map $msgDetailEntity")
            when (msgDetailEntity.tag) {
                MsgDetailEntity.TAG_MSG_CENTER_APP_UPGRADE -> {
                    val curVersion = Version.from(msgDataStore.getAppUpgradeRead() ?: "")
                    val tagVersion = Version.from(msgDetailEntity.appVersion ?: "")
                    if (tagVersion > curVersion) {
                        msgDataStore.setAppUpgradeRead(msgDetailEntity.appVersion ?: "")
                    }
                    itemUpdateFromPos.postValue(index)
                }

                MsgDetailEntity.TAG_MSG_CENTER_FIRMWARE_UPDATE -> {
                    msgDataStore.setDevUpgradeRead(
                        msgDetailEntity.deviceId.toString(),
                        msgDetailEntity.appVersion ?: ""
                    )
                    itemUpdateFromPos.postValue(index)
                }

                else -> {
                    readSystemMsg()
                }
            }
        }
        msgListEvent.postValue(msgListEvent.value)
    }

    /**
     * 将 系统消息 置为已读
     *
     * @param tag   消息tag
     */
    private fun readSystemMsg() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.readMsg(null, null)
                .onSuccess {
                    GwellLogUtils.i(TAG, "onSuccess: readSystemMsg $this")
                }
                .onServerError { code, msg ->
                    GwellLogUtils.e(TAG, "onServerError: code $code, msg $msg")
                }
                .onLocalError {
                    GwellLogUtils.e(TAG, "onLocalError: it $it")
                }

        }
    }

    /**
     * 将 服务器 的某类消息置为已读（本地消息需要手动处理）
     *
     * @param systemMsg 消息tag
     * @param position 列表的下标
     */
    fun readSystemMsg(systemMsg: MsgDetailEntity, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.readMsg(systemMsg.tag, systemMsg.deviceId)
                .onSuccess {
                    val entity = this
                    val map = HashMap<String, Any?>().apply {
                        put(KEY_READ_MSG, systemMsg)
                        put(KEY_READ_MSG_COUNT, entity?.count)
                        put(KEY_READ_MSG_POSITION, position)
                    }
                    readMsgEvent.postValue(map)
                }
                .onServerError { code, msg ->

                }
                .onLocalError {

                }
        }
    }

    /**
     * app升级消息的点击处理
     *
     * @param msg MsgDetailEntity
     */
    fun itemAppUpgradeClick(msg: MsgDetailEntity) {
        msgDataStore.setAppUpgradeRead(msg.appVersion ?: "")
        mineApi.appVersionUpgrade(app)
    }

    /**
     * 设备升级消息的点击处理
     *
     * @param msg MsgDetailEntity 消息
     */
    fun itemDevUpgradeClick(msg: MsgDetailEntity) {
        msgDataStore.setDevUpgradeRead(msg.deviceId.toString(), msg.appVersion ?: "")
        // 固件升级
        val upgradeList = msgApi.getDevUpgradeList()
        GwellLogUtils.i(TAG, "upgradeList $upgradeList")
        if (upgradeList.size > 1) {
            // 进入二级页面
            pageJumpData.postValue(
                PageJumpData(
                    TheRouter.build(
                        ReoqooRouterPath.MsgCenterPath.ACTIVITY_MSG_DEV_UPGRADE
                    ).withSerializable(ParamConstant.KEY_CURRENT_MSG_LIST, upgradeList.toJson())
                )
            )
        } else {
            // 直接进入设备升级页
            pageJumpData.postValue(PageJumpData(TheRouter.build(ReoqooRouterPath.Family.FAMILY_ACTIVITY_DEVICE_UPDATE)))
        }
    }


    /**
     * 系统消息的点击处理
     *
     * @param msg MsgDetailEntity
     */
    fun itemSystemMsgClick(msg: MsgDetailEntity) {
        if (msg.isHeap) {
            GwellLogUtils.i(TAG, "goto Second page")
            pageJumpData.postValue(
                PageJumpData(
                    TheRouter.build(
                        ReoqooRouterPath.MsgCenterPath.ACTIVITY_MSG_INFO
                    ).withSerializable(ParamConstant.KEY_CURRENT_MSG, msg)
                )
            )
        } else {
            // 其他消息，有配置url的情况下直接跳转至url
            GwellLogUtils.i(TAG, "msg tag: ${msg.tag}, url ${msg.redirectUrl}")
            if (msg.redirectUrl.isNotEmpty()) {
                webViewApi.openWebView(msg.redirectUrl, "")
            }
        }
    }

}