package com.gw.cp_msg.entity.http

import com.gw.cp_msg.entity.http.UserMessageListBean.DeviceShareInviteData
import com.gw.cp_msg.entity.http.UserMessageListBean.MessageData
import com.jwkj.lib_json_kit.IJsonEntity
import com.squareup.moshi.Json

/**
 * @message 用户消息
 * @user yanzheng@gwell.cc
 * @date 2023年12月14日
 */
data class UserMessageEntity(
    @Json(name = "data")
    var message: UserMessageData? = null
) {
    /**
     * "data":{"list":[{"msgId":6,"tag":"PopupH5.before_three_day_notice","data":"{\"deviceId\":\"4294967497\",
     * \"title\":\"before_three_day_notice\",\"type\":1,\"url\":\"https://notice.cloudlinks.cn/h5/notice/21122089/cloud/AI.html\"}",
     * "status":1,"createTime":1639941910,"expireTime":1655493910}],"end":true}
     */

    /**
     * 参数名	   类型	    描述
     * msgId	   int64	消息id
     * tag	       string	消息标签
     * data	       string	json字符串，里面包含消息标题、内容等数据，根据tag不同可能结构不一样
     * status	   int	    设置状态，0：未读，1：已读
     * createTime  int64	消息创建时间
     * expireTime  int64	消息过期时间
     * showWay     int      展示方式
     */
}

data class UserMessageData(
    /**
     * 公告状态列表
     */
    @Json(name = "list")
    var list: List<UserMessageListBean>? = null,

    /**
     * 分页是否结束
     */
    @Json(name = "end")
    var end: Boolean = false
)

data class UserMessageListBean(
    /**
     * 消息标签
     * 参见 [TagType]
     */
    @Json(name = "tag")
    var tag: String? = null,

    /**
     * json字符串，里面包含消息标题、内容等数据，根据tag不同可能结构不一样.
     * 如果 [.tag] 为 [TagType.TYPE_POPUP_H5] 时, 这个字段 对应的结果 是 [MessageData].
     * 如果 [.tag] 为 [TagType.TYPE_DEVICE_SHARE_INVITE] 时, 这个字段 对应的结果 是 [DeviceShareInviteData].
     */
    @Json(name = "data")
    var msgData: String? = null,

    /**
     * 设置状态.
     * 参见 [MsgState]
     */
    @Json(name = "status")
    var status: Int = MsgState.STATUS_UNREAD.state,

    /**
     * 消息创建时间
     */
    @Json(name = "createTime")
    var createTime: Long = 0,

    /**
     * 消息过期时间
     */
    @Json(name = "expireTime")
    var expireTime: Long = 0,

    /**
     * 消息id
     */
    @Json(name = "msgId")
    var msgId: Long = 0,
) {

    class MessageData : IJsonEntity {

        /**
         * 弹窗对应h5页面url
         */
        @Json(name = "url")
        var url: String? = null

        /**
         * 设备id
         */
        @Json(name = "deviceId")
        var deviceId: String? = null

        /**
         * 展示方式
         * 取值 参见 [ShowWayType]
         */
        @Json(name = "showWay")
        var showWay = ShowWayType.TYPE_DIALOG.type

        /**
         * 消息类型.
         * 取值 参见 [MsgType]
         */
        @Json(name = "msgType")
        var msgType: String? = null

        /**
         * 套餐时长，单位：天
         */
        @Json(name = "packageDays")
        var packageDays = 0

        /**
         * 有效期. 单位: 秒. 基于 UTC 时间.
         * 为0是app首次展示，根据validateDays开始做倒计时，非0时使用此过期时间做倒计时.
         */
        @Json(name = "expireTime")
        var expireTime: Long = 0

        /**
         * 有效天数
         */
        @Json(name = "validateDays")
        var validateDays = 0

    }

    class DeviceShareInviteData : IJsonEntity {

        /**
         * 弹窗对应h5页面url.
         * 比如: https://domain/index.html?inviteToken=xxxxx
         */
        @Json(name = "url")
        var url: String? = null

        /**
         * 设备id
         */
        @Json(name = "deviceId")
        var deviceId: String? = null

        /**
         * 展示方式
         * 取值 参见 [ShowWayType]
         */
        @Json(name = "showWay")
        var showWay = ShowWayType.TYPE_NONE.type

    }
}

/**
 * 消息标签
 *
 * @property tag String 消息标签
 * @constructor
 */
enum class TagType(val tag: String) {
    /**
     * 默认.
     * 包含: 新设备免费领取类型, 优惠劵领取, 优惠劵过期提醒
     */
    TYPE_POPUP_H5("PopupH5."),

    /**
     * 设备分享邀请
     */
    TYPE_DEVICE_SHARE_INVITE("DeviceShareInvite")
}

/**
 * 消息状态
 *
 * @property state Int 消息状态
 * @constructor
 */
enum class MsgState(val state: Int) {
    /**
     * 已读消息
     */
    STATUS_READ(1),

    /**
     * 未读消息
     */
    STATUS_UNREAD(0)
}

/**
 * 消息类型
 *
 * @property type Int 消息类型
 * @constructor
 */
enum class MsgType(val type: String) {
    /**
     * 新设备免费领取类型
     */
    TYPE_NEW_DEVICE_RECEIVE("NewDevFreeReceive"),

    /**
     * 优惠劵领取
     */
    TYPE_COUPON_RECEIVE("CouponReceive"),

    /**
     * 优惠劵过期提醒
     */
    TYPE_COUPON_EXPIRE("CouponExpire"),

    /**
     * 设备首绑
     */
    TYPE_NEW_DEVICE_GO_CLOUD("VasPromotion"),
}

/**
 * 展示方式
 *
 * @property type Int 展示方式
 * @constructor
 */
enum class ShowWayType(val type: Int) {
    /**
     * 静默，不展示
     */
    TYPE_NONE(0),

    /**
     * 弹窗展示url
     */
    TYPE_DIALOG(1),

    /**
     * 跳转展示url
     * 即 webview展示
     */
    TYPE_WEB(2),
}