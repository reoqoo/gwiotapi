package com.gw_reoqoo.component_device_share.data.datasource

import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/3 11:48
 * Description: RemoteShareDataSource
 */
class RemoteShareDataSource @Inject constructor(
    private val familyModeApi: FamilyModeApi
) {

    /**
     * 获取远程设备分享列表
     *
     * @param userId String 用户ID
     * @return List<IDevice> 设备列表
     */
    suspend fun getShareDev(userId: String): List<IDevice> {
        familyModeApi.refreshDevice()
        return familyModeApi.getDeviceList(userId)
    }

}