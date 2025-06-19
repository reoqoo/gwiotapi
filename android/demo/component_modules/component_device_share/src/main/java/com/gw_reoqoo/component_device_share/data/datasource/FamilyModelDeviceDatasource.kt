package com.gw_reoqoo.component_device_share.data.datasource

import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import javax.inject.Inject

/**
 * @Description: - 设备数据源来自模块共享的Datasource
 * @Author: XIAOLEI
 * @Date: 2023/8/9
 */
class FamilyModelDeviceDatasource @Inject constructor(
    private val familyModeApi: FamilyModeApi
) {
    /**
     * 根据设备ID获取设备列表
     */
    fun getDeviceList(userId: String): List<IDevice> {
        return familyModeApi.getDeviceList(userId)
    }
}