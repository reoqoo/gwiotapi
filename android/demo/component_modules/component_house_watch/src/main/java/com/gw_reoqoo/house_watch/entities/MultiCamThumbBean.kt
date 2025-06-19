package com.gw_reoqoo.house_watch.entities

/**
 * 多镜头的摄像头的Bean
 * @param thumbImgUrl 缩略图的url
 * @param checked 是否选中
 */
data class MultiCamThumbBean(
    val thumbImgUrl: String,
    val camIndex: Int,
    var checked: Boolean = false
)