package com.gw.cp_mine.api

import com.gw.cp_mine.api.impl.LocaleApiImpl
import com.gw.cp_mine.api.kapi.ILocaleApi
import com.gw.cp_mine.api.kapi.IMineModuleApi
import com.gw.cp_mine.api_impl.MineModuleImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/21 22:48
 * Description: HiltApi
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HiltApi {

    @Singleton
    @Binds
    abstract fun getMineApi(impl: MineModuleImpl): IMineModuleApi

    @Singleton
    @Binds
    abstract fun localeApi(impl: LocaleApiImpl): ILocaleApi

}