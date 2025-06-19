package com.gw_reoqoo.house_watch.ui.video_card.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IGuideDataStore
import com.gw_reoqoo.component_iotapi_plugin.opt.api.IGWIotOpt
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.house_watch.data.data_store.VideoDataStore
import com.gw_reoqoo.house_watch.entities.DevicePack
import com.gw_reoqoo.house_watch.entities.ViewTypeModel
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * @Description: - 视频卡片的VM
 * @Author: XIAOLEI
 * @Date: 2023/8/18
 *
 * @param familyModeApi 家庭模块提供的API
 * @param accountApi 账户模块提供的API
 * @param guideDataStore 新手引导提供的API
 */
@HiltViewModel
class VideoCardVM @Inject constructor(
    private val familyModeApi: FamilyModeApi,
    private val accountApi: IAccountApi,
    private val guideDataStore: IGuideDataStore,
    private val videoDataStore: VideoDataStore,
    private val igwIotOpt: IGWIotOpt,
) : ABaseVM() {

    companion object {
        private const val TAG = "VideoCardVM"
    }

    /**
     * 设备列表
     */
    private val _deviceList = MutableLiveData<List<DevicePack>>()

    /**
     * 预览类型
     */
    private val _viewType = MutableLiveData<ViewTypeModel>()

    /**
     * 设备卡片新手引导数据
     */
    private val _videoCardGuide = MutableLiveData<Boolean>()

    /**
     * 设备卡片新手引导数据
     */
    val videoCardGuide: LiveData<Boolean> get() = _videoCardGuide

    /**
     * 设备列表
     */
    val deviceList: LiveData<List<DevicePack>> get() = _deviceList

    /**
     * 预览类型
     */
    val viewType: LiveData<ViewTypeModel> get() = _viewType

    /**
     * 是否提示了移动数据
     */
    val hasTipMobileData = AtomicBoolean(false)

    init {
        loadVideoCardGuide()
    }

    /**
     * 加载设备列表
     */
    fun loadDeviceList() {
        GwellLogUtils.i(TAG, "loadDeviceList")
        viewModelScope.launch {
            val userId = accountApi.getAsyncUserId()
            if (!userId.isNullOrEmpty()) {
                familyModeApi.watchDeviceList(userId)
                    .asFlow()
                    .map { list ->
                        list.map { device ->
                            DevicePack(device)
                        }
                    }
                    .collect {
                        _deviceList.postValue(sortDeviceList(it))
                    }
            }
        }
    }

    /**
     * 本地配置 排序设备列表
     */
    private suspend fun sortDeviceList(devices: List<DevicePack>): List<DevicePack> {
        GwellLogUtils.i(TAG, "sortDeviceList: devices=$devices")
        val sortConfig = videoDataStore.getDevSortConfig()
        GwellLogUtils.i(TAG, "sortDeviceList: sortConfig=$sortConfig")
        if (sortConfig.isNullOrEmpty()) {
            return devices
        }
        val deviceList = devices.toMutableList()
        val sortList = mutableListOf<DevicePack>()
        sortConfig.map { sortDevice ->
            GwellLogUtils.i(TAG, "sortDeviceList: sortDevice=$sortDevice")
            val deviceInfo: DevicePack? = deviceList.find { it.deviceId == sortDevice["deviceId"] }
            GwellLogUtils.i(TAG, "sortDeviceList: deviceInfo=$deviceInfo")
            if (deviceInfo != null) {
                deviceInfo.offView = sortDevice["offView"] as Boolean
                sortList.add(deviceInfo)
                deviceList.remove(deviceInfo)
            }
        }
        GwellLogUtils.i(TAG, "sortDeviceList: deviceList=$deviceList")
        sortList.addAll(deviceList)
        GwellLogUtils.i(TAG, "sortDeviceList: sortList=$sortList")
        return sortList
    }

    /**
     * 设置视频设置
     */
    fun setVideoSettings() {
        viewModelScope.launch {
            _viewType.value = videoDataStore.getVideoShowType()
        }
    }

    /**
     * 刷新远程设备列表
     */
    fun refreshDeviceListFromRemote() {
        GwellLogUtils.i(TAG, "refreshDeviceListFromRemote")
        viewModelScope.launch {
            familyModeApi.refreshDevice()
            igwIotOpt.refreshDevices()
        }
    }

    /**
     * 设置排序后的设备列表
     */
    fun setSortDeviceList(list: List<DevicePack>) {
        GwellLogUtils.i(TAG, "setSortDeviceList(${list})")
        _deviceList.value = list
        viewModelScope.launch {
            videoDataStore.setDevSortConfig(list)
        }
    }

    /**
     * 设置视图类型
     */
    fun setViewType(viewTypeModel: ViewTypeModel) {
        this._viewType.value = viewTypeModel
        viewModelScope.launch {
            videoDataStore.setVideoShowType(viewTypeModel)
        }
    }

    /**
     * 加载设备卡片的引导数据
     */
    fun loadVideoCardGuide() {
        viewModelScope.launch {
            val shown = guideDataStore.getVideoCardGuide()
            _videoCardGuide.postValue(shown)
        }
    }

    /**
     * 设备卡片引导-我知道了
     */
    fun iKnowVideoCardGuide() {
        viewModelScope.launch {
            guideDataStore.setVideoCardGuide(true)
            _videoCardGuide.postValue(true)
        }
    }
}