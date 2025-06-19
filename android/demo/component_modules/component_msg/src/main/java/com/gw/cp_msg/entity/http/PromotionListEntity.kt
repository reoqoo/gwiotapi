package com.gw.cp_msg.entity.http

import com.google.gson.JsonObject
import com.squareup.moshi.Json

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/12/12 11:18
 * Description: PromotionDataEntity
 */
data class PromotionListEntity(
    /**
     * code : 0
     * msg : Success
     * requestId : 42ab6388-d53c-436a-9949-fb0a11d3deeb
     * data : {"list":[{"devId":"24515986","floatWindow":{"listIcon":{"title":"Overseas14Gift","content":"首次订阅赠送14天"}}},{"devId":"23092736","floatWindow":{"listIcon":{"title":"FirstPurchaseBenefits","content":"首次1云购","redirectUrl":"https://domain/"}}}]}
     */
    private val code: Int = 0,
    private val msg: String? = null,
    private val requestId: String? = null,

    @Json(name = "data")
    private val dataEntity: List<PromotionDataEntity>? = null,
)

data class PromotionDataEntity(
    /**
     * devId : 24515986
     * floatWindow : {"listIcon":{"title":"FirstPurchaseBenefits","content":"首次1云购","redirectUrl":"https://domain/"}}
     */
    private val devId: String? = null,
    private val floatWindow: FloatWindowBean? = null
)

data class FloatWindowBean(
    /**
     * listIcon : {"title":"FirstPurchaseBenefits","content":"首次1云购","redirectUrl":"https://domain/"}
     */
    private val listIcon: ListIconBean? = null
)

data class ListIconBean(
    /**
     * title : Overseas14Gift
     * content : 首次订阅赠送14天
     * redirectUrl : https://domain/
     */
    private val title: String? = null,
    private val content: String? = null,
    private val redirectUrl: String? = null,
)


