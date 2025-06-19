package com.gw_reoqoo.component_device_share.data.datasource

import androidx.lifecycle.LiveData
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/3 11:47
 * Description: LocalShareDataSource
 */
class LocalShareDataSource @Inject constructor(
    private val familyModeApi: FamilyModeApi,
) {
    
    /**
     * 获取本地设备列表
     *
     * @param userId String  用户ID
     * @return List<IDevice> 设备列表
     */
    fun watchDevices(userId: String): LiveData<List<IDevice>> {
        return familyModeApi.watchDeviceList(userId)
    }
}