package com.gw.cp_config.impl

import com.gw.cp_config.api.IAppParamApi
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/26 15:37
 * Description: AppParamApiImpl
 */
class AppParamApiImpl @Inject constructor() : IAppParamApi {

    companion object {

        /**
         * appID 值
         */
        private const val APP_ID = "6591aa95d687a8d263bd32073a784774"

        /**
         * appToken 值
         */
        private const val APP_TOKEN = "0ea9cbcc3a5f9f3ea1719dfc1955c21663bd49f5ac9ab1fc938b9b27408a5f4a"

        /**
         * appName 值（这个名称是协议的参数，与app的名称是两个不同的数据）
         */
        private const val APP_NAME = "Defender ClearVu"

    }

    override fun getAppID(): String {
        return APP_ID
    }

    override fun getAppToken(): String {
        return APP_TOKEN
    }

    override fun getAppName(): String {
        return APP_NAME
    }
}