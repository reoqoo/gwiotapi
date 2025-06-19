package com.gw_reoqoo.cp_account.api

import com.gw_reoqoo.cp_account.api.kapi.IAccountMgrApi
import com.gw_reoqoo.cp_account.api.impl.AccountApiImpl
import com.gw_reoqoo.cp_account.api.impl.AccountMgrImpl
import com.gw_reoqoo.cp_account.api.impl.InterfaceSignApiImpl
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.cp_account.api.kapi.IInterfaceSignApi
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
    abstract fun getAccountApi(apiImpl: AccountApiImpl): IAccountApi

    @Singleton
    @Binds
    abstract fun getAccountMgrApi(impl: AccountMgrImpl): IAccountMgrApi

    @Singleton
    @Binds
    abstract fun getInterfaceSignApi(impl: InterfaceSignApiImpl): IInterfaceSignApi

}
