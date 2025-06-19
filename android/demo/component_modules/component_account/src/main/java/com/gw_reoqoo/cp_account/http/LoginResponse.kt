package com.gw_reoqoo.cp_account.http

import com.gw_reoqoo.lib_http.HttpResponse
import com.gw_reoqoo.lib_room.user.UserInfo
import com.gw_reoqoo.lib_utils.ktx.isEmptyOrZero

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/4 9:52
 * Description: LoginResponse
 */
open class LoginResponse : HttpResponse() {

    /**
     * response实例数据
     * requestId : d009e0c7-5ec3-4281-af20-e6d2bb8a4eea
     * data : {"accessToken":"0181A97EC1ABD064F734766F01000000FC09F2E39AA22D7C7F30223937671D6FEE00A15957AED3071690157A5FC3131EC6D32BF27094F5FCB025F20C387C25DB","expireTime":1693989057,"accessId":"-9223372030332888386","vcode1":"679685666","vcode2":"1541647863","userId":"-2068047170","sessionId":"1338288829","sessionId2":"-1026407999","terminalId":"-9223372030332888386","area":"cn"}
     */

    /**
     * 用户信息
     */
    var data: DataBean? = null

    class DataBean {
        /**
         * accessToken : 0181A97EC1ABD064F734766F01000000FC09F2E39AA22D7C7F30223937671D6FEE00A15957AED3071690157A5FC3131EC6D32BF27094F5FCB025F20C387C25DB
         * expireTime : 1693989057
         * accessId : -9223372030332888386
         * vcode1 : 679685666
         * vcode2 : 1541647863
         * userId : -2068047170
         * sessionId : 1338288829
         * sessionId2 : -1026407999
         * terminalId : -9223372030332888386
         * area : cn
         */
        var accessToken: String? = null

        var expireTime: Long = 0

        var accessId: String? = null

        var vcode1: String? = null

        var vcode2: String? = null

        var headUrl: String? = null

        var nick: String? = null

        var userId: String? = null
            get() {
                return field?.let {
                    "0" + (it.toLong() and 0x7fffffff)
                } ?: "0"
            }

        var sessionId: String? = null

        var sessionId2: String? = null

        var terminalId: String? = null

        var firstLogin: Boolean? = null

        var unionIdToken: String? = null

        var hasBindAccount: Boolean? = null

        /**
         * 服务器所属大区
         */
        var area: String? = null

        /**
         * 用户注册地
         */
        var regRegion: String? = null
    }

}

/**
 * 登录resp的扩展类，用于将 LoginResponse.DataBean 转换成 数据库的 UserInfo 类
 *
 * @receiver LoginResponse.DataBean
 * @param email String?
 * @param phone String?
 * @return UserInfo?
 */
fun LoginResponse.DataBean.toUserInfo(email: String? = null, phone: String? = null): UserInfo? {
    val mPhone = if (phone.isEmptyOrZero()) {
        ""
    } else {
        phone
    }
    val userId = userId ?: ""
    val terminalId = terminalId ?: ""
    val accessId = accessId ?: ""
    val accessToken = accessToken ?: ""
    val sessionId = sessionId ?: ""
    if (userId.isEmpty() || terminalId.isEmpty() || accessId.isEmpty() || accessToken.isEmpty() || sessionId.isEmpty()) {
        return null
    } else {
        return UserInfo(
            id = userId,
            expireTime = expireTime.toString(),
            terminalId = terminalId,
            email = email,
            phone = mPhone,
            nickname = nick,
            headUrl = headUrl,
            unionIdToken = unionIdToken,
            sessionId = sessionId,
            accessId = accessId,
            accessToken = accessToken,
            firstLogin = firstLogin,
            area = area,
            regRegion = regRegion,
            hasBindAccount = hasBindAccount
        )
    }
}