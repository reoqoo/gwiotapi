package com.gw_reoqoo.component_device_share.entities

/**
 * @Description: - 对Fragment的路由地址，以及需要传参的包装
 * @Author: XIAOLEI
 * @Date: 2023/8/9
 * @param url fragment对应的路由地址
 * @param with fragment需要的参数
 */
data class FragmentRouteWrapper(
    val url: String,
    val with: Map<String, Any>
)