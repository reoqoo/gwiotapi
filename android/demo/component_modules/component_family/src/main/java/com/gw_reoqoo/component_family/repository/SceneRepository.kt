package com.gw_reoqoo.component_family.repository

import com.gw_reoqoo.component_family.datasource.RemoteSceneDataSource
import com.gw_reoqoo.component_family.entrties.Scene
import javax.inject.Inject

/**
 * @Description: - 场景管理的数据IO中心
 * @Author: XIAOLEI
 * @Date: 2023/7/31
 */
class SceneRepository @Inject constructor(
    private val remoteSceneDataSource: RemoteSceneDataSource,
) {
    /**
     * 加载场景列表
     */
    suspend fun loadSceneList(userId: String): List<Scene> {
        return remoteSceneDataSource.loadSceneList(userId)
    }
}