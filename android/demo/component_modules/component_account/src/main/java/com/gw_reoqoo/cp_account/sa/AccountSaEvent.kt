package com.gw_reoqoo.cp_account.sa

import com.jwkj.base_statistics.sa.kits.SA

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/3/13 10:11
 * Description: AccountSaEvent
 */
object AccountSaEvent : SA.IEvent {

    /**
     * 确认注册地
     */
    const val REGISTER_BUTTONCLICK = "Register_ButtonClick"

    /**
     * 确认注册地
     */
    const val REGION_GETNAME = "Region_GetName"

    /**
     * 注册_确定注册
     */
    const val REGISTER_RESULTCLICK = "Register_ResultClick"

    /**
     * 发送验证码
     */
    const val SEND_VCODE = "Send_Vcode"

    /**
     * 设置密码
     */
    const val SET_PASSWORD = "Set_Password"

    /**
     * 登录_点击忘记密码
     */
    const val LOGIN_FORGOTPASSWORDBUTTONCLICK = "Login_ForgotPasswordButtonClick"

    /**
     * 登录_确认登录
     */
    const val LOGIN_ACCOUNTBUTTONCLICK = "Login_AccountButtonClick"

    object EventAttr {

        /**
         * 地名
         */
        const val REGION_NAME = "region_name"

        /**
         * 注册方式
         */
        const val REGISTER_METHOD = "register_method"

        /**
         * 发送验证码结果
         */
        const val SEND_RESULT = "Send_Result"

        /**
         * 设置密码结果
         */
        const val SET_PASSWORDRESULT = "Set_PasswordResult"

        /**
         * 登录方式
         */
        const val LOGIN_METHOD = "login_method"

    }

}

