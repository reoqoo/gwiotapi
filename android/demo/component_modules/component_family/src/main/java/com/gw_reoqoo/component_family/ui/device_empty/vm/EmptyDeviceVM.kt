package com.gw_reoqoo.component_family.ui.device_empty.vm

import com.gw_reoqoo.component_family.repository.DeviceRepository
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_room.device.DeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 */
@HiltViewModel
class EmptyDeviceVM @Inject constructor(
    private val deviceRepository: DeviceRepository,
) : ABaseVM() {
    /**
     * 添加假的设备数据，供测试使用
     */
    fun addFakeDevice(userId: String) {
        val fakeDevices = List(50) {
            val uuid = UUID.randomUUID().toString()
            DeviceInfo(
                deviceId = uuid,
                userId = userId,
                remarkName = "新增设备",
                relation = 1,
                permission = 0,
                modifyTime = ""
            )
        }
        deviceRepository.addDevices(fakeDevices)
    }
}