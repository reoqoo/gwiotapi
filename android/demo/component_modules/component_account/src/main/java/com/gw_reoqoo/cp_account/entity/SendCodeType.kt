package com.gw_reoqoo.cp_account.entity

/**
 * 发送验证码的类型
 *
 * @property type Int 类型number
 * @constructor
 */
enum class SendCodeType(val type: Int) {
    /**
     * 登录后绑定/换绑
     */
    CHANGE_BIND(0),

    /**
     * 找回密码
     */
    FIND_PWD(1),

    /**
     * H5积分/电话提醒
     */
    TYPE_H5(2),

    /**
     * 注册/第三方登录绑定
     */
    REGISTER_BIND(3)

}