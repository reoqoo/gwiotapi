package com.gw.cp_config.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gw.cp_config.entity.DevConfigEntity
import com.gw.cp_config.entity.SceneEntity
import com.gw_reoqoo.lib_datastore.DataStoreUtils
import com.gwell.loglibs.GwellLogUtils
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.lang.reflect.Type
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore("global_config")

class ConfigDataStore @Inject constructor(@ApplicationContext context: Context) :
    IConfigDataStoreApi {

    companion object {

        private const val TAG = "ConfigDataStore"

        /**
         * 配置信息更新时间
         */
        private const val CONFIG_UPDATE_TIME = "config_update_time"

        /**
         * 支持短信推送的国家码列表
         */
        private const val COUNTRY_CODE_LIST = "country_code_list"

        /**
         * 产品配置信息版本
         */
        private const val PRODUCT_CONF_VER = "product_conf_ver"

        /**
         * 场景列表
         */
        private const val SCENE_NAME = "scene_name"

        /**
         * 产品配置列表
         */
        private const val PRODUCT_PID = "product_pid"

        /**
         * 产品配置权限模式
         */
        private const val PERMISSION_MODE = "permission_mode"

    }

    private val dataStore = context.dataStore

    override fun getConfigUpdateTime(): Long? {
        return DataStoreUtils.getData(dataStore, CONFIG_UPDATE_TIME, 0L)
    }

    override fun setConfigUpdateTime(timestamp: Long) {
        DataStoreUtils.putData(dataStore, CONFIG_UPDATE_TIME, timestamp)
    }

    override fun getCountryCodeList(): List<String>? {
        val json = DataStoreUtils.getData(dataStore, COUNTRY_CODE_LIST, "")
        return if (json.isEmpty()) {
            null
        } else {
            val type: Type = object : TypeToken<ArrayList<String>>() {}.type
            Gson().fromJson(json, type)
        }
    }

    override fun setCountryCodeList(codes: List<String>) {
        val json = Gson().toJson(codes)
        DataStoreUtils.putData(dataStore, COUNTRY_CODE_LIST, json)
    }

    override fun getProductConfVer(): Int {
        return DataStoreUtils.getData(dataStore, PRODUCT_CONF_VER, 0)
    }

    override fun setProductConfVer(version: Int) {
        DataStoreUtils.putData(dataStore, PRODUCT_CONF_VER, version)
    }

    override fun getSceneName(): SceneEntity? {
        val json = DataStoreUtils.getData(dataStore, SCENE_NAME, "")
        return if (json.isEmpty()) {
            null
        } else {
            Gson().fromJson(json, SceneEntity::class.java)
        }
    }

    override fun setSceneName(entity: SceneEntity) {
        val json = Gson().toJson(entity)
        DataStoreUtils.putData(dataStore, SCENE_NAME, json)
    }

    override fun getProductPid(): Map<String, DevConfigEntity>? {
        val json = DataStoreUtils.getData(dataStore, PRODUCT_PID, "")
        GwellLogUtils.i(TAG, "json: $json")
        return if (json.isEmpty()) {
            null
        } else {
            val type: Type = object : TypeToken<Map<String, DevConfigEntity>>() {}.type
            Gson().fromJson(json, type)
        }
    }

    override fun setProductPid(map: Map<String, DevConfigEntity>) {
        val json = Gson().toJson(map)
        DataStoreUtils.putData(dataStore, PRODUCT_PID, json)
    }

    override fun getPermissionMode(): Int {
        return DataStoreUtils.getData(dataStore, PERMISSION_MODE, 0)
    }

    override fun setPermissionMode(permission: Int) {
        DataStoreUtils.putData(dataStore, PERMISSION_MODE, permission)
    }

}

interface IConfigDataStoreApi {

    /**
     * 获取配置更新时间
     *
     * @return Long? 时间戳
     */
    fun getConfigUpdateTime(): Long?

    /**
     * 设置配置更新时间
     *
     * @param timestamp Long 时间戳
     */
    fun setConfigUpdateTime(timestamp: Long)

    /**
     * 获取支持短信推送的国家码列表，如不支持则不能通过手机号码注册
     *
     * @return List<String>? 国家码列表
     */
    fun getCountryCodeList(): List<String>?

    /**
     * 保存支持短信推送的国家码列表，如不支持则不能通过手机号码注册
     *
     * @param codes List<String> 国家码列表
     */
    fun setCountryCodeList(codes: List<String>)

    /**
     * 获取配置信息的版本
     *
     * @return Int 版本
     */
    fun getProductConfVer(): Int

    /**
     * 保存配置信息的版本
     *
     * @param version Int 版本
     */
    fun setProductConfVer(version: Int)

    /**
     * 获取场景配置信息
     *
     * @return SceneEntity 场景数据
     */
    fun getSceneName(): SceneEntity?

    /**
     * 保存场景配置信息
     *
     * @param entity SceneEntity 场景数据
     */
    fun setSceneName(entity: SceneEntity)

    /**
     * 获取产品配置信息
     *
     * @return Map<String, DevConfigEntity> String: pid, DevConfigEntity: 产品配置信息
     */
    fun getProductPid(): Map<String, DevConfigEntity>?

    /**
     * 保存产品配置信息
     *
     * @param map MutableMap<String, DevConfigEntity> String: pid, DevConfigEntity: 产品配置信息
     */
    fun setProductPid(map: Map<String, DevConfigEntity>)

    /**
     * 获取配置权限模式
     *
     * @return Int 0：旧版本权限，1：新版本权限
     */
    fun getPermissionMode(): Int

    /**
     * 保存配置权限模式
     *
     * @param permission Int 0：旧版本权限，1：新版本权限
     */
    fun setPermissionMode(permission: Int)

}

@InstallIn(SingletonComponent::class)
@Module
abstract class IConfigDataApi {
    @Binds
    abstract fun api(impl: ConfigDataStore): IConfigDataStoreApi

}

