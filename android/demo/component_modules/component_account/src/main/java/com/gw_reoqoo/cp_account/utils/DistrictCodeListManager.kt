package com.gw_reoqoo.cp_account.utils

import android.app.Application
import com.google.gson.Gson
import com.gw_reoqoo.cp_account.http.DistrictCodeListEntity
import com.gw.cp_mine.api.kapi.ILocaleApi
import com.gw_reoqoo.lib_http.entities.DistrictEntity
import com.gw_reoqoo.lib_http.toJson
import com.gwell.loglibs.GwellLogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by lele on 2017/12/5.
 * desc: 解析 地区码列表
 */
@Singleton
class DistrictCodeListManager @Inject constructor() {

    companion object {
        private const val TAG = "DistrictCodeListManager"

        /**
         * 中国地区
         */
        private const val CHINA_ZH = "zh"
        private const val CHINA_MAINLAND = "CN"
        private const val CHINA_HK = "HK"
        private const val CHINA_MACAU = "MO"
        private const val CHINA_TAIWAN = "TW"

    }

    @Inject
    lateinit var app: Application

    @Inject
    lateinit var localeApi: ILocaleApi

    /**
     * 国家地区码列表
     */
    private var codeList: List<DistrictEntity>? = null

    /**
     * 国家地区码对应的国家地区信息
     */
    private val maps = HashMap<String, DistrictEntity>()

    /**
     * 获取当前系统语言的地区码列表
     *
     * @return List<DistrictEntity>
     */
    suspend fun getCodeList(): List<DistrictEntity>? {
        return readDistrictCodeList()
    }

    /**
     * 通过获取国家地区的缩写获取地区码
     *
     * @return DistrictEntity 地区码实体
     */
    suspend fun getDistrictCodeInfo(shorthand: String): DistrictEntity? {
        readDistrictCodeList()
        return maps[shorthand] ?: maps[shorthand.uppercase(Locale.getDefault())]
    }

    /**
     * 通过地区码获取国家地区
     *
     * @return DistrictEntity 地区码实体
     */
    suspend fun getDistrictCodeInfoByCode(code: String): DistrictEntity? {
        readDistrictCodeList()
        return codeList?.let {
            if (it.isEmpty()) {
                null
            } else {
                for (codeBean in it) {
                    if (code == codeBean.districtCode) {
                        return codeBean
                    }
                }
                null
            }
        }
    }

    /**
     * 读取本地国家地区码的文件
     */
    private suspend fun readDistrictCodeList(): List<DistrictEntity>? {
        withContext(Dispatchers.IO) {
            try {
                val `is`: InputStream
                val language =
                    localeApi.getCurrentLanguage().split("_".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                GwellLogUtils.i(TAG, "language: ${language.toJson()}")
                `is` = if (language.size > 1) {
                    val languageStr = language[0]
                    val countryStr = language[1]
                    if (languageStr.contains(CHINA_ZH, true)
                        && (countryStr.contains(CHINA_HK, true) ||
                                countryStr.contains(CHINA_MACAU, true) ||
                                countryStr.contains(CHINA_TAIWAN, true))
                    ) {
                        app.assets.open("district_code/gw_reoqoo_district_code_tc.txt")
                    } else if (languageStr.contains(CHINA_ZH, true) &&
                        countryStr.contains(CHINA_MAINLAND, true)
                    ) {
                        app.assets.open("district_code/gw_reoqoo_district_code.txt")
                    } else {
                        app.assets.open("district_code/gw_reoqoo_district_code_oversea.txt")
                    }
                } else if (language.size == 1) {
                    val languageStr = language[0]
                    if (languageStr.contains(CHINA_HK, true) ||
                        languageStr.contains(CHINA_MACAU, true) ||
                        languageStr.contains(CHINA_TAIWAN, true)
                    ) {
                        app.assets.open("district_code/gw_reoqoo_district_code_tc.txt")
                    } else if (languageStr.contains(CHINA_MAINLAND, true) ||
                        languageStr.contains(CHINA_ZH, true)
                    ) {
                        app.assets.open("district_code/gw_reoqoo_district_code.txt")
                    } else {
                        app.assets.open("district_code/gw_reoqoo_district_code_oversea.txt")
                    }
                } else {
                    app.assets.open("district_code/gw_reoqoo_district_code_oversea.txt")
                }
                val size = `is`.available()
                val buffer = ByteArray(size)
                `is`.read(buffer)
                `is`.close()
                val text = String(buffer)
                parseJSON(text)
            } catch (e: IOException) {
                GwellLogUtils.i(TAG, "readDistrictCodeList: $e")
            }
        }
        return codeList
    }

    /**
     * 解析国家地区的json
     *
     * @param jsonData String
     */
    private fun parseJSON(jsonData: String) {
        try {
            val districtCodeListEntity =
                Gson().fromJson(jsonData, DistrictCodeListEntity::class.java)
            codeList = districtCodeListEntity.districtCodeList
            codeList?.forEach { _entity ->
                maps[_entity.district] = _entity
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}