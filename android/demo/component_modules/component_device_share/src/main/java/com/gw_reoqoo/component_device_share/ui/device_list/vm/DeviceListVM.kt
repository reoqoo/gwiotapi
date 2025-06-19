package com.gw_reoqoo.component_device_share.ui.device_list.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gw_reoqoo.component_device_share.data.rspository.DevShareRepository
import com.gw_reoqoo.component_device_share.data.rspository.DeviceRepository
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.entities.ListGuestContent
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @Description: - 设备列表的VM
 * @Author: XIAOLEI
 * @Date: 2023/8/9
 */
@HiltViewModel
class DeviceListVM @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val devShareRepository: DevShareRepository
) : ABaseVM() {
    /**
     * 设备列表
     */
    private val _deviceList = MutableLiveData<List<IDevice>>()
    val deviceList: LiveData<List<IDevice>> get() = _deviceList

    /**
     * 加载设备列表
     */
    fun loadDeviceList(userId: String) {
        val devList = deviceRepository.getDeviceList(userId).filter { it.isMaster }
        _deviceList.postValue(devList)
    }

    /**
     * 校验此设备分享数量已满
     */
    fun listGuest(deviceId: String): Flow<HttpAction<ListGuestContent>> {
        return devShareRepository.listGuest(deviceId)
    }
}