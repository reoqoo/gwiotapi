package com.gw_reoqoo.component_device_share.data.rspository

import androidx.lifecycle.LiveData
import com.gw_reoqoo.component_device_share.data.datasource.LocalShareDataSource
import com.gw_reoqoo.component_device_share.data.datasource.RemoteShareDataSource
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/3 11:46
 * Description: DevShareRepository
 */
class ShareMgrRepository @Inject constructor(
    private val localShareDataSource: LocalShareDataSource,
    private val remoteShareDataSource: RemoteShareDataSource
) {

    /**
     * 获取本地的设备列表
     *
     * @return List<IDevice>?
     */
    fun watchDevices(userId:String): LiveData<List<IDevice>> {
        return localShareDataSource.watchDevices(userId)
    }
}