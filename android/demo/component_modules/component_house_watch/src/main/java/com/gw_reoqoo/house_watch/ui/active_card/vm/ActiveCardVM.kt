package com.gw_reoqoo.house_watch.ui.active_card.vm

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw.component_plugin_service.api.IPluginManager
import com.gw.component_plugin_service.api.ModeResponse
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.house_watch.data.repository.ActiveRepository
import com.gw_reoqoo.house_watch.entities.ActiveTypeWrapper
import com.gw_reoqoo.house_watch.entities.DeviceWrapper
import com.gw_reoqoo.house_watch.uitls.TimeUtils
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.HttpResp
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.entities.ActiveBean
import com.gw_reoqoo.lib_http.entities.ActiveList
import com.gw_reoqoo.lib_http.jsonToEntity
import com.gw.lib_plugin_service.IResultCallback
import com.gw_reoqoo.component_iotapi_plugin.opt.api.IGWIotOpt
import com.gw_reoqoo.component_iotapi_plugin.opt.api.PluginSolution
import com.gw_reoqoo.lib_utils.ktx.bitAt
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.math.roundToLong
import com.gw_reoqoo.resource.R as RR

/**
 * @Description: - 活动卡片的VM
 * @Author: XIAOLEI
 * @Date: 2023/8/18
 */
