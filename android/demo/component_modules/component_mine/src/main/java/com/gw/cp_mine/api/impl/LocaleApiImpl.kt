package com.gw.cp_mine.api.impl

import android.app.Activity
import android.app.Application
import com.gw.cp_mine.api.kapi.ILocaleApi
import com.gw.cp_mine.data_store.LocaleDataStoreImpl
import com.gw.cp_mine.kits.LanguageMgr
import com.gwell.loglibs.GwellLogUtils
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/11/17 9:54
 * Description: LocaleApiImpl
 */
@Singleton
class LocaleApiImpl @Inject constructor(
    private val app: Application,
    private val dataStoreImpl: LocaleDataStoreImpl,
    private val languageMgr: LanguageMgr
) : ILocaleApi {

    companion object {
        private const val TAG = "LocaleApiImpl"
    }

    /**
     * 初始化app语言
     */
    override fun initAppLanguage(activity: Activity?) {
        val language = languageMgr.getAppLanguage()
        val newLocale = language.locale
        if (newLocale != null) {
            Locale.setDefault(newLocale)
            GwellLogUtils.i(
                TAG,
                "initAppLanguage: language = ${newLocale.language}, country = ${newLocale.country}"
            )
            if (activity != null) {
                languageMgr.applyAppLanguage(activity)
            }
        }
    }

    /**
     * 获取当前语言
     *
     * @return String 当前语言
     */
    override fun getCurrentLanguage(): String {
        val locale = languageMgr.getAppLanguage().locale ?: app.resources.configuration.locale
        locale.country.let {
            if (it.isNullOrEmpty()) {
                return locale.language
            } else {
                return buildString {
                    append(locale.language)
                    append("_")
                    append(locale.country)
                }
            }
        }

    }

    override fun getCurrentLanguageLocale(): Locale {
        return languageMgr.getAppLanguage().locale ?: app.resources.configuration.locale
    }

    /**
     * 获取当前国家
     *
     * @return String 当前国家
     */
    override fun getCurrentCountry(): String {
        return dataStoreImpl.getCurrentCountry()
    }

    /**
     * 设置当前国家
     *
     * @param country String 国家
     */
    override fun setCurrentCountry(country: String) {
        dataStoreImpl.setCurrentCountry(country)
    }
}