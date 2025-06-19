package com.gw_reoqoo.cp_account.utils

import android.text.TextUtils
import com.gw.cp_mine.api.kapi.ILocaleApi
import com.gw_reoqoo.lib_http.entities.DistrictEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DistrictCodeUtils @Inject constructor() {

    companion object {
        private const val TAG = "DistrictCodeUtils"
    }

    @Inject
    lateinit var manager: DistrictCodeListManager

    @Inject
    lateinit var localeApi: ILocaleApi

    /**
     * 通过 手机的local信息 来 获取 用户当前所在的地区 的 地区码
     */
    suspend fun getCurrentDistrictCodeInfo(): DistrictEntity? {
        val district = localeApi.getCurrentCountry()
        val districtCodeBean = if (!TextUtils.isEmpty(district)) {
            //以下地区没有找到对应的地区码，用右边的代替
            //BQ    Bonaire, Sint Eustatius and Saba,        NL  Netherlands
            //CW    Curaçao                                  NL  Netherlands
            //BV    Bouvet Island                            NO  Norway
            //HM    Heard Island and McDonald Islands        AU  Australia
            //IO    British Indian Ocean Territory           GB  Britain
            //SX    Sint Maarten (Dutch part)                NL  Netherlands
            //TF    French Southern Territories              FR  France
            when (district) {
                "BQ", "CW", "SX" -> {
                    manager.getDistrictCodeInfo("NL")
                }

                "BV" -> {
                    manager.getDistrictCodeInfo("NO")
                }

                "HM" -> {
                    manager.getDistrictCodeInfo("AU")
                }

                "IO" -> {
                    manager.getDistrictCodeInfo("GB")
                }

                "TF" -> {
                    manager.getDistrictCodeInfo("FR")
                }

                else -> {
                    manager.getDistrictCodeInfo(district)
                }
            }
        } else {
            manager.getDistrictCodeInfo("other")
        }
        return districtCodeBean ?: manager.getDistrictCodeInfo("other")
    }

    /**
     * 获取地区+地区码
     *
     * @param districtCodeBean 地区类
     * @return 真实拼接
     */
    fun getRealDistrict(districtCodeBean: DistrictEntity?): String {
        var district = ""
        if (null != districtCodeBean) {
            val sb = StringBuilder(districtCodeBean.districtName)
            sb.append("+")
            sb.append(districtCodeBean.districtCode)
            district = sb.toString()
        }
        return district
    }
}