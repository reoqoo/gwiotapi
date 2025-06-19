package com.gw_reoqoo.component_family.api

import com.gw_reoqoo.component_family.api.impl.FamilyModelImpl
import com.gw_reoqoo.component_family.api.impl.LocalDeviceImpl
import com.gw_reoqoo.component_family.api.impl.ShareDeviceImpl
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IGuideDataStore
import com.gw_reoqoo.component_family.api.interfaces.ILocalDeviceApi
import com.gw_reoqoo.component_family.api.interfaces.IShareDeviceApi
import com.gw_reoqoo.component_family.data_store.GuideDataStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @Description: - 注册注入FamilyModule的实现类
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class HiltApi {
    @Singleton
    @Binds
    abstract fun getFamilyModelApi(impl: FamilyModelImpl): FamilyModeApi

    @Singleton
    @Binds
    abstract fun getGuideDataStore(impl: GuideDataStore): IGuideDataStore

    @Singleton
    @Binds
    abstract fun getScanShareDeviceApi(impl: ShareDeviceImpl): IShareDeviceApi

    @Singleton
    @Binds
    abstract fun getLocalDeviceApi(impl: LocalDeviceImpl): ILocalDeviceApi
}
