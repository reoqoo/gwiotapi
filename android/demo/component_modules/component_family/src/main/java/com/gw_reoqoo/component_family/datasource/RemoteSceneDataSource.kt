package com.gw_reoqoo.component_family.datasource

import com.gw_reoqoo.component_family.entrties.Scene
import javax.inject.Inject


/**
 * @Description: - 场景远程网络数据源
 * @Author: XIAOLEI
 * @Date: 2023/7/31
 */
class RemoteSceneDataSource @Inject constructor() {
    // 加载场景列表
    suspend fun loadSceneList(userId: String): List<Scene> {
        return emptyList()
    }
}