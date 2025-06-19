package com.gw_reoqoo.cp_account.http

import com.jwkj.lib_json_kit.IJsonEntity
import java.io.Serializable

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/1/5 17:06
 * Description: VerifyCodeResult
 */
class VerifyCodeResponse : Serializable, IJsonEntity {
    /**
     * {
     * "code": 0,
     * "msg": "Success",
     * "data": null
     * }
     */
    var code = 0
    var msg: String? = null
    var data: Any? = null
    override fun toString(): String {
        return "VerifyCodeResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}'
    }
}