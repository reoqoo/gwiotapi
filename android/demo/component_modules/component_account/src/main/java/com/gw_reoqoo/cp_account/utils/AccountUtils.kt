package com.gw_reoqoo.cp_account.utils

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/11/1 23:51
 * Description: AccountUtils
 */
object AccountUtils {

    /**
     * 密码格式校验（长度8-30个字符，包含字母和数字）
     *
     * @param input String?
     * @return Boolean
     */
    fun checkPasswordFormat(input: String?): Boolean {
        if (input.isNullOrEmpty()) {
            return false
        }
        val regex = Regex("^(?=.*[a-zA-Z])(?=.*\\d).{8,30}$")
        return regex.matches(input)
    }

}