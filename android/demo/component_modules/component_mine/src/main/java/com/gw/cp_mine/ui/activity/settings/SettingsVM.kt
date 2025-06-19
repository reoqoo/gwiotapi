package com.gw.cp_mine.ui.activity.settings

import com.gw.cp_mine.entity.MenuListEntity
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.therouter.TheRouter
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/6/25 10:00
 * Description: SettingsVm
 */
class SettingsVM : ABaseVM() {

    fun getItems() = listOf(
        MenuListEntity(
            functionName = RR.string.AA0613,
            routerPath = ReoqooRouterPath.MinePath.ACTIVITY_SETTING_MSG_PUSH
        ),
        MenuListEntity(
            functionName = RR.string.AA0614,
            routerPath = ReoqooRouterPath.MinePath.ACTIVITY_SETTING_SYSTEM_PERMISSION
        ),
        MenuListEntity(
            functionName = RR.string.AA0221,
            routerPath = ReoqooRouterPath.MinePath.ACTIVITY_LANGUAGE
        ),
    )

    /**
     * 页面跳转
     *
     * @param routerPath String 页面路径
     */
    fun jumpToNext(routerPath: String) {
        pageJumpData.postValue(PageJumpData(TheRouter.build(routerPath)))
    }

}