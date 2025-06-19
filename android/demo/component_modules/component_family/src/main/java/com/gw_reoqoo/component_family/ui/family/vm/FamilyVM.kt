package com.gw_reoqoo.component_family.ui.family.vm

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.gw.component_debug.api.interfaces.IShakeApi
import com.gw_reoqoo.component_family.api.interfaces.IGuideDataStore
import com.gw_reoqoo.component_family.api.interfaces.IShareDeviceApi
import com.gw_reoqoo.component_family.entrties.Scene
import com.gw_reoqoo.component_family.repository.DeviceRepository
import com.gw_reoqoo.component_family.repository.SceneRepository
import com.gw_reoqoo.component_family.repository.UserMsgRepository
import com.gw_reoqoo.component_family.ui.family.bean.FragmentBeanWrapper
import com.gw.component_plugin_service.api.IPluginManager
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.cp_account.api.kapi.IUserInfo
import com.gw.cp_config.api.IAppConfigApi
import com.gw.cp_config_net.api.interfaces.DevShareConstant
import com.gw.cp_msg.api.kapi.INoticeMgrApi
import com.gw.cp_msg.entity.http.BannerEntity
import com.gw.cp_msg.entity.http.MainNoticeEntity
import com.gw.cp_msg.entity.http.NoticeEntity
import com.gw.lib_http.entities.DeviceHistoryBean
import com.gw_reoqoo.component_iotapi_plugin.opt.api.IGWIotOpt
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.entities.MessageBean
import com.gw_reoqoo.lib_http.entities.MessageStatus
import com.gw_reoqoo.lib_http.entities.ScanShareQRCodeResult
import com.gw_reoqoo.lib_room.device.DeviceInfo
import com.gw_reoqoo.lib_room.ktx.isMaster
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RS


/**
 * @Description: - 家庭界面的ViewModel
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 *
 * @param deviceRepository 设备的Repository
 * @param sceneRepository 场景的Repository
 * @param userMsgRepository 用户消息的Repository
 * @param pluginManager 插件模块的控制器
 * @param accountApi 用户信息模块的API
 * @param guideDataStore 引导信息的DataStore
 * @param scanShareDeviceApi 扫分享设备二维码
 */
