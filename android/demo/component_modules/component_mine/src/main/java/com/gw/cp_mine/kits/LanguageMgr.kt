package com.gw.cp_mine.kits

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.gw.cp_mine.data_store.LocaleDataStoreImpl
import com.gw.cp_mine.entity.Language
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 多语言工具类
 * Created by Fitem on 2020/03/20.
 */
@Singleton
class LanguageMgr @Inject constructor(
    private val app: Application,
    private val dataStoreImpl: LocaleDataStoreImpl,
) {

    companion object {

        private const val TAG = "LanguageUtils"

        private const val SYSTEM_LANGUAGE_TGA = "systemLanguageTag"
    }

    /**
     * 获取系统Local
     */
    private val systemLocale: Locale
        /**
         * 获取系统Local
         *
         * @return
         */
        get() = Resources.getSystem().configuration.locale

    /**
     * 获取app缓存语言
     */
    private val prefAppLocaleLanguage: String
        /**
         * 获取app缓存语言
         *
         * @return
         */
        get() {
            val locale = dataStoreImpl.getCurrentLanguage().locale ?: systemLocale
            locale.country.let {
                if (it.isNullOrEmpty()) {
                    return locale.language
                } else {
                    return buildString {
                        append(locale.language)
                        append("-")
                        append(locale.country)
                    }
                }
            }
        }

    /**
     * 获取app缓存Locale
     */
    private val prefAppLocale: Locale?
        /**
         * 获取app缓存Locale
         *
         * @return null则无
         */
        get() {
            val appLocaleLanguage = prefAppLocaleLanguage
            return if (appLocaleLanguage.isNotEmpty()) {
                // 系统语言则返回null
                if (SYSTEM_LANGUAGE_TGA == appLocaleLanguage) {
                    null
                } else {
                    Locale.forLanguageTag(appLocaleLanguage)
                }
            } else {
                // 为空，默认是简体中文
                Locale.SIMPLIFIED_CHINESE
            }
        }

    /**
     * 获取当前需要使用的locale，用于activity上下文的生成
     */
    private val currentAppLocale: Locale
        /**
         * 获取当前需要使用的locale，用于activity上下文的生成
         *
         * @return
         */
        get() {
            val prefAppLocale = prefAppLocale
            return prefAppLocale ?: systemLocale
        }


    /**
     * 更新该context的config语言配置，对于application进行反射更新
     *
     * @param context Context 上下文
     * @param locale Locale 语言
     */
    private fun updateLanguage(context: Context, locale: Locale) {
        val resources = context.resources
        val config = resources.configuration
        val contextLocale = config.locale
        if (isSameLocale(contextLocale, locale)) {
            return
        }
        val dm = resources.displayMetrics
        config.setLocale(locale)
        resources.updateConfiguration(config, dm)
    }

    /**
     * 对Application上下文进行替换
     *
     * @param activity activity
     */
    fun applyAppLanguage(activity: Activity) {
        val appLocale = currentAppLocale
        updateLanguage(app.applicationContext, appLocale)
        updateLanguage(activity, appLocale)
    }


    /**
     * 缓存app当前语言
     *
     * @param language
     */
    fun saveAppLocaleLanguage(language: Language) {
        dataStoreImpl.setCurrentLanguage(language)
    }

    /**
     * 获取App当前语言
     *
     * @return
     */
    fun getAppLanguage(): Language {
        return dataStoreImpl.getCurrentLanguage()
    }

    /**
     * 是否是相同的locale
     *
     * @param l0
     * @param l1
     * @return
     */
    private fun isSameLocale(l0: Locale, l1: Locale): Boolean {
        return (equals(l1.language, l0.language)
                && equals(l1.country, l0.country))
    }

    /**
     * Return whether string1 is equals to string2.
     *
     * @param s1 The first string.
     * @param s2 The second string.
     * @return `true`: yes<br></br>`false`: no
     */
    private fun equals(s1: CharSequence?, s2: CharSequence?): Boolean {
        if (s1 === s2) return true
        var length = 0
        return if (s1 != null && s2 != null && s1.length.also { length = it } == s2.length) {
            if (s1 is String && s2 is String) {
                s1 == s2
            } else {
                for (i in 0 until length) {
                    if (s1[i] != s2[i]) return false
                }
                true
            }
        } else false
    }
}
