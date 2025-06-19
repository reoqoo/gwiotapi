package com.gw.cp_msg.entity.http

import com.google.gson.annotations.SerializedName
import com.jwkj.lib_json_kit.IJsonEntity
import com.squareup.moshi.Json

/**
 * 活动列表
 *
 * @constructor Create empty Notice list
 */
data class EventBenefitsEntity(

    @Json(name = "data")
    val dataEntity: NoticeList?

) : IJsonEntity {

    companion object {
        /**
         * 未读消息
         */
        const val STATUS_UNREAD_NOTICE = 0

        /**
         * 已读消息
         */
        const val STATUS_HAVE_READ = 1

        /**
         * 已过期
         */
        const val STATUS_ACTIVE_EXPIRE = 2
    }
}

data class NoticeList(
    @Json(name = "list")
    val noticeList: List<Notice>?
) : IJsonEntity

/**
 * 参数名	类型	描述
 * id	int64	活动福利的唯一id
 * tag	string	标签,唯一性标识。值为 messageCenter
 * url	string	Url
 * picUrl	string	banner位图片Url
 * status	int	1 未过期 2 已过期
 * startTime	int64	活动开始时间
 * expireTime	int64	过期时间
 */
data class Notice(
    
    @Json(name = "id")
    var id: Long,

    @Json(name = "noticeId")
    var noticeId: Long,

    @Json(name = "tag")
    var tag: String,

    @Json(name = "url")
    var url: String,

    @Json(name = "picUrl")
    var picUrl: String,

    @Json(name = "status")
    var status: Int,

    @Json(name = "startTime")
    var startTime: Long,

    @Json(name = "expireTime")
    var expireTime: Long,

    /**
     * 是否已上报
     */
    var isReport: Boolean = false

) : IJsonEntity
