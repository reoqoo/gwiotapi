package com.gw_reoqoo.component_device_share.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.reflect.TypeToken
import com.gw_reoqoo.lib_http.entities.ShareContent
import com.gw_reoqoo.lib_http.jsonToEntity
import com.gw_reoqoo.lib_http.toJson
import com.gwell.loglibs.GwellLogUtils
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("component_device_share")

@Singleton
class DeviceShareDatStore @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        private const val TAG = "DeviceShareDataStore"
    }

    private val dataStore by lazy { context.dataStore }

    /**
     * 获取最后一次获取分享二维码的成功的数据
     */
    suspend fun getLastShareQRCode(userId: String): ShareContent? {
        val key = stringPreferencesKey("last_share_qrcode:$userId")
        val json = dataStore.data.map {
            it[key]
        }.firstOrNull() ?: return null
        return json.jsonToEntity()
    }

    /**
     * 保存最后一次获取分享二维码的成功的数据
     */
    suspend fun saveLastShareQRCode(userId: String, content: ShareContent) {
        val json = content.toJson()
        GwellLogUtils.i(TAG, "saveLastShareQRCode($userId,$json)")
        val key = stringPreferencesKey("last_share_qrcode:$userId")
        dataStore.edit {
            if (json == null) {
                it.remove(key)
            } else {
                it[key] = json
            }
        }
    }
}
