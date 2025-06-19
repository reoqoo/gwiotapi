package com.gw.cp_config.impl

import android.app.Application
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw.cp_config.api.IAppConfigApi
import com.gw.cp_config.api.ProductImgType
import com.gw.cp_config.data.datastore.IConfigDataStoreApi
import com.gw.cp_config.data.repository.ConfigRepository
import com.gw.cp_config.entity.DevConfigEntity
import com.gwell.loglibs.GwellLogUtils
import com.therouter.router.getStringFromAssets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/6 20:18
 * Description: GlobalApiImpl
 */
class AppConfigApiImpl @Inject constructor(
    private val accountApi: IAccountApi,
) : IAppConfigApi {

    companion object {
        private const val TAG = "AppConfigApiImpl"
    }

    private val scope = MainScope()

    /**
     * 是否使用本地配置（用于调试使用）
     */
    private val userLocalConfig = false

    @Inject
    lateinit var app: Application

    @Inject
    lateinit var repository: ConfigRepository

    @Inject
    lateinit var api: IConfigDataStoreApi

    override fun uploadConfig() {
        val currentTime = System.currentTimeMillis()
        scope.launch(Dispatchers.IO) {
            if (userLocalConfig) {
                initConfigFromLocal()
                return@launch
            }
            repository.getGlobalConfig(accountApi.isSyncLogin())
                .onSuccess {
                    api.setConfigUpdateTime(currentTime)
                    GwellLogUtils.i(TAG, "uploadGlobal onSuccess: ${this.toString()}")
                    if (api.getProductConfVer() == 0) {
                        initConfigFromLocal()
                    }
                    this?.run {
                        countryCodeList?.let {
                            GwellLogUtils.i(TAG, "countryCodeList $it")
                            api.setCountryCodeList(it)
                        } ?: GwellLogUtils.e(TAG, "uploadGlobal: countryCodeList is null")
                        GwellLogUtils.i(TAG, "getProductConfVer ${api.getProductConfVer()} productConfVer==$productConfVer")
                        val configFile = File(repository.getConfigFilePath() ?: "")
                        if (api.getProductConfVer() >= productConfVer && configFile.isFile && configFile.exists()) {
                            repository.initConfigFile()
                            return@run
                        }
                        productConfUrl?.let { _url ->
                            repository.getConfigFilePath()?.let { _filePath ->
                                val result = repository.downloadFile(_url, _filePath)
                                GwellLogUtils.i(TAG, "FileDownloadMgr: download $result")
                                if (result) {
                                    api.setProductConfVer(productConfVer)
                                    repository.initConfigFile()
                                }
                            }
                        } ?: GwellLogUtils.e(TAG, "uploadGlobal: productConfUrl is null")
                    } ?: GwellLogUtils.e(TAG, "uploadGlobal: GlobalEntity is null")
                }

                .onServerError { code, msg ->
                    GwellLogUtils.e(TAG, "uploadGlobal onServerError: code $code, msg $msg")
                    if (api.getProductConfVer() == 0) {
                        // 如果配置文件的版本为0，则认为没有从服务器获取到过配置，则使用本地配置文件
                        initConfigFromLocal()
                    }
                }

                .onLocalError {
                    GwellLogUtils.e(TAG, "uploadGlobal onLocalError: e ${it.message}")
                    if (api.getProductConfVer() == 0) {
                        // 如果配置文件的版本为0，则认为没有从服务器获取到过配置，则使用本地配置文件
                        initConfigFromLocal()
                    }
                }
        }
    }

    override fun getCountryCodeList(): List<String> {
        val districtCodeList = api.getCountryCodeList()
        return if (districtCodeList.isNullOrEmpty()) {
            listOf("86")
        } else {
            districtCodeList
        }
    }

    override fun getSystemScenes(language: String, country: String?): List<String>? {
        GwellLogUtils.i(TAG, "language $language")
        return when (language) {
            "zh" -> {
                if (country == "HK" ||
                    country == "TW" ||
                    country == "MO"
                ) {
                    api.getSceneName()?.hant
                } else {
                    api.getSceneName()?.hans
                }
            }

            "vi" -> api.getSceneName()?.vi
            "th" -> api.getSceneName()?.th
            "ko" -> api.getSceneName()?.ko
            "ja" -> api.getSceneName()?.ja
            "in" -> api.getSceneName()?.android_in
            "ms" -> api.getSceneName()?.ms
            else -> api.getSceneName()?.en
        }
    }

    /**
     * 获取所有设备配置信息
     *
     * @return Map<String, DevConfigEntity>?
     */
    override fun getDevConfig(): Map<String, DevConfigEntity>? = api.getProductPid()

    /**
     * 通过设备pid来获取设备配置信息
     *
     * @param pid String        产品Pid
     * @return DevConfigEntity? 设备配置信息
     */
    override fun getDevConfigByPid(pid: String): DevConfigEntity? {
        val entity = repository.getProductPid(pid)
        GwellLogUtils.i(TAG, "entity: $entity")
        return entity
    }

    /**
     * 根据产品ID获取产品图片
     *
     * @param pid String?             产品Pid
     * @param imgType ProductImgType  产品图片类型ProductImgType
     * @return String? ⾸⻚、设备列表⻚、分享设备⻚共⽤图
     * @return String? 配置Wi-Fi⻚、设备控制⻚、共享管理⻚共⽤图
     */
    override fun getProductImgUrl(pid: String?, imgType: ProductImgType): String? {
        return getDevConfig()?.get(pid)?.productImageURL_A
    }

    /**
     * 根据产品ID获取产品图片
     * @return String? 配置Wi-Fi⻚、设备控制⻚、共享管理⻚共⽤图
     */
    override fun getProductImgUrl_C(pid: String?, imgType: ProductImgType): String? {
        return getDevConfig()?.get(pid)?.productImageURL_C
    }

    /**
     * 根据产品ID获取产品图片
     * @return String?  设备连接⻚、设备分享弹窗
     */
    override fun getProductImgUrl_D(pid: String?, imgType: ProductImgType): String? {
        return getDevConfig()?.get(pid)?.productImageURL_D
    }

    /**
     * 根据产品id获取产品名称（自动国际化）
     */
    override fun getProductName(pid: String): String? {
        val entity = getDevConfigByPid(pid)
        val appLocal = app.resources.configuration.locale
        // zh
        val language = appLocal.language
        // hk tw hans
        val country = appLocal.country
        return entity?.productName
    }

    override fun getPermissionMode(): Int {
        return api.getPermissionMode()
    }

    override fun setPermissionMode(mode: Int) {
        api.setPermissionMode(mode)
    }

    /**
     * 初始化本地获取配置文件
     */
    private suspend fun initConfigFromLocal() {
        val configJson = getStringFromAssets(app.applicationContext, "appConfig.json")
        repository.initConfigFile(configJson)
    }

}