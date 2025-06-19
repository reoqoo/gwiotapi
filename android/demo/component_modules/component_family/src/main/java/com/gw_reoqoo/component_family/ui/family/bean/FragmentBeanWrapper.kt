package com.gw_reoqoo.component_family.ui.family.bean


/**
 * @Description: - 对fragment对象的包装以及创建方式的封装
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 * @param textSrc 界面顶部对应的模块名称 如：设备,场景
 * @param fragmentUrl fragment对应的Route的Uri
 * @param params fragment所需要传递的参数
 */
data class FragmentBeanWrapper(
    val textSrc: Int,
    val fragmentUrl: String,
    val params: Map<String, String>,
)