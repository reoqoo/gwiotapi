package com.gw_reoqoo.house_watch.receivers.api

import com.gw_reoqoo.house_watch.receivers.api.impl.NetworkStatus
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/10/25
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class HiltApi {
    @Singleton
    @Binds
    abstract fun getNetworkStatusApi(impl: NetworkStatus): INetworkStatusApi
}