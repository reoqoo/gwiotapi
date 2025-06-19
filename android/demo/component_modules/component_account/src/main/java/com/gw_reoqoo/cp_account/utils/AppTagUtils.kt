package com.gw_reoqoo.cp_account.utils

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import com.gw_reoqoo.lib_utils.version.VersionUtils
import com.gw_reoqoo.cp_account.BuildConfig
import com.gwell.loglibs.GwellLogUtils

/**
 * Yoosee中登录使用的工具类
 */
object AppTagUtils {

    private const val TAG = "AppTagUtils"

    /**
     * 应用版本号
     */
    private var versionName: String? = null

    /**
     * 获取应用信息
     *
     * @param context Context 上下文
     * @param appId String    应用Id
     * @param verName String  默认应用版本号（当获取PackageInfo失败时使用）
     * @return String
     */
    fun getAppTag(context: Context, appId: String, verName: String): String {
        GwellLogUtils.i(TAG, "getAppTag appId=$appId, verName=$verName, versionName=$versionName")
        versionName?.let { version ->
            val appIntStringVersion = getAppVersion(context, version)
            return appId + appIntStringVersion
        } ?: run {
            try {
                val pm = context.applicationContext.packageManager
                val pi = pm.getPackageInfo(context.packageName, 0)
                versionName = pi.versionName
                GwellLogUtils.i(TAG, "getPackageInfo versionName=$versionName")
            } catch (e: NameNotFoundException) {
                GwellLogUtils.e(TAG, "getAppTag error, msg=${e.message}")
            }
            val appIntStringVersion = getAppVersion(context, versionName ?: verName)
            return appId + appIntStringVersion
        }
    }

    /**
     * 获取app的版本号
     *
     * @param context Context 上下文
     * @param verName String  默认应用版本号（当获取PackageInfo失败时使用）
     * @param context Context
     * @param verName String
     * @return Int
     */
    private fun getAppVersion(context: Context, verName: String): Int {
        return VersionUtils.getIntVersion(VersionUtils.getAppVersionName(context))
    }

}