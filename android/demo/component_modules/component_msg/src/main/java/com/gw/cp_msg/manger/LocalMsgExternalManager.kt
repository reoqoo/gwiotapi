package com.gw.cp_msg.manger

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.gw.cp_msg.api.interfaces.ILocalMsgApi
import com.gw.cp_msg.api.kapi.IMsgExternalApi
import com.gw.cp_msg.datastore.IMsgDataStoreApi
import com.gw.cp_msg.entity.http.MsgDetailEntity
import com.gw.cp_msg.entity.http.MsgInfoListEntity
import com.gw_reoqoo.lib_http.entities.VersionInfoEntity
import com.gw.cp_msg.repository.MsgCenterRepository
import com.gw_reoqoo.lib_http.entities.AppUpgradeEntity
import com.gw_reoqoo.lib_utils.version.Version
import com.gw_reoqoo.resource.R
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.str_utils.GwStringUtils
import com.therouter.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/11/2 11:16
 * Description: MsgManager
 */
@Singleton
class LocalMsgExternalManager @Inject constructor(
    private val app: Application,
    private val repository: MsgCenterRepository,
    private val msgDataStore: IMsgDataStoreApi,
    private val msgCenterRepository: MsgCenterRepository
) : ILocalMsgApi, IMsgExternalApi {

    companion object {
        private const val TAG = "MsgManager"
    }

    private val scope = MainScope()

    /**
     * 所有的系统消息
     */
    private val systemMsgList = mutableListOf<MsgDetailEntity>()

    /**
     * 固件升级的列表信息
     */
    private var upgradeList = mutableListOf<MsgDetailEntity>()

    /**
     * 结果回调监听
     */
    private var mOnResult: ((List<MsgDetailEntity>?) -> Unit)? = null

    /**
     * 小红点的监听
     */
    private var hasNewMsgLiveData = MutableLiveData<Boolean>()

    override fun initMsgList(onResult: (List<MsgDetailEntity>?) -> Unit) {
        mOnResult = onResult
        initAllMsg()
    }

    /**
     * 获取设备升级的列表
     *
     * @return List<MsgDetailEntity>
     */
    override fun getDevUpgradeList(): List<MsgDetailEntity> {
        return upgradeList
    }

    /**
     * 获取所有未读消息
     *
     * @return 未读消息数量
     */
    override fun getUnreadMsgCount(unReadMsgCount: (Int) -> Unit) {
        initMsgList(onResult = {
            var unReadCount = 0
            for (msg in systemMsgList) {
                msg.let {
                    GwellLogUtils.i(TAG, "msg.unread ${msg.unreadCnt}, total $unReadCount")
                    unReadCount += it.unreadCnt
                }
            }
            unReadMsgCount.invoke(unReadCount)
        })
    }

    /**
     * 初始化所有消息
     */
    private fun initAllMsg() {
        systemMsgList.clear()
        scope.launch(Dispatchers.IO) {
            initAppVersionMsg()
            initDevUpgradeMsg()
            initServerMsg()
            GwellLogUtils.i(TAG, "initAllMsg $systemMsgList")
            mOnResult?.invoke(systemMsgList)
        }
    }

    /**
     * 初始化app版本升级消息
     */
    private suspend fun initAppVersionMsg() {
        repository.getAppUpdateMsg()
            .onSuccess {
                this?.let {
                    val appUpdateMsg = initAppUpgrade(it)
                    GwellLogUtils.i(TAG, "appUpdateMsg: $appUpdateMsg")
                    systemMsgList.add(appUpdateMsg)
                }
            }
            .onServerError { code, msg ->
                GwellLogUtils.e(TAG, "getAppUpdateMsg error: code $code, msg $msg")
            }
            .onLocalError {
                GwellLogUtils.e(TAG, "getAppUpdateMsg onLocalError: it ${it.message}")
            }
    }

    /**
     * 初始化固件升级的消息
     */
    private suspend fun initDevUpgradeMsg() {
        // 初始化固件升级消息
        msgCenterRepository.getUpdateDeviceMsg()?.let {
            GwellLogUtils.i(TAG, "getUpdateDeviceMsg $it")
            upgradeList.clear()
            val list = initDevUpgrade(it)
            GwellLogUtils.i(TAG, "initDevUpgrade $list")
            upgradeList.addAll(list)
            if (list.isNotEmpty()) {
                systemMsgList.add(list.first())
            }
        } ?: GwellLogUtils.i(TAG, "getUpdateDeviceMsg is null")
    }

    /**
     * 初始化服务器消息
     */
    private suspend fun initServerMsg() {
        // 获取消息列表数据
        repository.getMsgList()
            .onSuccess {
                GwellLogUtils.i(TAG, "MsgListEntity: $this")
                this?.list?.run {
                    initCustomerMsg(this)
                }
                GwellLogUtils.i(TAG, "systemMsgList: $systemMsgList")
            }
            .onServerError { code, msg ->
                GwellLogUtils.e(TAG, "onServerError: code $code, msg $msg")
            }
            .onLocalError {
                GwellLogUtils.e(TAG, "onLocalError: error ${it.message}")
            }
    }

    /**
     * app 升级消息
     *
     * @param entity AppUpgradeEntity? app升级信息
     * @return MsgDetailEntity? 消息
     */
    private fun initAppUpgrade(entity: AppUpgradeEntity): MsgDetailEntity {
        return entity.run {
            val curVersion = Version.from(msgDataStore.getAppUpgradeRead() ?: "")
            val tagVersion = Version.from(versionNo)
            val unReadCount = if (tagVersion > curVersion) {
                1
            } else {
                0
            }
            MsgDetailEntity(
                MsgDetailEntity.TAG_MSG_CENTER_APP_UPGRADE,
                0, System.currentTimeMillis() / 1000, false,
                app.getString(R.string.AA0495),
                GwStringUtils.formatStr(
                    app.getString(R.string.AA0496),
                    versionNo
                ),
                unReadCount,
                "",
                0,
                "",
                versionNo
            )
        }
    }

    /**
     * 将固件升级信息 转为 消息
     *
     * @param entityList List<VersionInfoEntity>
     * @return List<MsgDetailEntity>
     */
    private fun initDevUpgrade(entityList: List<VersionInfoEntity>): List<MsgDetailEntity> {
        return entityList.mapNotNull {
            val deviceId = it.deviceId
            if (deviceId.isNullOrEmpty() || it.version.isEmpty()) {
                null
            } else {
                val haveReadUpdate = msgDataStore.getDevUpgradeRead(deviceId)
                val currentUpdate = buildString {
                    append(deviceId)
                    append(it.version)
                }
                val unReadCount = if (haveReadUpdate == currentUpdate) 0 else 1
                MsgDetailEntity(
                    MsgDetailEntity.TAG_MSG_CENTER_FIRMWARE_UPDATE,
                    deviceId.toLong(),
                    System.currentTimeMillis() / 1000,
                    true,
                    app.getString(R.string.AA0219),
                    GwStringUtils.formatStr(
                        app.getString(R.string.AA0497),
                        it.devName,
                        it.version
                    ),
                    unReadCount,
                    "",
                    0,
                    "",
                    it.version
                )
            }
        }
    }

    /**
     * 处理客服消息
     *
     * @param msgList List<Msg?>
     */
    private fun initCustomerMsg(msgList: List<MsgDetailEntity>) {
        val customerMsg = ArrayList<MsgDetailEntity>()
        for (msg in msgList) {
            // 区分客服消息和其他消息（客服消息需要App处理是否有详情页）
            GwellLogUtils.i(TAG, "msg $msg")
            if (MsgDetailEntity.TAG_MSG_CENTER_CUSTOMER_SRV == msg.tag) {
                customerMsg.add(msg)
            } else {
                systemMsgList.add(msg)
            }
        }
        // 如果有两条以上的客服消息
        if (customerMsg.size > 1) {
            val msg = customerMsg[0]
            msg.isHeap = true
            msg.customerMsg = ArrayList()
            msg.customerMsg?.addAll(initCustomerMsgInfo(customerMsg))
            systemMsgList.add(msg)
        } else {
            systemMsgList.addAll(customerMsg)
        }
        GwellLogUtils.i(TAG, "customerMsg: $customerMsg")
    }

    /**
     * 初始化客服详情
     * @param customerMsg   客服消息
     * @return 客服详情消息
     */
    private fun initCustomerMsgInfo(customerMsg: ArrayList<MsgDetailEntity>): ArrayList<MsgInfoListEntity.MSGInfo> {
        val customerMsgInfo = ArrayList<MsgInfoListEntity.MSGInfo>()
        for (msg in customerMsg) {
            msg.run {
                val msgInfo = MsgInfoListEntity.MSGInfo(
                    0L,
                    tag,
                    deviceId,
                    12,
                    title,
                    summary,
                    msgTime,
                    redirectUrl
                )
                customerMsgInfo.add(msgInfo)
            }
        }
        return customerMsgInfo
    }

}