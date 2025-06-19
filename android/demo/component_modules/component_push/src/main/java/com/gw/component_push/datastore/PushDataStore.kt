package com.gw.component_push.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("component_push")

/**
 * @Description: - Push模块的DataStore
 * @Author: XIAOLEI
 * @Date: 2023/10/18
 */
class PushDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_DEVICE_TOKEN = stringPreferencesKey("device_token")
    }

    /**
     * 保存Token
     */
    suspend fun saveToken(userId: String, token: String) {
        val dataStore = context.dataStore
        dataStore.edit {
            it[stringPreferencesKey(userId)] = token
        }
    }

    /**
     * 根据用户ID获取token
     */
    suspend fun getToken(userId: String): String? {
        val dataStore = context.dataStore
        return dataStore.data.map {
            it[stringPreferencesKey(userId)]
        }.first()
    }

    /**
     * 保存推送Token的状态
     */
    suspend fun saveTokenPushStatus(userId: String, token: String, success: Boolean) {
        val dataStore = context.dataStore
        dataStore.edit {
            it[booleanPreferencesKey("$userId-$token")] = success
        }
    }

    /**
     * 获取token是否推送成功的状态
     */
    suspend fun getTokenPushStatus(userId: String, token: String): Boolean? {
        val dataStore = context.dataStore
        return dataStore.data.map {
            it[booleanPreferencesKey("$userId-$token")]
        }.first()
    }

    /**
     * 根据用户ID，移除Token
     */
    suspend fun removeAll(userId: String) {
        val dataStore = context.dataStore
        dataStore.edit {
            it.remove(stringPreferencesKey(userId))
        }
    }

    /**
     * 保存Token
     */
    suspend fun saveToken(token: String) {
        val dataStore = context.dataStore
        dataStore.edit {
            it[KEY_DEVICE_TOKEN] = token
        }
    }

    /**
     * 获取token
     */
    suspend fun getToken(): String? {
        val dataStore = context.dataStore
        return dataStore.data.map {
            it[KEY_DEVICE_TOKEN]
        }.first()
    }
}