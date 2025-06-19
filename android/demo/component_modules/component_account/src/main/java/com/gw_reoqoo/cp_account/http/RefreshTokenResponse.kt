package com.gw_reoqoo.cp_account.http

import com.squareup.moshi.Json

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/1/9 18:05
 * Description: RefreshTokenResponse
 */
data class RefreshTokenResponse(

    @Json(name = "accessToken")
    val accessToken: String,

    @Json(name = "expireTime")
    val expireTime: Long

)
