package com.gw_reoqoo.component_device_share.data.rspository

import com.gw_reoqoo.component_device_share.data.datasource.RemoteDevShareDatasource
import com.gw_reoqoo.component_device_share.data.datasource.RemoteVisitorDataSource
import com.gw_reoqoo.lib_http.entities.OwnerInfo
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/4 0:05
 * Description: 设备访客管理repository
 */
class DevVisitorRepository @Inject constructor(
    private val remoteDevShareDataSource: RemoteDevShareDatasource,
    private val remoteVisitorDataSource: RemoteVisitorDataSource
) {
    
    /**
     * 获取设备所有的访客
     *
     * @param devId String 设备ID
     * @return ListGuestContent? 访客列表
     */
    fun getDevVisitors(devId: String) = remoteDevShareDataSource.listGuest(devId)
    
    /**
     * 设备停止分享
     *
     * @param devId String 设备ID
     */
    fun delAllGuest(devId: String): Flow<HttpAction<Any>> {
        return delDevVisitor(devId, "")
    }
    
    /**
     * 设备删除单个访客
     *
     * @param devId String 设备ID
     * @param visitorId String 访客ID ,为空则表示删除所有访客
     * @return Boolean? 是否删除成功
     */
    fun delDevVisitor(
        devId: String, visitorId: String
    ): Flow<HttpAction<Any>> = remoteVisitorDataSource.deleteGuest(deviceId = devId, visitorId)
    
    
    /**
     * 访客查询设备主人信息
     * @param deviceId 设备ID
     */
    fun loadOwnerInfo(deviceId: String): Flow<HttpAction<OwnerInfo>> {
        return remoteDevShareDataSource.loadOwnerInfo(deviceId)
    }
    
    /**
     * 访客-删除被分享的设备
     */
    fun cancelShare(deviceId: String): Flow<HttpAction<Any>> {
        return remoteDevShareDataSource.cancelShare(deviceId)
    }
}