package com.gw.cp_msg.entity.http

import com.google.gson.annotations.SerializedName
import com.jwkj.lib_json_kit.IJsonEntity
import java.io.Serializable

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/19 15:23
 * Description: MsgDetailEntity
 */
data class MsgDetailEntity(

    /**
     *  参数	        类型	    描述
     *  tag	        string	消息类型（MsgCenter_CouponRemind:优惠劵提醒，MsgCenter_VSS:云存服务动态，
     *  MsgCenter_FCS:4G流量服务动态，MsgCenter_CustomerSrv:客服消息，MsgCenter_CoinsRemind:金币提醒，MsgCenter_Feedback:问题反馈, MsgCenter_ShareGuest:分享访客）
     *  deviceId	long	设备id
     *  msgTime	    long	最新消息时间（毫秒时间戳）
     *  isHeap	    boolean	消息是否沉淀入库，false：无详情页，true：有详情页可点击进入
     *  title	    string	标题
     *  summary	    string	消息概要
     *  unreadCnt	int	    未读消息条数（大于>1: 数字提示，1：红点提示）
     *  redirectUrl	String	覆盖类消息，点击跳转页面地址，为空时不可跳转
     *  alarmType   int     报警事件类型（注意，该字段是本地使用，非服务器返回，用于报警通知判断）
     *  alarmId     string  报警事件id(注意，该字段是本地使用，非服务器返回，用于报警通知判断)
     */

    @SerializedName("tag")
    var tag: String,

    @SerializedName("deviceId")
    var deviceId: Long,

    @SerializedName("msgTime")
    var msgTime: Long,

    @SerializedName("isHeap")
    var isHeap: Boolean,

    @SerializedName("title")
    var title: String,

    @SerializedName("summary")
    var summary: String,

    @SerializedName("unreadCnt")
    var unreadCnt: Int,

    @SerializedName("redirectUrl")
    var redirectUrl: String,

    @SerializedName("alarmType")
    var type: Long,

    @SerializedName("alarmId")
    var alarmId: String?,

    @SerializedName("appVersion")
    var appVersion: String? = null

) : IJsonEntity, Serializable, Comparable<MsgDetailEntity> {

    var customerMsg: MutableList<MsgInfoListEntity.MSGInfo>? = null

    override fun compareTo(other: MsgDetailEntity): Int {
        return (msgTime - other.msgTime).toInt()
    }

    companion object {
        /**
         * 优惠券提醒
         */
        const val TAG_MSG_CENTER_COUPON_REMIND = "MsgCenter_CouponRemind"

        /**
         * 云服务动态
         */
        const val TAG_MSG_CENTER_VSS = "MsgCenter_VSS"

        /**
         * 4G流量动态
         */
        const val TAG_MSG_CENTER_FCS = "MsgCenter_FCS"

        /**
         * 客服消息
         */
        const val TAG_MSG_CENTER_CUSTOMER_SRV = "MsgCenter_CustomerSrv"

        /**
         * 金币提醒
         */
        const val TAG_MSG_CENTER_COINS_REMIND = "MsgCenter_CoinsRemind"

        /**
         * 问题反馈
         */
        const val TAG_MSG_CENTER_FEEDBACK = "MsgCenter_Feedback"

        /**
         * 分享访客
         */
        const val TAG_MSG_CENTER_SHARE_GUEST = "MsgCenter_ShareGuest"

        /**
         * App更新
         */
        const val TAG_MSG_CENTER_APP_UPGRADE = "MsgCenter_APPUpgrade"

        /**
         * 固件更新
         */
        const val TAG_MSG_CENTER_FIRMWARE_UPDATE = "MsgCenter_FirmwareUpdate"

        /**
         * 报警事件
         */
        const val TAG_MSG_CENTER_ALARM_EVENT = "MsgCenter_AlarmEvent"

    }
}
