package com.gw_reoqoo.cp_account.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.gw_reoqoo.lib_datastore.DataStoreUtils
import com.gw_reoqoo.lib_http.entities.DistrictEntity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/7/25 10:39
 * Description: AccountDataStore
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "account")

class AccountDataStore @Inject constructor(@ApplicationContext context: Context) :
    AccountDataStoreApi {

    companion object {

        /**
         * 支持手机注册的地区码列表
         */
        private const val DISTRICT_CODE_LIST = "district_code_list"

        /**
         * 用户登录时选择的地区信息
         */
        private const val USER_DISTRICT_CODE = "user_district_code"

        /**
         * 隐私协议弹框
         */
        private const val NEED_SHOW_PROTOCOL = "need_show_protocol"

        /**
         * token刷新时间
         */
        private const val TOKEN_REFRESH_TIME = "token_refresh_time"
    }

    private val dataStore = context.dataStore

    override fun getUserDistrictEntity(): DistrictEntity? {
        val json = DataStoreUtils.getData(dataStore, USER_DISTRICT_CODE, "")
        return if (json.isEmpty()) {
            null
        } else {
            Gson().fromJson(json, DistrictEntity::class.java)
        }
    }

    override fun setUserDistrictEntity(entity: DistrictEntity) {
        val json = Gson().toJson(entity)
        DataStoreUtils.putData(dataStore, USER_DISTRICT_CODE, json)
    }

    override fun getNeedShowProtocol(): Boolean {
        return DataStoreUtils.getData(dataStore, NEED_SHOW_PROTOCOL, true)
    }

    override fun setNeedShowProtocol(isShow: Boolean) {
        DataStoreUtils.putData(dataStore, NEED_SHOW_PROTOCOL, isShow)
    }

    /**
     * 获取token刷新时间
     *
     * @return Long 刷新时间
     */
    override fun getTokenRefreshTime(): Long {
        return DataStoreUtils.getData(dataStore, TOKEN_REFRESH_TIME, 0L)
    }

    /**
     * 保存token刷新时间
     *
     * @param time Long 刷新时间
     */
    override fun setTokenRefreshTime(time: Long) {
        DataStoreUtils.putData(dataStore, TOKEN_REFRESH_TIME, time)
    }
}

interface AccountDataStoreApi {

    /**
     * 获取用户选择的地区信息
     *
     * @return DistrictEntity? 地区信息
     */
    fun getUserDistrictEntity(): DistrictEntity?

    /**
     * 保存用户选择的地区信息
     *
     * @param entity DistrictEntity
     */
    fun setUserDistrictEntity(entity: DistrictEntity)

    /**
     * 获取隐私弹框协议是否需要展示
     *
     * @return Boolean true：需要， false：不需要
     */
    fun getNeedShowProtocol(): Boolean

    /**
     * 设置隐私弹框协议是否需要展示
     *
     * @return Boolean true：需要， false：不需要
     */
    fun setNeedShowProtocol(isShow: Boolean)

    /**
     * 获取token刷新时间
     *
     * @return Long 刷新时间
     */
    fun getTokenRefreshTime(): Long

    /**
     * 保存token刷新时间
     *
     * @param time Long 刷新时间
     */
    fun setTokenRefreshTime(time: Long)

}

@InstallIn(SingletonComponent::class)
@Module
abstract class IAccountDataApi {
    @Binds
    abstract fun api(impl: AccountDataStore): AccountDataStoreApi

}