@HiltViewModel
class ActiveCardVM @Inject constructor(
    private val app: Application,
    private val repository: ActiveRepository,
    private val accountApi: IAccountApi,
    private val familyModeApi: FamilyModeApi,
    private val configApi: IAppConfigApi,
    private var pluginManager: IPluginManager,
    private var igwIotOpt: IGWIotOpt,
) : ABaseVM() {
    companion object {

        private const val TAG = "ActiveCardVM"

        /**
         * 防抖加载活动列表传给 handler的TOKEN
         */
        private const val LOAD_ACTIVE_TOKEN = "LOAD_ACTIVE_TOKEN"

        /**
         * 一天的秒数
         */
        private const val oneDaySecond = 1 * 24 * 60 * 60L

    }

    private val handler = Handler(Looper.getMainLooper())

    /**
     * 活动列表
     */
    private val _activeBeanList = MutableLiveData<LinkedList<ActiveBean>>(LinkedList())

    /**
     * 事件列表
     */
    private val _activeList =
        MutableLiveData<List<ActiveTypeWrapper>>()

    /**
     * 设备列表
     */
    private val _deviceList =
        MutableLiveData<List<DeviceWrapper>>()

    /**
     * 预览类型
     */
    private val _viewType = MutableLiveData<PreviewType>()

    /**
     * 事件列表
     */
    val activeList: LiveData<List<ActiveTypeWrapper>> get() = _activeList

    /**
     * 活动列表
     */
    val activeBeanList: LiveData<LinkedList<ActiveBean>> get() = _activeBeanList

    /**
     * 设备列表
     */
    val deviceList: LiveData<List<DeviceWrapper>> get() = _deviceList

    /**
     * 网络状态
     */
    val netWorkSate = MutableLiveData<HttpAction<ActiveList>>()

    /**
     * 记录显示的次数
     */
    val showCount = AtomicInteger(0)

    /**
     * 预览类型
     */
    val viewType: LiveData<PreviewType> get() = _viewType

    /**
     * 设备预览类型（是单设备还是多设备）
     *
     * @param deviceWrappers List<DeviceWrapper> 设备列表
     */
    fun getPreviewType(deviceWrappers: List<DeviceWrapper>) {
        val checkedDevices: List<DeviceWrapper> =
            deviceWrappers.filter {
                it.checked
            }
        GwellLogUtils.i(TAG, "checkedDevices = $checkedDevices")
        _viewType.postValue(
            when (deviceWrappers.size) {
                0, 1 -> {
                    // 没有设备
                    PreviewType.DEFAULT
                }

                2 -> {
                    PreviewType.SINGLE
                }

                else -> {
                    when (checkedDevices.size) {
                        0 -> {
                            PreviewType.DEFAULT
                        }

                        1 -> {
                            if (checkedDevices.firstOrNull()?.device == null) {
                                PreviewType.MULTI
                            } else {
                                PreviewType.SINGLE
                            }
                        }

                        else -> {
                            PreviewType.MULTI
                        }
                    }
                }
            }
        )
    }

    /**
     * 根据设备ID获取设备名称
     *
     * @param devId 设备ID
     */
    fun getDeviceName(devId: String): String {
        val device = familyModeApi.deviceInfo(devId)
        val pid = device?.productId ?: ""
        val remarkName = device?.remarkName ?: ""
        return remarkName.ifEmpty {
            configApi.getProductName(pid) ?: devId
        }
    }

    /**
     * 防抖刷新活动列表
     * @param delay 多久时间内防抖
     */
    fun debounceRefreshData(delay: Long = 300L) {
        handler.removeCallbacksAndMessages(LOAD_ACTIVE_TOKEN)
        handler.postDelayed(delay, LOAD_ACTIVE_TOKEN) {
            viewModelScope.launch {
                refreshActiveList()
            }
        }
    }

    /**
     * 刷新活动列表
     */
    fun refreshActiveList() {
        GwellLogUtils.i(TAG, "refreshActiveList")
        endTime = (System.currentTimeMillis() / 1000f).roundToLong()
        startTime = endTime - oneDaySecond
        loadActiveList(true, startTime, endTime)
    }

    /**
     * 加载更多活动列表
     */
    fun loadMoreActiveList() {
        loadActiveList(false, startTime, endTime)
    }

    /**
     * 筛选结束时间（当前时间）
     */
    private var endTime = (System.currentTimeMillis() / 1000f).roundToLong()

    /**
     * 筛选开始时间(24小时以前)
     */
    private var startTime = endTime - oneDaySecond


    /**
     * 查询结束到结尾了
     */
    val endOfEvent get() = startTime == endTime


    /**
     * 加载更多
     * @param refresh 是否是刷新操作
     * @param startTime 事件开始时间(单位秒)
     * @param endTime 事件结束时间(单位秒)
     */
    private fun loadActiveList(
        refresh: Boolean,
        startTime: Long,
        endTime: Long,
    ) {
        // 设备ID列表
        val allDevIds = _deviceList.value?.mapNotNull { it.device?.deviceId } ?: emptyList()
        GwellLogUtils.i(TAG, "allDevIds:$allDevIds")
        val checkedDev = _deviceList.value?.filter { it.checked }
        GwellLogUtils.i(TAG, "checkedDev:$checkedDev")
        val checkedDevIds = checkedDev?.mapNotNull { it.device?.deviceId }
        GwellLogUtils.i(TAG, "checkedDevIds:$checkedDevIds")
        val devIds = if (checkedDevIds.isNullOrEmpty()) allDevIds else checkedDevIds
        GwellLogUtils.i(TAG, "devIds:$devIds")
        // 事件类型过滤列表
        val almTypeMasks = _activeList.value?.filter {
            it.checked
        }?.mapNotNull { it.type?.let { type -> 1L shl type.bitOfIndex } } ?: emptyList()
        GwellLogUtils.i(TAG, "almTypeMasks:$almTypeMasks")
        val masks = almTypeMasks.sum()
        GwellLogUtils.i(TAG, "masks:$masks")

        viewModelScope.launch {
            repository.getActiveList(devIds, startTime, endTime, listOf(masks))
                .onSuccess {
                    this?.let { list ->
                        GwellLogUtils.i(TAG, "onSuccess loadActiveList-> $list")
                        netWorkSate.postValue(HttpAction.Success(list))
                        val serverStartTime = list.startTime
                        val activeList = list.list ?: emptyList()
                        val filterActiveList = activeList.toMutableList().filter { bean ->
                            // 需要丢弃，事件图片为空的事件（无论是视频还是图片事件，都需要过滤）
                            !bean.imgUrl.isNullOrEmpty() || bean.thumbUrlSuffix.isNotEmpty()
                        }
                        val oldList = _activeBeanList.value ?: LinkedList()
                        if (refresh) {
                            oldList.clear()
                        }
                        oldList.addAll(filterActiveList)
                        this@ActiveCardVM.endTime = serverStartTime
                        _activeBeanList.postValue(oldList)
                    }
                }
                .onServerError { code, msg ->
                    GwellLogUtils.e(TAG, "onServerError loadActiveList-> code:$code msg:$msg")
                    val exception =
                        ResponseNotSuccessException(code, msg, HttpResp(code, msg, ""), msg ?: "")
                    netWorkSate.postValue(HttpAction.Fail(exception))
                }
//            flow.collect { action ->
//                netWorkSate.postValue(action)
//                GwellLogUtils.i(TAG, "loadActiveList->$action")
//                when (action) {
//                    is HttpAction.Loading -> Unit
//                    is HttpAction.Fail -> Unit
//                    is HttpAction.Success -> {
//                        val list = action.data
//                        if (list != null) {
//                            val serverStartTime = list.startTime
//                            val activeList = list.list ?: emptyList()
//                            val filterActiveList = activeList.toMutableList().filter { bean ->
//                                // 需要丢弃，事件图片为空的事件（无论是视频还是图片事件，都需要过滤）
//                                !bean.imgUrl.isNullOrEmpty() || bean.thumbUrlSuffix.isNotEmpty()
//                            }
//                            val oldList = _activeBeanList.value ?: LinkedList()
//                            if (refresh) {
//                                oldList.clear()
//                            }
//                            oldList.addAll(filterActiveList)
//                            this@ActiveCardVM.endTime = serverStartTime
//                            _activeBeanList.postValue(oldList)
//                        }
//                    }
//                }
//            }
        }
    }

    /**
     * 加载事件类型列表和设备列表
     */
    fun loadEventTypesAndDevices() {
        this.loadEventList()
        this.loadDeviceList()
    }

    /**
     * 加载时间类型
     *
     * @param eventList 事件列表
     * @return List<Any>
     */
    fun loadTimeType(eventList: List<ActiveBean>): List<Any> {
        GwellLogUtils.i(TAG, "loadTimeType: $eventList")
        val eventData = mutableListOf<Any>()
        if (eventList.isEmpty()) {
            return eventList
        }
        var isYesterdayAdd = false
        for (i in eventList.indices) {
            val bean = eventList[i]
            val isYesterday = TimeUtils.isBeforeYesterday(bean.startTime)
            if (isYesterday && !isYesterdayAdd) {
                isYesterdayAdd = true
                val timeBean =
                    com.gw_reoqoo.lib_http.entities.TimeBean(app.getString(RR.string.AA0417))
                eventData.add(timeBean)
            }
            eventData.add(bean)
        }
        return eventData
    }

    /**
     * 加载事件列表
     */
    private fun loadEventList() {
        val list = LinkedList<ActiveTypeWrapper>()
        list.add(ActiveTypeWrapper(null, true))
        list.addAll(com.gw_reoqoo.house_watch.entities.ActiveType.values().map { type ->
            ActiveTypeWrapper(type, checked = false)
        })
        _activeList.value = list
    }

    /**
     * 获取设备列表
     */
    private fun loadDeviceList() {
        val userId = accountApi.getSyncUserId() ?: return
        viewModelScope.launch {
            familyModeApi.watchDeviceList(userId).asFlow().map { devList ->
                val list = LinkedList<DeviceWrapper>()
                // 先添加全部
                list.add(DeviceWrapper(null, true))
                // 再添加单个
                if (configApi.getPermissionMode() == 0) {
                    list.addAll(devList.map { device ->
                        DeviceWrapper(device, false)
                    })
                } else {
                    devList.map {
                        if (it.isMaster) {
                            list.add(DeviceWrapper(it, false))
                        } else {
                            val deferred = async {
                                getDevicePlaybackPermission(it.deviceId)
                            }
                            val result = deferred.await()
                            GwellLogUtils.i(
                                TAG,
                                "getDevicePlaybackPermission: ${it.deviceId} $result"
                            )
                            if (result) {
                                list.add(
                                    DeviceWrapper(
                                        it,
                                        false
                                    )
                                )
                            } else {
                            }
                        }
                    }
                }
                val oldList = _deviceList.value ?: emptyList()
                for (oldWrapper in oldList) {
                    for (newWrapper in list) {
                        if (oldWrapper.device?.deviceId == newWrapper.device?.deviceId) {
                            newWrapper.checked = oldWrapper.checked
                        }
                    }
                }
                list
            }.collect { devList ->
                GwellLogUtils.i(TAG, "loadDeviceList: $devList")
                _deviceList.postValue(devList)
            }
        }
    }

    private suspend fun getDevicePlaybackPermission(devId: String): Boolean =
        suspendCancellableCoroutine { cont ->
            checkPermissionById(devId) { result ->
                cont.resumeWith(result)
            }
        }

    private fun checkPermissionById(deviceId: String, callback: (Result<Boolean>) -> Unit) {
        viewModelScope.launch {
            val hasPermission = igwIotOpt.getCloudPlaybackPermission(deviceId)
            callback(Result.success(hasPermission))
        }
    }

    /**
     * 获取当前选中设备的名称
     *
     * @return String? 设备名称
     */
    fun getCheckedDevName(): String? {
        val list = _deviceList.value
        if (list.isNullOrEmpty()) {
            return null
        }
        // 单设备预览类型时获取设备名称，如果只有一台设备，直接返回。如果有多台设备，则取选中的设备
        GwellLogUtils.i(TAG, "list $list")
        if (list.size == 2 && list[0].device == null) {
            return list[1].device?.remarkName
        }
        for (i in list.indices) {
            if (list[i].checked && list[i].device != null) {
                return list[i].device?.remarkName
            }
        }
        return null
    }

    /**
     * 从配置弹窗中更新数据
     */
    fun setDataFromConfig(
        devList: List<DeviceWrapper>,
        types: List<ActiveTypeWrapper>
    ) {
        _deviceList.postValue(devList)
        _activeList.postValue(types)
    }

    /**
     * 开启回放页
     */
    fun startPlaybackPage(bean: ActiveBean) {
        val devId = "${bean.devId}"
        val alarmId = bean.alarmId
        val device = familyModeApi.deviceInfo(devId) ?: return
        val pid = device.productId ?: ""
        if (familyModeApi.isReoqooDevice(pid)) {
            pluginManager.startPlaybackActivity(deviceId = devId, alarmId = alarmId)
        } else {
            viewModelScope.launch {
                igwIotOpt.openPlayback(devId, alarmId, bean.startTime * 1000, PluginSolution.YOOSEE)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * 预览类型
     */
    enum class PreviewType {
        DEFAULT,
        SINGLE,
        MULTI
    }

}