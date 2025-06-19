package com.gw.cp_mine.entity

import android.app.Activity
import com.gw_reoqoo.lib_router.ReoqooRouterPath

/**
 * @author: xuhaoyuan
 * @date: 2023/8/18
 * description:
 * 1.
 */
class MenuListEntity(
    var iconId: Int? = null,
    var functionName: Int,
    var routerPath: String,
    var showNotice: Boolean? = false
) {

    override fun toString(): String {
        return "MenuListEntity(iconId=$iconId, functionName=$functionName, routerPath='$routerPath')"
    }
}