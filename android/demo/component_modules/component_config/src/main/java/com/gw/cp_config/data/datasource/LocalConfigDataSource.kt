package com.gw.cp_config.data.datasource

import android.app.Application
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gw.cp_config.data.datastore.IConfigDataStoreApi
import com.gw.cp_config.entity.ConfigJsonEntity
import com.gw.cp_config.entity.DevConfigEntity
import com.gw_reoqoo.lib_utils.file.StorageMgr
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.file.GwFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/6 20:20
 * Description: 本地获取配置信息的数据源
 */
class LocalConfigDataSource @Inject constructor(
    private val app: Application,
    private val api: IConfigDataStoreApi
) {

    companion object {
        private const val TAG = "LocalConfigDataSource"

        /**
         * 配置文件名
         */
        const val FILE_NAME_CONFIG_PID = "appConfig.json"
    }

    val pidConfigPath: String?
        get() {
            return StorageMgr.getInstance(app).appDocPath?.let {
                "$it/$FILE_NAME_CONFIG_PID"
            }
        }

    /**
     * 初始化产品配置文件，将json保存到DataStore中
     */
    suspend fun initConfig() {
        withContext(Dispatchers.IO) {
            val json = GwFileUtils.readFile2String(pidConfigPath)
            GwellLogUtils.i(TAG, "initConfig（） json===$json")
            val configJsonEntity: ConfigJsonEntity? =
                Gson().fromJson(json, object : TypeToken<ConfigJsonEntity>() {}.type)
            GwellLogUtils.i(TAG, "configJsonEntity $configJsonEntity")
            configJsonEntity?.run {
                if (productList.isNotEmpty()) {
                    api.setProductPid(productList)
                }
                api.setSceneName(sceneList)
                if (platform != null) {
                    api.setPermissionMode(platform.mode)
                }
            }
        }
    }

    /**
     * 初始化产品配置文件，将json保存到DataStore中
     */
    suspend fun initConfig(configJson: String) {
        withContext(Dispatchers.IO) {
            val configJsonEntity: ConfigJsonEntity? =
                Gson().fromJson(configJson, object : TypeToken<ConfigJsonEntity>() {}.type)
            GwellLogUtils.i(TAG, "configJsonEntity $configJsonEntity")
            configJsonEntity?.run {
                if (productList.isNotEmpty()) {
                    api.setProductPid(productList)
                }
                api.setSceneName(sceneList)
                if (platform != null) {
                    api.setPermissionMode(platform.mode)
                }
            }
        }
    }

    fun getPermissionMode(): Int {
        return api.getPermissionMode()
    }

    /**
     * 通过Pid来获取产品的详细信息
     *
     * @param pid String 产品Pid
     * @return DevConfigEntity? 详细信息
     */
    fun getProductPid(pid: String): DevConfigEntity? {
        return api.getProductPid()?.get(pid) ?: kotlin.run {
            try {
                val json = GwFileUtils.readFile2String(pidConfigPath)
                val configJsonEntity = Gson().fromJson(json, ConfigJsonEntity::class.java)
                val productPid = configJsonEntity.productList[pid]
                GwellLogUtils.i(TAG, "productPid: $productPid")
                return productPid
            } catch (e: Exception) {
                GwellLogUtils.e(TAG, "getDevConfigByPid error: e ${e.message}")
                return null
            }
        }
    }

}