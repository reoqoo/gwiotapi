package com.gw.cp_mine.api_impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.gw.cp_mine.api.kapi.IMineModuleApi
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.package_utils.LaunchAppUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/21 22:53
 * Description: MineModuleImpl
 */
@Singleton
class MineModuleImpl @Inject constructor() : IMineModuleApi {
    companion object {
        private const val TAG = "MineModuleImpl"

        private const val WEBSITE_URL = "https://reoqoo.com/d/"
    }

    override fun appVersionUpgrade(context: Context) {
        try {
            LaunchAppUtils.lunchGoogleMarket(context)
        } catch (e: Exception) {
            GwellLogUtils.e(TAG, "appVersionUpgrade fail: reason ${e.message}")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}