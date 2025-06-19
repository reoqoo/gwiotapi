package com.gw.cp_config.entity

import com.google.gson.annotations.SerializedName
import com.jwkj.lib_json_kit.IJsonEntity
import java.io.Serializable

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/7 14:15
 * Description: ConfigJsonEntity
 */
data class ConfigJsonEntity(

    /**
     * 配置版本号
     */
    @SerializedName("config_version") val version: String,

    /**
     * 当前是否使用新版本权限
     */
    @SerializedName("platform") val platform: PlatformEntity?,

    /**
     * 产品列表
     */
    @SerializedName("pids") val productList: Map<String, DevConfigEntity>,

    /**
     * 场景默认名称
     */
    @SerializedName("sceneName") val sceneList: SceneEntity

) : Serializable, IJsonEntity

data class PlatformEntity(
    /**
     * 是否使用新版本权限(0为 旧版本，1 为新版本)
     */
    @SerializedName("newSharePermissionMode") val mode: Int = 0,
)
