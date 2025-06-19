package com.gw_reoqoo.component_device_share.data.rspository

import com.gw_reoqoo.component_device_share.data.datasource.FamilyModelDeviceDatasource
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import javax.inject.Inject

/**
 * @Description: - 设备的Repository
 * @Author: XIAOLEI
 * @Date: 2023/8/9
 */
class DeviceRepository @Inject constructor(
    private val datasource: FamilyModelDeviceDatasource
) {
    /**
     * 根据设备ID获取设备列表
     */
    fun getDeviceList(userId: String): List<IDevice> {
        return datasource.getDeviceList(userId)
    }
}