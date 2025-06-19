package com.gw_reoqoo.cp_account.widget.verify_code

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/1/10 10:12
 * Description: 验证码 item view，所需要对外调用的方法。
 * 如果需要自定义验证码View，必须实现VerifyCodeItemViewStyle
 */
interface VerifyCodeItemViewStyle {

    /**
     * 显示数字
     */
    fun displayNumStyle(char: Char)

    /**
     * 光标闪烁样式
     */
    fun cursorBlinksStyle()

    /**
     * 不显示光标，也不显示数字
     */
    fun defaultStyle()

}