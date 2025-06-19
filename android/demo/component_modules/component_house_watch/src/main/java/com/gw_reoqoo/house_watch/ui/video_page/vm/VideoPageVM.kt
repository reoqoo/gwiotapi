package com.gw_reoqoo.house_watch.ui.video_page.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.gw.component_plugin_service.api.IPluginManager
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.component_iotapi_plugin.opt.api.IGWIotOpt
import com.gw_reoqoo.component_iotapi_plugin.opt.api.PluginSolution
import com.gw_reoqoo.house_watch.receivers.api.INetworkStatusApi
import com.gw_reoqoo.house_watch.receivers.api.StatusDiff
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/9/4
 */
@HiltViewModel
class VideoPageVM @Inject constructor(
    private val networkStatusApi: INetworkStatusApi,
    private val familyModeApi: FamilyModeApi,
    private var pluginManager: IPluginManager,
    private var igwIotOpt: IGWIotOpt,
) : ABaseVM() {
    val networkStatus: LiveData<StatusDiff> get() = networkStatusApi.networkStatus

    /**
     * 打开设备监控页
     */
    fun openDeviceMonitor(device: IDevice) {
        val pid = device.productId ?: return
        if (familyModeApi.isReoqooDevice(pid)) {
            pluginManager.startMonitorActivity(device.deviceId)
        } else {
            viewModelScope.launch {
                igwIotOpt.openHome(device.deviceId)
            }
        }
    }
}