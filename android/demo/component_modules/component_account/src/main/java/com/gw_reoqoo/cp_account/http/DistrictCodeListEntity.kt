package com.gw_reoqoo.cp_account.http

import com.gw_reoqoo.lib_http.entities.DistrictEntity
import com.jwkj.lib_json_kit.IJsonEntity


/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/3 20:00
 * Description: DistrictCodeList
 */
data class DistrictCodeListEntity(var districtCodeList: List<DistrictEntity>) : IJsonEntity