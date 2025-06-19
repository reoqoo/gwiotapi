package com.gw_reoqoo.component_family.data_store

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.gw_reoqoo.component_family.api.interfaces.IGuideDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("guide_data")

/**
 * @Description: - 引导页的数据存储
 * @Author: XIAOLEI
 * @Date: 2023/9/11
 */
@Singleton
class GuideDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : IGuideDataStore {
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
     * 右上角添加按钮的引导操作结束标志
     */
    override suspend fun getAddBtnGuide(): Boolean {
        val key = booleanPreferencesKey("ADD_BTN_GUIDE")
        return get(key) ?: false
    }

    /**
     * 右上角添加按钮的引导操作结束标志
     */
    override suspend fun setAddBtnGuide(value: Boolean) {
        val key = booleanPreferencesKey("ADD_BTN_GUIDE")
        set(key, value)
    }

    /**
     * 第一个设备的引导操作
     */
    override suspend fun getFirstDeviceGuide(): Boolean {
        val key = booleanPreferencesKey("FIRST_DEVICE_GUIDE")
        return get(key) ?: false
    }

    /**
     * 第一个设备的引导操作
     */
    override suspend fun setFirstDeviceGuide(value: Boolean) {
        val key = booleanPreferencesKey("FIRST_DEVICE_GUIDE")
        set(key, value)
    }

    /**
     * 获取设备卡片引导
     */
    override suspend fun getVideoCardGuide(): Boolean {
        val key = booleanPreferencesKey("VIDEO_CARD_GUIDE")
        return get(key) ?: false
    }

    /**
     * 设置设备卡片引导
     */
    override suspend fun setVideoCardGuide(value: Boolean) {
        val key = booleanPreferencesKey("VIDEO_CARD_GUIDE")
        set(key, value)
    }
}
