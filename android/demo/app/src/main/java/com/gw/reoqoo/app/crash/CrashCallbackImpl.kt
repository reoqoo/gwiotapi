package com.gw.reoqoo.app.crash

import android.app.Application
import com.gw.reoqoo.BuildConfig
import com.jwkj.base_crash.IAppInfoCallback

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/12/20 11:19
 * Description: CrashCallbackImpl
 */
class CrashCallbackImpl(val app: Application) : IAppInfoCallback {
    override fun appBuildNum(): String {
        return BuildConfig.BUILD_NUMBER
    }

    override fun appBuildTime(): String {
        return BuildConfig.BUILD_TIME
    }

    override fun appBuildType(): String {
        return BuildConfig.BUILD_TYPE
    }

    override fun appContext(): Application {
        return app
    }

    override fun appJenkinsBuild(): Boolean {
        return BuildConfig.IS_JENKINS_ENV
    }

    override fun appSubVersion(): String {
        return BuildConfig.SUB_VERSION
    }

    override fun appVersionCode(): String {
        return BuildConfig.VERSION_CODE.toString()
    }

    override fun appVersionName(): String {
        return BuildConfig.VERSION_NAME
    }

    override fun flavorName(): String {
        return BuildConfig.FLAVOR
    }

    override fun isRelease(): Boolean {
        return !BuildConfig.DEBUG
    }

    override fun packageName(): String {
        return BuildConfig.APPLICATION_ID
    }
}