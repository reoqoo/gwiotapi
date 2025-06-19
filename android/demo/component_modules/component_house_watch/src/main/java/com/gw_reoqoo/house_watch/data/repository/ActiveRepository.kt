package com.gw_reoqoo.house_watch.data.repository

import com.gw_reoqoo.house_watch.data.data_source.RemoteActiveDataSource
import com.gw_reoqoo.lib_http.RespResult
import com.gw_reoqoo.lib_http.entities.ActiveList
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @Description: - 设备活动数据中心
 * @Author: XIAOLEI
 * @Date: 2023/8/22
 *
 * @param remoteDataSource 远程数据源
 * @param fakeDataSource 假数据的数据源
 */
class ActiveRepository @Inject constructor(
    private val remoteDataSource: RemoteActiveDataSource,
) {
    /**
     * 获取用户事件列表
     * @param devIds 设备ID列表
     * @param startTime 事件开始时间(单位秒)
     * @param endTime 事件结束时间(单位秒)
     * @param almTypeMasks 时间类型过滤列表
     */
    suspend fun getActiveList(
        devIds: List<String>,
        startTime: Long,
        endTime: Long,
        almTypeMasks: List<Long>
    ): RespResult<ActiveList> {
        return remoteDataSource.getActiveList(devIds, startTime, endTime, almTypeMasks)
    }
}