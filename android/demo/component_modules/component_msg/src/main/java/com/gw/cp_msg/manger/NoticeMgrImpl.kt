package com.gw.cp_msg.manger

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import androidx.annotation.MainThread
import com.gw.component_webview.api.interfaces.IWebViewApi
import com.gw.cp_config_net.api.interfaces.IActivityNameApi
import com.gw.cp_msg.api.kapi.INoticeMgrApi
import com.gw.cp_msg.entity.http.BannerEntity
import com.gw.cp_msg.entity.http.BannerTag
import com.gw.cp_msg.entity.http.H5DoMainEntity
import com.gw.cp_msg.entity.http.MainNoticeEntity
import com.gw.cp_msg.entity.http.MsgState
import com.gw.cp_msg.entity.http.MsgType
import com.gw.cp_msg.entity.http.NoticeDataEntity
import com.gw.cp_msg.entity.http.NoticeEntity
import com.gw.cp_msg.entity.http.ShowOptType
import com.gw.cp_msg.entity.http.ShowWayType
import com.gw.cp_msg.entity.http.TagType
import com.gw.cp_msg.entity.http.UserMessageListBean
import com.gw.cp_msg.repository.NoticeRepository
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_lifecycle.activity_lifecycle.ActivityLifecycleManager
import com.jwkj.base_utils.handler.WeakHandler
import com.jwkj.base_utils.time.GwTimeUtils
import com.jwkj.base_utils.ui.DensityUtil
import com.gw_reoqoo.widget_webview.jsinterface.WebViewJSCallbackImpl
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.tencentcs.iotvideo.utils.JSONUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/12/12 15:42
 * Description: 公告管理类
 */
