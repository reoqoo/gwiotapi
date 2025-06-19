package com.gw.cp_mine.entity

import com.gw_reoqoo.resource.R
import java.util.Locale

/**
 * @author: xuhaoyuan
 * @date: 2023/9/6
 * description: 该entity试用于recyclerView中有某一项item需要被选中、打钩等默认状态的情况
 * @param language 使用在资源文件中已经存放好的文案的id值
 * @param selected false(未选中), true(默认选中)
 */
data class LanguageEntity(
    var language: Language,
    var selected: Boolean = false
)

/**
 * 简体中文
 */
private val CHINESE = Locale("zh", "CN")

/**
 * 台湾繁体
 */
private val CHINESE_TW = Locale("zh", "TW")

/**
 * 香港繁体
 */
private val CHINESE_HK = Locale("zh", "HK")

/**
 * 泰语
 */
private val LANGUAGE_TH = Locale("th", "")

/**
 * 越南
 */
private val LANGUAGE_VI = Locale("vi", "")

/**
 * 韩语
 */
private val LANGUAGE_KO = Locale("ko", "")

/**
 * 日语
 */
private val LANGUAGE_JA = Locale("ja", "")

/**
 * 马来西亚
 */
private val LANGUAGE_MS = Locale("ms", "")

/**
 * 印尼语
 */
private val LANGUAGE_IN = Locale("in", "")

/**
 * 英语
 */
private val LANGUAGE_EN = Locale.ENGLISH

/**
 * 语言对应的枚举
 *
 * @param strRes 对应的资源
 * @param locale 对应的local
 */
enum class Language(val strRes: Int, val locale: Locale?) {
    /**
     * 跟随系统
     */
    SYSTEM(R.string.AA0272, null),

    /**
     * 简体中文
     */
    ZH_HANS(R.string.simplified_chinese, CHINESE),

    /**
     * 繁体中文(香港)
     */
    ZH_HK(R.string.traditional_chinese_hongKong, CHINESE_HK),

    /***
     * 繁体中文(台湾)
     */
//    ZH_TW(R.string.traditional_chinese_taiwan, CHINESE_TW),

    /**
     * 英文
     */
    EN(R.string.english, LANGUAGE_EN),

    /**
     * 越南语
     */
    VI(R.string.language_vi, LANGUAGE_VI),

    /**
     * 泰语
     */
    TH(R.string.language_th, LANGUAGE_TH),

    /**
     * 韩语
     */
    KO(R.string.language_ko, LANGUAGE_KO),

    /**
     * 日语
     */
    JA(R.string.language_ja, LANGUAGE_JA),

    /**
     * 印尼语
     */
    IN(R.string.language_in, LANGUAGE_IN),

    /**
     * 马来西亚语
     */
    MS(R.string.language_ms, LANGUAGE_MS),

}