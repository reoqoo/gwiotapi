package com.gw_reoqoo.cp_account.http

import com.gw_reoqoo.lib_http.HttpResponse

/**
 * Created by USER on 2017/11/8.
 */
class CountryCodeListResponse: HttpResponse() {

    var countryCodeList: String? = null

    override fun toString(): String {
        return "CountryCodeListResponse{" +
                super.toString() +
                "CountryCodeList='" + countryCodeList + '\'' +
                '}'
    }
}