@Singleton
class NoticeMgrImpl @Inject constructor(
    private val webViewApi: IWebViewApi,
    private val repository: NoticeRepository,
    private val activityNameApi: IActivityNameApi
) : INoticeMgrApi, WeakHandler.IHandler {

    companion object {
        private const val TAG = "NoticeMgrImpl"
    }

    /**
     * 前端灰度
     */
    private val h5DoMains = mutableListOf<H5DoMainEntity>()

    /**
     * 公告列表
     */
    private var notices = mutableListOf<NoticeEntity>()

    /**
     * banner列表
     */
    private var banners = mutableListOf<BannerEntity>()

    /**
     * 用户消息列表
     */
    private val userMsgList = mutableListOf<UserMessageListBean>()

    /**
     * 系统公告消息类型的主页消息 的 列表.
     */
    private val noticeMainNoticeEntityList = mutableListOf<MainNoticeEntity>()

//    /**
//     * 用于 保存 所有 新设备免费领取的优惠信息.
//     */
//    private val newDevFreeReceiveInfoMap: ConcurrentHashMap<String, NewDevFreeReceiveInfo> =
//        ConcurrentHashMap<String, NewDevFreeReceiveInfo>()

    /**
     * 首绑消息待处理列表
     */
    private val vasPromotionList = mutableListOf<MainNoticeEntity>()

    /**
     * 服务端推送的用户消息类型的主页消息 的 列表.
     * 这种消息 必然 是 用户消息.
     * 这种消息 是: 由于 收到 topic为"SaasUserMsg.Update"的推送消息 而 拉取的 用户消息.
     */
    private val pushedUserMainNoticeListEntity = mutableListOf<MainNoticeEntity>()

    /**
     * 普通的用户消息类型的主页消息 的 列表.
     */
    private val normalUserMainNoticeListEntity = mutableListOf<MainNoticeEntity>()

    /**
     * 设备分享类型的主页消息 的 列表.
     */
    private val deviceShareInviteNoticeList = mutableListOf<MainNoticeEntity>()

    /**
     * 首绑消息处理的handler
     */
    private var weakHandler: WeakHandler = WeakHandler(this)

    private val scope = MainScope()

    /**
     * 更新公告消息
     *
     * @param fromSaasPush Boolean 是否saas推送
     */
    suspend fun requestNoticeMsg(fromSaasPush: Boolean) {
        repository.getNoticeList(fromSaasPush).collect { action ->
            when (action) {
                HttpAction.Loading<NoticeDataEntity>() -> {

                }

                is HttpAction.Success -> {
                    action.data?.let { msgData ->
                        GwellLogUtils.i(TAG, "getNoticeList $msgData")
                        msgData.h5DoMains?.let { h5DoMains.addAll(it) }
                        msgData.banners?.let { banners.addAll(it) }
                        msgData.noticeEntities?.let { notices.addAll(it) }
                        GwellLogUtils.i(
                            TAG,
                            "requestNoticeMsg: h5DoMains = $h5DoMains, banners = $banners, msgData = $msgData"
                        )

                        // 把 系统公告消息 添加到 系统公告消息队列 中
                        processSystemNotice(notices, fromSaasPush)
                    }

                    requestUserMessage(fromSaasPush)
                }

                is HttpAction.Fail -> {

                }
            }
        }
    }

    /**
     * 获取系统公告消息类型的主页消息 的 列表.
     *
     * @return List<MainNotice> 系统公告消息类型的主页消息 的 列表.
     */
    fun getNoticeMainNoticeList(): List<MainNoticeEntity> {
        return noticeMainNoticeEntityList
    }

    /**
     * 请求 公告消息, 成功 后 再 请求 用户消息.
     * 几个时机:
     * 1) 一个是: app启动后 或 登陆成功后;
     * 2) 一个是: 当 收到 topic为"SaasUserMsg.Update"的推送消息 时, 也会 触发 执行 这个函数.
     * @param fromPush Boolean  是不是 由 topic为"SaasUserMsg.Update"的推送消息 触发的
     */
    override suspend fun requestMsg(fromPush: Boolean) {
        requestNoticeMsg(fromPush)
    }

    /**
     * 清掉 缓存的消息.
     * 当 登陆成功 后, 会 执行 这个函数.
     */
    override fun clearMessage() {
        clearMessage()
    }

    /**
     * 获取 缓存的主页消息列表 中的 第1个主页消息.
     * @return MainNotice?
     */
    override fun getMainNotice(): MainNoticeEntity? {
        return if (deviceShareInviteNoticeList.size > 0) {
            deviceShareInviteNoticeList[0]
        } else if (pushedUserMainNoticeListEntity.size > 0) {
            pushedUserMainNoticeListEntity[0]
        } else if (normalUserMainNoticeListEntity.size > 0) {
            normalUserMainNoticeListEntity[0]
        } else if (noticeMainNoticeEntityList.size > 0) {
            noticeMainNoticeEntityList[0]
        } else {
            null
        }
    }

    /**
     * 获取 推送级别的主页消息列表 中的 第1个主页消息.
     * 这种消息 必然 是 用户消息.
     * 这种消息 是: 由于 收到 topic为"SaasUserMsg.Update"的推送消息 而 拉取的 用户消息.
     * @return MainNotice?
     */
    override fun getPushMainNotice(): MainNoticeEntity? {
        return if (deviceShareInviteNoticeList.size > 0) {
            deviceShareInviteNoticeList[0]
        } else if (pushedUserMainNoticeListEntity.size > 0) {
            pushedUserMainNoticeListEntity[0]
        } else {
            null
        }
    }

    /**
     * 从 主页消息列表 中 删除 指定的主页消息
     * @param mainNoticeEntity MainNotice
     * @return Boolean
     */
    override fun deleteMainNotice(mainNoticeEntity: MainNoticeEntity): Boolean {
        GwellLogUtils.i(TAG, "deleteMainNotice(..), mainNotice = $mainNoticeEntity")
        for (item in deviceShareInviteNoticeList) {
            if (item.msgId == mainNoticeEntity.msgId) {
                return deviceShareInviteNoticeList.remove(item)
            }
        }
        for (item in pushedUserMainNoticeListEntity) {
            if (item.msgId == mainNoticeEntity.msgId) {
                return pushedUserMainNoticeListEntity.remove(item)
            }
        }
        for (item in normalUserMainNoticeListEntity) {
            if (item.msgId == mainNoticeEntity.msgId) {
                return normalUserMainNoticeListEntity.remove(item)
            }
        }
        for (item in noticeMainNoticeEntityList) {
            if (item.tag == mainNoticeEntity.tag) {
                return noticeMainNoticeEntityList.remove(item)
            }
        }
        for (item in vasPromotionList) {
            if (item.tag == mainNoticeEntity.tag) {
                return vasPromotionList.remove(item)
            }
        }
        return false
    }

    /**
     * 获取 增值业务灰度值
     * @return String?
     */
    override fun getVasGrayLevel(): String? {
        return ""
    }

    /**
     * 获取 帮助中心灰度值
     * @return String?
     */
    override fun getHelpServerUrl(): String? {
        return ""
    }

    /**
     * 获取 自营4G灰度
     * @return String?
     */
    override fun getFourCardGrayLevel(): String? {
        return ""
    }

    /**
     * 获取 积分首页灰度值
     * @return String?
     */
    override fun getIntegralHomeGrayLevel(): String? {
        return ""
    }

    /**
     * 获取 首页banner数据
     * @return SystemMessage.Data.Banner?
     */
    override fun getHomeBanner(): BannerEntity? {
        var homeBanner: BannerEntity? = null
        if (banners.isNotEmpty()) {
            for (banner in banners) {
                if (BannerTag.BANNER_HOME.tag == banner.tag) {
                    homeBanner = banner
                    break
                }
            }
        }
        return homeBanner
    }

    /**
     * 获取 首页跑马灯悬浮公告
     * @return SystemMessage.Data.NoticeEntity
     */
    override fun getHomeMarqueeNotice(): NoticeEntity? {
        var noticeEntity: NoticeEntity? = null
        if (banners.isNotEmpty()) {
            // 先从banner中找到important_Home,然后通过banner里面的noticeTag去notice中通过tag找到对应的notice
            banners.find { it.tag == BannerTag.BANNER_MARQUEE_NOTICE.tag }?.let { bannerEntity ->
                notices.find { it.tag == bannerEntity.noticeTag }?.let { notice ->
                    notice.url = bannerEntity.url
                    notice.showOpt = bannerEntity.showOpt
                    noticeEntity = notice
                }
            }
//            for (banner in banners) {
//                if (BannerTag.BANNER_MARQUEE_NOTICE.tag == banner.tag) {
//                    for (notice in notices) {
//                        if (notice.tag == banner.noticeTag) {
//                            notice.url = banner.url
//                            notice.showOpt = banner.showOpt
//                            noticeEntity = notice
//                            break
//                        }
//                    }
//                }
//            }
        }
        return noticeEntity
    }

    /**
     * 获取 我的界面banner数据
     * @return SystemMessage.Data.Banner?
     */
    override fun getMyBanner(): BannerEntity? {
        var myBanner: BannerEntity? = null
        if (banners.isNotEmpty()) {
            for (banner in banners) {
                if (BannerTag.BANNER_MY.tag == banner.tag) {
                    myBanner = banner
                    break
                }
            }
        }
        return myBanner
    }

    /**
     * 获取 首页需要展示在头部的活动
     * @return SystemMessage.Data.Banner?
     */
    override fun getMainHeadBanner(): BannerEntity? {
        var mainHeadBanner: BannerEntity? = null
        if (banners.isNotEmpty()) {
            for (banner in banners) {
                // tag值包含活动tag
                if (BannerTag.BANNER_HOME_TOP.tag == banner.tag) {
                    mainHeadBanner = banner
                    break
                }
            }
        }
        return mainHeadBanner
    }

    /**
     * 获取设备列表页悬浮窗banner
     *
     * @return 设备列表页悬浮窗banner
     */
    override fun getFloatBanner(): BannerEntity? {
        var floatBanner: BannerEntity? = null
        banners.let {
            for (banner in it) {
                // tag值包含
                if (BannerTag.BANNER_FLOAT_ICON.tag == banner.tag) {
                    floatBanner = banner
                    break
                }
            }
        }
        return floatBanner
    }

    /**
     * 设置消息的已读状态
     */
    override suspend fun setUserMessageState(msgId: Long, state: Int): Boolean? {
        return repository.setMessageStatus(msgId, state)
    }

    /**
     * 设置公告的已读状态
     */
    override suspend fun setNoticeState(tag: String, state: Int): Boolean? {
        return repository.setNoticeStatus(tag, state)
    }

    /**
     * 请求用户消息
     *
     * @param fromPush Boolean 是否来自saas推送
     */
    private suspend fun requestUserMessage(fromPush: Boolean) {
        GwellLogUtils.i(TAG, "requestUserMessage(..), fromPush = $fromPush")
        repository.getUserMessageByPageFlow(fromPush).collect { action ->
            when (action) {
                is HttpAction.Loading -> {

                }

                is HttpAction.Success -> {
                    GwellLogUtils.i(
                        TAG,
                        "requestUserMessage(..), fromPush = $fromPush, action.data = ${action.data}"
                    )
                    action.data?.let { userMsg ->
                        userMsg.list?.let {
                            userMsgList.addAll(it)
                            processUserMessageList(it, fromPush)
                        }
                        GwellLogUtils.i(
                            TAG,
                            "requestUserMessage(..), fromPush = $fromPush, userMsg = $userMsg"
                        )
                    }
                }

                is HttpAction.Fail -> {
                }
            }
        }
    }

    /**
     * 处理 系统公告消息.
     *
     * @param list List<NoticeEntity>? 公告数据
     * @param fromPush Boolean 是否来自saas推送
     */
    private fun processSystemNotice(list: List<NoticeEntity>?, fromPush: Boolean) {
        GwellLogUtils.i(TAG, "processSystemNotice(..), fromPush = $fromPush, list = $list")
        if (!list.isNullOrEmpty()) {
            for (notice: NoticeEntity in list) {
                if (ShowOptType.TYPE_HOME_SHOW_DIALOG.type == notice.showOpt
                    && !notice.url.isNullOrEmpty()
                ) {
                    var existedInList = false
                    for (item in noticeMainNoticeEntityList) {
                        if (TextUtils.equals(item.tag, notice.tag)) {
                            // 这个消息 已在 系统消息队列 中了
                            existedInList = true
                            break
                        }
                    }
                    if (!existedInList) {
                        val mainNoticeEntity = MainNoticeEntity(null, notice.url ?: "")
                        mainNoticeEntity.type = MainNoticeEntity.Type.SYSTEM_NOTICE
                        mainNoticeEntity.tag = notice.tag
                        noticeMainNoticeEntityList.add(mainNoticeEntity)
                    }
                }
            }
        }
    }

    /**
     * 处理 用户消息.
     *
     * @param list List<UserMessageListBean>?
     * @param fromPush Boolean
     */
    private suspend fun processUserMessageList(
        list: List<UserMessageListBean>?,
        fromPush: Boolean
    ) {
        GwellLogUtils.i(TAG, "processUserMessageList(..), fromPush = $fromPush, list = $list")
        if (list.isNullOrEmpty()) {
            GwellLogUtils.i(TAG, "processUserMessageList(..), list is null or empty")
            return
        }
        for (listBean in list) {
            if (MsgState.STATUS_UNREAD.state == listBean.status
                && listBean.expireTime > System.currentTimeMillis() / 1000
            ) {
                // 只处理 未读的并且未过期的用户消息
                when (listBean.tag) {
                    TagType.TYPE_POPUP_H5.tag -> {
                        val messageData: UserMessageListBean.MessageData? = JSONUtils.JsonToEntity(
                            listBean.msgData,
                            UserMessageListBean.MessageData::class.java
                        )
                        if (messageData != null) {
                            if (MsgType.TYPE_NEW_DEVICE_RECEIVE.type == messageData.msgType) {
                                if (messageData.expireTime == 0L) {
                                    messageData.expireTime =
                                        (System.currentTimeMillis() + messageData.validateDays * GwTimeUtils.MILLSECONDS_OF_ONE_DAY) / 1000L
                                    tryToConfirmFreeService(
                                        listBean.msgId.toString(),
                                        messageData
                                    )
                                }
                                // 如果 是 新设备免费领取类型, 那么 缓存 起来, 等着 设备列表 来 获取
                                handleNewDevFreeReceiveUserMsg(listBean, messageData)
                            } else {
                                handleGeneralUserMsg(listBean, messageData, fromPush)
                            }
                        }
                    }

                    TagType.TYPE_DEVICE_SHARE_INVITE.tag -> {
                        // TODO 这里暂时不处理 分享消息，后期需要合并处理
//                        val deviceShareInviteData: UserMessageListBean.DeviceShareInviteData =
//                            JSONUtils.JsonToEntity(
//                                listBean.msgData,
//                                UserMessageListBean.DeviceShareInviteData::class.java
//                            )
//                        handleDeviceShareInviteUserMsg(listBean)
                    }
                }
            }
        }
    }

    /**
     * 通知 云服务 启动 开始免费服务的倒计时.
     *
     * @param msgId
     * @param messageData
     */
    private suspend fun tryToConfirmFreeService(
        msgId: String,
        messageData: UserMessageListBean.MessageData
    ) {
        GwellLogUtils.i(
            TAG,
            "tryToConfirmFreeService(..), msgId = $msgId, messageData = $messageData"
        )
        messageData.deviceId?.let { deviceId ->
            repository.confirmShowFreeService(deviceId, msgId).collect {
                when (it) {
                    is HttpAction.Loading -> {

                    }

                    is HttpAction.Success -> {
                        GwellLogUtils.i(
                            TAG,
                            "tryToConfirmFreeService(msgId = $msgId, messageData).onNext(..), it = $it"
                        )
                    }

                    is HttpAction.Fail -> {
                        GwellLogUtils.e(
                            TAG,
                            "tryToConfirmFreeService(msgId = $msgId, messageData).onError(..), it = $it"
                        )
                    }
                }
            }
        }
    }

    /**
     * 处理 新设备免费领取类型的消息
     *
     * @param listBean
     * @param messageData
     */
    private fun handleNewDevFreeReceiveUserMsg(
        listBean: UserMessageListBean,
        messageData: UserMessageListBean.MessageData
    ) {
        GwellLogUtils.i(
            TAG,
            "handleNewDevFreeReceiveUserMsg(..), listBean = $listBean, messageData = $messageData"
        )
        // 如果 已缓存 这个信息, 那么 直接 跳过; 否则, 添加到 缓存 中
//        var newDevFreeReceiveInfo: NewDevFreeReceiveInfo? =
//            newDevFreeReceiveInfoMap.get(messageData.deviceId)
//        if (newDevFreeReceiveInfo == null && messageData.expireTime > System.currentTimeMillis() / 1000) {
//            newDevFreeReceiveInfo = NewDevFreeReceiveInfo()
//            newDevFreeReceiveInfo.msgId = listBean.msgId
//            newDevFreeReceiveInfo.info = messageData
//            newDevFreeReceiveInfoMap.put(messageData.deviceId, newDevFreeReceiveInfo)
//            for (onNewDevFreeReceiveInfoListChangedListener in mOnNewDevFreeReceiveInfoListChangedListener) {
//                onNewDevFreeReceiveInfoListChangedListener.onNewDevFreeReceiveInfoListChanged()
//            }
//        } else {
//            if (messageData.expireTime < System.currentTimeMillis() / 1000) {
//                newDevFreeReceiveInfoMap.remove(messageData.deviceId)
//                for (onNewDevFreeReceiveInfoListChangedListener in mOnNewDevFreeReceiveInfoListChangedListener) {
//                    onNewDevFreeReceiveInfoListChangedListener.onNewDevFreeReceiveInfoListChanged()
//                }
//            }
//        }
    }

    /**
     * 通用用户消息的处理
     *
     * @param listBean UserMessageListBean 用户消息
     * @param messageData MessageData
     * @param fromPush Boolean
     */
    private fun handleGeneralUserMsg(
        listBean: UserMessageListBean,
        messageData: UserMessageListBean.MessageData,
        fromPush: Boolean
    ) {
        GwellLogUtils.i(
            TAG,
            "handleGeneralUserMsg(..), fromPush = $fromPush, listBean = $listBean, messageData = $messageData"
        )
        // 弹窗展示 或 打开webview 的 消息 加入 队列
        if (!messageData.url.isNullOrEmpty() &&
            (ShowWayType.TYPE_DIALOG.type == messageData.showWay ||
                    ShowWayType.TYPE_WEB.type == messageData.showWay)
        ) {
            if (fromPush) {
                var existedInList = false
                for ((_, _, _, msgId) in pushedUserMainNoticeListEntity) {
                    if (msgId == listBean.msgId) {
                        existedInList = true
                        break
                    }
                }
                if (!existedInList) {
                    for (item in normalUserMainNoticeListEntity) {
                        if (item.msgId == listBean.msgId) {
                            normalUserMainNoticeListEntity.remove(item)
                            item.isFromPush = true
                            pushedUserMainNoticeListEntity.add(item)
                            existedInList = true
                            break
                        }
                    }
                }
                if (!existedInList) {
                    val notice = MainNoticeEntity(messageData.deviceId, messageData.url ?: "")
                    notice.type = MainNoticeEntity.Type.USER_MSG
                    notice.msgId = listBean.msgId
                    notice.isFromPush = true
                    notice.showWeb =
                        ShowWayType.TYPE_WEB.type == messageData.showWay
                    pushedUserMainNoticeListEntity.add(notice)
                }
                val notice = pushedUserMainNoticeListEntity.firstOrNull()
                if (MsgType.TYPE_NEW_DEVICE_GO_CLOUD.type == messageData.msgType &&
                    notice != null &&
                    !vasPromotionList.containsNotice(notice)
                ) {
                    vasPromotionList.add(notice)
                    // 首绑消息
                    showGoCloudDialog(notice)
                }
            } else {
                var existedInList = false
                for ((_, _, _, msgId) in pushedUserMainNoticeListEntity) {
                    if (msgId == listBean.msgId) {
                        existedInList = true
                        break
                    }
                }
                if (!existedInList) {
                    for ((_, _, _, msgId) in normalUserMainNoticeListEntity) {
                        if (msgId == listBean.msgId) {
                            existedInList = true
                            break
                        }
                    }
                }
                if (!existedInList) {
                    val notice = MainNoticeEntity(messageData.deviceId, messageData.url ?: "")
                    notice.type = MainNoticeEntity.Type.USER_MSG
                    notice.msgId = listBean.msgId
                    notice.isFromPush = false
                    notice.showWeb =
                        ShowWayType.TYPE_WEB.type == messageData.showWay
                    normalUserMainNoticeListEntity.add(notice)
                }
            }
        }
    }

    @MainThread
    private fun showGoCloudDialog(
        notice: MainNoticeEntity,
    ) {
        val activity = ActivityLifecycleManager.getResumeActivity()
        val devConnectActivity = activityNameApi.getDevConnectName()
        val devAddSuccessActivity = activityNameApi.getDevAddSuccessName()
        val currentActivity = ActivityLifecycleManager.getResumeActivity()?.javaClass?.simpleName
        GwellLogUtils.i(
            TAG,
            "showGoCloudDialog: devConnect ${devConnectActivity}, devAddSuccess $devAddSuccessActivity, currentActivity $currentActivity"
        )
        if (currentActivity == devConnectActivity ||
            currentActivity == devAddSuccessActivity
        ) {
            GwellLogUtils.i(
                TAG,
                "showGoCloudDialog: activity is DevAddSuccessActivity or DevConnectActivity"
            )
            weakHandler.sendEmptyMessageDelayed(1, 3000)
            return
        }
        activity?.let {
            Handler(Looper.getMainLooper()).post {
                webViewApi.showWebViewDialog(
                    activity = it,
                    width = DensityUtil.getScreenWidth(it),
                    height = DensityUtil.getScreenHeight(it),
                    url = notice.url,
                    deviceId = notice.deviceId,
                    callBack = object : WebViewJSCallbackImpl() {
                        /**
                         * 打开其他webview
                         *
                         * @param url webview地址
                         */
                        override fun openWebView(url: String?) {
                            super.openWebView(url)
                            if (url.isNullOrEmpty()) {
                                GwellLogUtils.e(TAG, "openWebView: url is null or empty")
                                return
                            }
                            webViewApi.openWebView(
                                url = url,
                                title = "",
                                deviceId = notice.deviceId
                            )
                        }

                        override fun dialogShow() {
                            super.dialogShow()
                            GwellLogUtils.i(TAG, "dialogShow: notice $notice")
                            deleteMainNotice(notice)
                            scope.launch(Dispatchers.IO) {
                                if (notice.type == MainNoticeEntity.Type.USER_MSG) {
                                    notice.msgId?.let { setUserMessageState(it, 1) }
                                } else if (notice.type == MainNoticeEntity.Type.SYSTEM_NOTICE) {
                                    notice.tag?.let { setNoticeState(it, 1) }
                                }
                            }
                        }

                        override fun dialogDismiss() {
                            super.dialogDismiss()
                            notice.isHaveShow = true
                        }
                    }
                )
            }
        }
    }

    /**
     * 处理 设备分享的消息
     *
     * @param listBean
     * @param deviceShareInviteData
     * @param fromPush
     */
    private fun handleDeviceShareInviteUserMsg(
        listBean: UserMessageListBean,
    ) {
//        GwellLogUtils.i(
//            TAG,
//            "handleDeviceShareInviteUserMsg(..), fromPush = $fromPush, listBean = $listBean, deviceShareInviteData = $deviceShareInviteData"
//        )
//        if (!deviceShareInviteData.url.isNullOrEmpty() &&
//            (deviceShareInviteData.showWay == ShowWayType.TYPE_DIALOG.type
//                    || deviceShareInviteData.showWay == ShowWayType.TYPE_WEB.type)
//        ) {
//            var existedInList = false
//            for ((_, _, _, msgId) in deviceShareInviteNoticeList) {
//                if (msgId == listBean.msgId) {
//                    existedInList = true
//                    break
//                }
//            }
//            if (!existedInList) {
//                val notice =
//                    MainNoticeEntity(
//                        deviceShareInviteData.deviceId,
//                        deviceShareInviteData.url ?: ""
//                    )
//                // 注意: 本质上 仍然是 用户消息
//                notice.type = MainNoticeEntity.Type.USER_MSG
//                notice.msgId = listBean.msgId
//                notice.isFromPush = fromPush
//                notice.showWeb =
//                    ShowWayType.TYPE_WEB.type == deviceShareInviteData.showWay
//                deviceShareInviteNoticeList.add(notice)
//            }
//        }
    }

    override fun handleMsg(p0: Message?) {
        when (p0?.what) {
            1 -> {
                val notice = vasPromotionList.firstOrNull()
                if (notice != null) {
                    showGoCloudDialog(notice)
                }
            }
        }
    }
}

fun List<MainNoticeEntity>.containsNotice(value: MainNoticeEntity): Boolean {
    // 在这里自定义你的 contains 行为
    return any { it.tag == value.tag }
}