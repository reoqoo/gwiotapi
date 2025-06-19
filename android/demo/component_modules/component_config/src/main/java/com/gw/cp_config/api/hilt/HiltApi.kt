package com.gw.cp_config.api.hilt

import com.gw.cp_config.api.IAppConfigApi
import com.gw.cp_config.api.IAppParamApi
import com.gw.cp_config.impl.AppConfigApiImpl
import com.gw.cp_config.impl.AppParamApiImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * @Description: - 向Hilt注册依赖注入
 * @Author: XIAOLEI
 * @Date: 2023/8/4
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class HiltApi {

    @Binds
    abstract fun getConfigApi(apiImpl: AppConfigApiImpl): IAppConfigApi

    @Binds
    abstract fun getAppParamApi(apiImpl: AppParamApiImpl): IAppParamApi

}