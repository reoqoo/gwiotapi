package com.gw.reoqoo

import com.gw.reoqoo.app.api.IReoqooSdkService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/10/8 20:51
 * Description: Hilt
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HiltApi {

    @Binds
    @Singleton
    abstract fun provideService(sdk: ReoqooSdkServiceImpl): IReoqooSdkService

}