package com.gw.component_push.api

import com.gw.component_push.api.impl.AlarmEventApiImpl
import com.gw.component_push.api.impl.PushApiImpl
import com.gw.component_push.api.interfaces.IAlarmEventApi
import com.gw.component_push.api.interfaces.IPushApi
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/10/13
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class HiltApi {
    @Singleton
    @Binds
    abstract fun getFamilyModelApi(impl: PushApiImpl): IPushApi

    @Singleton
    @Binds
    abstract fun getAlarmEventApi(impl: AlarmEventApiImpl): IAlarmEventApi
}