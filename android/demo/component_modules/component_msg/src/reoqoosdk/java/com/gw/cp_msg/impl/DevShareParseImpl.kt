package com.gw.cp_msg.impl

import com.gw.cp_config_net.entity.ShareQRCodeEntity
import com.gw.cp_msg.api.interfaces.IDevShareParse
import com.gw.reoqoosdk.constant.NetConfigConstant
import com.gw.reoqoosdk.net_config.api.INetConfigService
import com.gwell.loglibs.GwellLogUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/10/18 15:29
 * Description: DevShareParseImpl
 */
@Singleton
class DevShareParseImpl @Inject constructor(
    private val configApi: INetConfigService
) : IDevShareParse {

    companion object {
        private const val TAG = "DevShareParseImpl"
    }

    override fun devShareMsg(shareUrl: String): Map<String, String>? {
        shareUrl.run {
            if (isNullOrEmpty()) {
                GwellLogUtils.e(TAG, "redirectUrl is empty")
                return null
            }
            if (this.startsWith("AppNativeUrl?", true)) {
                val url = parseShareUrl(this)
                GwellLogUtils.i(TAG, "url $url")

                val qrcodeEntity = configApi.parseShareUrl(url, INetConfigService.Type.SHARE)
                GwellLogUtils.i(TAG, "qrcodeEntity $qrcodeEntity")
                val inviteCode = qrcodeEntity[NetConfigConstant.PARAMS_INVITE_CODE]
                val deviceID = qrcodeEntity[NetConfigConstant.PARAMS_DEVICE_ID]
                val sharerName = qrcodeEntity[NetConfigConstant.PARAMS_SHARER_NAME]
                if (inviteCode.isNullOrEmpty() || deviceID.isNullOrEmpty()) {
                    GwellLogUtils.e(TAG, "inviteCode $inviteCode, deviceID $deviceID")
                    return null
                }
                return mapOf(
                    "inviteCode" to inviteCode,
                    "deviceID" to deviceID,
                    "sharerName" to (sharerName ?: "")
                )
            }
        }
        return null
    }

    override fun parseShareUrl(url: String): String {
        return if (url.startsWith("AppNativeUrl?", true)) {
            url.replace(
                "AppNativeUrl?",
                ShareQRCodeEntity.HOST_DEV_SHARE_SDK
            )
        } else {
            url
        }
    }
}