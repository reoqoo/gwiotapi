package com.gw.cp_config.data.repository

import android.content.Context
import com.gw.cp_config.data.datasource.LocalConfigDataSource
import com.gw.cp_config.data.datasource.RemoteConfigDataSource
import com.gw_reoqoo.lib_http.RespResult
import com.gw_reoqoo.lib_http.entities.AppConfigEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/6 20:23
 * Description: GlobalRepository
 */
@Singleton
class ConfigRepository @Inject constructor(
    private val remoteDataSource: RemoteConfigDataSource,
    private val localDataSource: LocalConfigDataSource
) {

    companion object {
        private const val TAG = "ConfigRepository"
    }

    /**
     * 获取 远程服务器的配置
     *
     * @return RespResult<AppConfigEntity>
     */
    suspend fun getGlobalConfig(isLogin: Boolean): RespResult<AppConfigEntity> = remoteDataSource.getAppConfig(isLogin)

    /**
     * 下载配置文件
     *
     * @param url String url地址
     * @param filePath String 文件保存路径
     * @return Boolean 是否成功
     */
    suspend fun downloadFile(url: String, filePath: String) =
        remoteDataSource.downloadConfig(url, filePath)

    /**
     * 将配置文件保存到DataStore
     */
    suspend fun initConfigFile() = localDataSource.initConfig()

    /**
     * 将配置文件保存到DataStore
     */
    suspend fun initConfigFile(initConfig: String) = localDataSource.initConfig(initConfig)

    /**
     * 获取配置文件的地址
     *
     * @return String?
     */
    fun getConfigFilePath(): String? = localDataSource.pidConfigPath

    /**
     * 通过Pid获取对应的产品信息
     *
     * @param pid String 产品ID
     * @return DevConfigEntity? 产品信息
     */
    fun getProductPid(pid: String) = localDataSource.getProductPid(pid)

}