@HiltViewModel
class FamilyVM @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val sceneRepository: SceneRepository,
    private val userMsgRepository: UserMsgRepository,
    private val pluginManager: IPluginManager,
    private val accountApi: IAccountApi,
    private val guideDataStore: IGuideDataStore,
    private val scanShareDeviceApi: IShareDeviceApi,
    private val configApi: IAppConfigApi,
    private val noticeMgrApi: INoticeMgrApi,
    private val shakeMgrApi: IShakeApi,
    private val igwIotOpt: IGWIotOpt,
) : ABaseVM() {

    companion object {
        private const val TAG = "FamilyVM"
        private const val PARAM_PID_KEY = "Pid"
    }

    // 监听设备列表数据改变
    private val deviceListObserve = Observer<List<DeviceInfo>> {
        loadAdapterList()
    }

    // 监听场景列表数据改变
    private val sceneListObserve = Observer<List<Scene>> {
        loadAdapterList()
    }

    /**
     * 登录的用户信息
     */
    private val _accountInfo = MutableLiveData<IUserInfo>()

    /**
     * 适配器的数据
     */
    private val _adapterList = MutableLiveData<List<FragmentBeanWrapper>>()

    /**
     * 设备列表
     */
    private val _deviceList = MutableLiveData<List<DeviceInfo>>()

    /**
     * 当前的设备列表
     */
    private var currentDeviceListLD: LiveData<List<DeviceInfo>>? = null

    /**
     * 场景列表
     */
    private val _sceneList = MutableLiveData<List<Scene>>()

    /**
     * 未读的设备分享消息列表
     */
    private val _unRadDevShareList = MutableLiveData<List<MessageBean>>()

    /**
     * 添加按钮的引导
     */
    private val _addBtnGuide = MutableLiveData<Boolean>()

    /**
     * 首页弹框
     */
    private val _mainNoticeEntity = MutableLiveData<MainNoticeEntity>()

    /**
     * 悬浮框
     */
    private val _floatBannerEntity = MutableLiveData<BannerEntity>()

    /**
     * 首页公告banner
     */
    private val _homeBannerEntity = MutableLiveData<BannerEntity>()


    /**
     * 首页跑马灯悬浮公告
     */
    private val _homeNoticeEntity = MutableLiveData<NoticeEntity>()

    /**
     * 添加按钮的引导
     */
    val addBtnGuide: LiveData<Boolean> get() = _addBtnGuide

    /**
     * 未读的设备分享消息列表
     */
    val unRadDevShareList: LiveData<List<MessageBean>> get() = _unRadDevShareList

    /**
     * 场景列表
     */
    val sceneList: LiveData<List<Scene>> get() = _sceneList

    /**
     * 设备列表
     */
    val deviceList: LiveData<List<DeviceInfo>> get() = _deviceList

    /**
     * 适配器的数据
     */
    val adapterList: LiveData<List<FragmentBeanWrapper>> get() = _adapterList

    /**
     * 登录的用户信息
     */
    private val accountInfo: IUserInfo? get() = _accountInfo.value

    /**
     * 登录的用户信息
     */
    val watchAccountInfo: LiveData<IUserInfo?> get() = _accountInfo

    /**
     * 当扫描成功，并且解析的livedata
     */
    val onScanShareDevice: LiveData<Map<String, String>> get() = scanShareDeviceApi.onScanShareDevice

    /**
     * 公告消息
     */
    val mainNoticeEntity: LiveData<MainNoticeEntity> get() = _mainNoticeEntity

    /**
     * 悬浮窗banner
     */
    val floatBannerEntity: LiveData<BannerEntity> get() = _floatBannerEntity

    /**
     * 首页公告banner
     */
    val homeBannerEntity: LiveData<BannerEntity> get() = _homeBannerEntity

    /**
     * 首页跑马灯悬浮公告
     */
    val mHomeNoticeEntity: LiveData<NoticeEntity> get() = _homeNoticeEntity

    /**
     * 历史设备列表
     */
    private val _historyDeviceList = MutableLiveData<List<DeviceHistoryBean>>()

    val mHistoryDeviceList: LiveData<List<DeviceHistoryBean>> get() = _historyDeviceList

    /**
     * 加载用户信息
     */
    private fun loadAccountInfo() {
        val userInfo = accountApi.getSyncUserInfo()
        userInfo?.let(_accountInfo::setValue)
    }

    /**
     * 获取首页顶部的名称
     */
    fun getHomeTopName(): String? {
        val accountInfo = accountInfo ?: return null
        val nickName = accountInfo.nickName
        if (!nickName.isNullOrEmpty()) {
            return nickName
        }
        val phone = accountInfo.phone
        if (!phone.isNullOrEmpty()) {
            if (phone.length <= 7) return phone
            val sub = phone.substring(3, phone.length - 4)
            return phone.replace(sub, "*".repeat(4))
        }
        val email = accountInfo.email
        if (!email.isNullOrEmpty()) {
            if (email.length <= 6) return email
            val sub = email.substring(3, email.length - 3)
            return email.replace(sub, "*".repeat(4))
        }
        return null
    }


    /**
     * 加载适配器的数据
     */
    private fun loadAdapterList() {
        val userId = accountInfo?.userId ?: return
        val deviceList = _deviceList.value
        val sceneList = _sceneList.value
        val newList = ArrayList<FragmentBeanWrapper>()
        // 获取设备列表界面的URL
        val firstFragmentUrl = if (deviceList.isNullOrEmpty()) {
            ReoqooRouterPath.Family.FAMILY_FRAGMENT_DEVICE_EMPTY_PATH
        } else {
            ReoqooRouterPath.Family.FAMILY_FRAGMENT_DEVICE_LIST_PATH
        }
        // 如果新的url跟老的url相同，则用回老的，否则就新建一个
        val deviceWrapper = FragmentBeanWrapper(
            textSrc = RS.string.AA0214,
            fragmentUrl = firstFragmentUrl,
            params = mapOf("userId" to userId)
        )
        newList.add(deviceWrapper)

        // 获取场景列表界面的URL
        val secondFragmentUrl = if (sceneList.isNullOrEmpty()) {
            ReoqooRouterPath.Family.FAMILY_FRAGMENT_SCENE_EMPTY_PATH
        } else {
            ReoqooRouterPath.Family.FAMILY_FRAGMENT_SCENE_LIST_PATH
        }
        // 如果新的url跟老的url相同，则用回老的，否则就新建一个
        val sceneWrapper = FragmentBeanWrapper(
            textSrc = RS.string.AA0501,
            fragmentUrl = secondFragmentUrl,
            params = mapOf("userId" to userId)
        )

        // 暂时隐藏场景选项
        // newList.add(sceneWrapper)

        _adapterList.postValue(newList)
    }


    /**
     * 加载设备列表
     */
    private fun loadDeviceList(owner: LifecycleOwner) {
        val userId = accountInfo?.userId ?: return
        currentDeviceListLD?.removeObservers(owner)
        val livedata = deviceRepository.getAllDeviceBy(userId)
        livedata.observe(owner) { list ->
            _deviceList.value = list
        }
        currentDeviceListLD = livedata
    }

    /**
     * 从网络加载最新设备列表
     */
    fun loadRemoteDeviceList(callBack: (() -> Unit)? = null) {
        GwellLogUtils.i(TAG, "loadRemoteDeviceList")
        viewModelScope.launch {
            igwIotOpt.refreshDevices()
            deviceRepository.loadDeviceFromRemote()
            callBack?.invoke()
        }
    }

    fun openHome(devId: String) {
        viewModelScope.launch {
            igwIotOpt.openHome(devId)
        }
    }

    /**
     * 更新公告消息
     */
    fun loadNoticeList() {
        viewModelScope.launch(Dispatchers.IO) {
            noticeMgrApi.requestMsg(true)
            noticeMgrApi.getMainNotice()?.let {
                GwellLogUtils.i(TAG, "getMainNotice: $it")
                _mainNoticeEntity.postValue(it)
            }
            noticeMgrApi.getFloatBanner()?.let {
                GwellLogUtils.i(TAG, "getFloatBanner: $it")
                _floatBannerEntity.postValue(it)
            }
            noticeMgrApi.getHomeBanner()?.let {
                GwellLogUtils.i(TAG, "getHomeBanner: $it")
                _homeBannerEntity.postValue(it)
            }
            noticeMgrApi.getHomeMarqueeNotice()?.let {
                GwellLogUtils.i(TAG, "getHomeMarqueeNotice: $it")
                _homeNoticeEntity.postValue(it)
            }
        }
    }

    /**
     * 加载场景列表
     */
    private fun loadSceneList() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = accountInfo?.userId ?: return@launch
            val sceneList = sceneRepository.loadSceneList(userId)
            _sceneList.postValue(sceneList)
        }
    }

    /**
     * 加载设备和场景列表
     */
    fun loadDeviceAndSceneList(
        owner: LifecycleOwner, callBack: (() -> Unit)? = null
    ) {
        GwellLogUtils.i(TAG, "loadDeviceAndSceneList")
        this.loadDeviceList(owner)
        this.loadSceneList()
        this.loadRemoteDeviceList {
            callBack?.invoke()
        }
    }


    /**
     * 获取未读的设备分享消息列表
     */
    fun loadUnRadDevShareList() {
        viewModelScope.launch(Dispatchers.IO) {
            // 由于p2p接收到到消息后，马上去查询未读消息可能会为空，所以延迟300ms
            delay(300)
            val list = userMsgRepository.loadUnReadDeviceShareMsgList()
            _unRadDevShareList.postValue(list)
        }
    }

    /**
     * 忽略设备分享消息,并且重新加载分享消息
     */
    fun ignoreAndRestoreDevShareMessage() {
        viewModelScope.launch {
            val list = unRadDevShareList.value ?: emptyList()
            for (messageBean in list) {
                userMsgRepository.setMessageStatus(messageBean.msgId, MessageStatus.READ)
            }
            loadUnRadDevShareList()
        }
    }

    /**
     * 解绑设备
     */
    fun deleteDevice(device: DeviceInfo): Flow<HttpAction<Any>> {
        val flow = if (device.isMaster) {
            deviceRepository.unbindDevice(device.deviceId)
        } else {
            deviceRepository.cancelShare(device.deviceId)
        }
        return flow.map { action ->
            when (action) {
                is HttpAction.Loading -> {}
                is HttpAction.Fail -> {}
                is HttpAction.Success -> {
                    igwIotOpt.refreshDevices()
                    if (device.isMaster) {
                        pluginManager.onDeviceDeleted(device.deviceId)
                    } else {
                        pluginManager.onDeviceCancelShare(device.deviceId)
                    }
                    loadRemoteDeviceList()
                }
            }
            action
        }
    }

    /**
     * 加载 首页右上角的引导弹窗的数据
     */
    private fun loadAddBtnGuide() {
        viewModelScope.launch {
            val shown = guideDataStore.getAddBtnGuide()
            _addBtnGuide.postValue(shown)
        }
    }

    /**
     * 右上角添加引导弹窗点击我知道了
     */
    fun iKnowAddBtnGuide() {
        viewModelScope.launch {
            guideDataStore.setAddBtnGuide(true)
            _addBtnGuide.postValue(true)
        }
    }

    /**
     * 通过扫描别人分享的设备二维码添加设备
     */
    fun addDeviceByScanShareCode(params: Map<String, String>): Flow<HttpAction<ScanShareQRCodeResult>>? {
        val qrcodeToken = params[DevShareConstant.PARAMS_INVITE_CODE]
        val pid = params[PARAM_PID_KEY]
        val deviceId = params[DevShareConstant.PARAMS_DEVICE_ID] ?: ""
        val productName = configApi.getProductName(pid ?: "")
        GwellLogUtils.i(
            TAG,
            "addDeviceByScanShareCode-pid=$pid,productName=$productName,deviceId=$deviceId"
        )
        if (!qrcodeToken.isNullOrEmpty()) {
            return deviceRepository.addDeviceByScanShareCode(qrcodeToken, productName ?: deviceId)
        }
        return null
    }

    init {
        _deviceList.observeForever(deviceListObserve)
        _sceneList.observeForever(sceneListObserve)
        loadAccountInfo()
        loadAddBtnGuide()
        viewModelScope.launch {
            accountApi.watchUserInfo().asFlow().collect { info ->
                info?.let(_accountInfo::postValue)
            }
        }
        shakeMgrApi.initShakeMgr()
    }


    override fun onCleared() {
        _deviceList.removeObserver(deviceListObserve)
        _sceneList.removeObserver(sceneListObserve)
        super.onCleared()
    }

    /**
     * 历史用户首次登录新app获取相关设备信息
     */
    fun getDeviceHistoryList() {
        viewModelScope.launch(Dispatchers.IO) {
            val deviceList = userMsgRepository.getDeviceHistoryList()
            _historyDeviceList.postValue(deviceList)
        }
    }
}