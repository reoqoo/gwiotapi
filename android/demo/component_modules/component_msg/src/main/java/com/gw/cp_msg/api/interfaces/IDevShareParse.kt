package com.gw.cp_msg.api.interfaces

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/10/18 15:25
 * Description: DevShareParseUtils
 */
interface IDevShareParse {

    /**
     * 解析分享链接
     *
     * @param shareUrl String
     * @return Map<String, String>?
     */
    fun devShareMsg(shareUrl: String): Map<String, String>?

    /**
     * 解析分享链接中的信息
     *
     * @param url String
     * @return String
     */
    fun parseShareUrl(url: String): String

}