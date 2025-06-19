package com.gw_reoqoo.house_watch.data.data_store

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gw_reoqoo.house_watch.entities.DevicePack
import com.gw_reoqoo.house_watch.entities.ViewTypeModel
import com.gw_reoqoo.lib_http.jsonToEntity
import com.gw_reoqoo.lib_http.toJson
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


private val Context.dataStore by preferencesDataStore("video_data")

/**
 * @Description: - 看家监控的数据存储
 * @Author: yanzheng@gwell.cc
 * @Time: 2023/10/11 9:47
 */
@Singleton
class VideoDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "VideoDataStore"

        /**
         * 视频显示模式（单屏还是多屏）
         */
        private const val KEY_VIDEO_SHOW_TYPE = "key_video_show_type"

        /**
         * 设备列表 排序
         */
        private const val KEY_DEVICES_SORT = "key_devices_list"
    }

    private val dataStore by lazy { context.dataStore }

    private suspend fun <T> get(key: Preferences.Key<T>): T? {
        return dataStore.data.map { preference ->
            preference[key]
        }.firstOrNull()
    }

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit {
            it[key] = value
        }
    }

    /**
     * 设置视频显示模式
     *
     * @param showType ViewTypeModel
     */
    suspend fun setVideoShowType(showType: ViewTypeModel) {
        val key = intPreferencesKey(KEY_VIDEO_SHOW_TYPE)
        set(key, showType.viewType)
    }

    /**
     * 获取视频显示模式
     */
    suspend fun getVideoShowType(): ViewTypeModel {
        val key = intPreferencesKey(KEY_VIDEO_SHOW_TYPE)
        return when (get(key)) {
            ViewTypeModel.SINGLE.viewType -> ViewTypeModel.SINGLE
            ViewTypeModel.MULTI.viewType -> ViewTypeModel.MULTI
            else -> ViewTypeModel.SINGLE
        }
    }

    /**
     * 设置本地的设备列表排序配置
     *
     * @param devices List<DevicePack> 设备列表排序配置
     */
    suspend fun setDevSortConfig(devices: List<DevicePack>) {
        val key = stringPreferencesKey(KEY_DEVICES_SORT)
        val jsonStr = devices.map {
            mapOf<String, Any>("deviceId" to it.deviceId, "offView" to it.offView)
        }.toJson()
        GwellLogUtils.i(TAG, "setDevSortConfig: jsonStr=$jsonStr")
        if (jsonStr.isNullOrEmpty()) {
            return
        }
        set(key, jsonStr)
    }

    /**
     * 获取本地的设备列表排序配置
     *
     * @return List<DevicePack>? 设备列表排序配置
     */
    suspend fun getDevSortConfig(): List<Map<String, Any>>? {
        val key = stringPreferencesKey(KEY_DEVICES_SORT)
        val jsonStr = get(key)
        return if (jsonStr.isNullOrEmpty()) {
            null
        } else {
            GwellLogUtils.i(TAG, "getDevSortConfig: jsonStr=$jsonStr")
            jsonStr.jsonToEntity<List<Map<String, Any>>>()
        }
    }

}
