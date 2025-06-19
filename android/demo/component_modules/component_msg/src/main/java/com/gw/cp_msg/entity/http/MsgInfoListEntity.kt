package com.gw.cp_msg.entity.http

import com.google.gson.annotations.SerializedName
import com.jwkj.lib_json_kit.IJsonEntity
import java.io.Serializable

/**
 * 二级界面消息列表.
 * 说明一下: 其实 消息中心 不应当 需要 app去知道 各个消息的内容.
 * app 应当 只是 展示 消息, 以及 处理 用户的点击事件. 并且, 对 点击 跳转到 哪里, 应当 scheme化, 交给 跳由中心 去 跳转处理, 而 不应当 根据 某种type 来 决定 跳转到 哪里.
 * @property msgInfo
 * @constructor
 */

data class MsgInfoListEntity(

    @SerializedName("list")
    val list: List<MSGInfo>?

) : IJsonEntity {

    /**
     *
    参数	类型	描述
    id	long	记录id
    tag	string	消息类型（MsgCenter_CouponRemind:优惠劵提醒，MsgCenter_VSS:云存服务动态，MsgCenter_FCS:4G流量服务动态，MsgCenter_CustomerSrv:客服消息，MsgCenter_CoinsRemind:金币提醒，MsgCenter_Feedback:问题反馈）
    deviceId	long	设备id
    type	int	消息类型, 1: 优惠劵即将过期 2: 优惠劵到账 3: 云服务购买 4 : 云服务即将过期 5: 云服务已经过期 6: 4G购买 7: 4G即将过期 8: 4G已经过期 9: 金币即将过期 10: 签到提醒 11: 问题反馈 12: 客服消息
    title	string	消息标题
    body	string	消息内容
    time	long	消息产生时间（毫秒时间戳）
    redirectUrl	String	覆盖类消息，点击跳转页面地址，为空时不可跳转
     */
    data class MSGInfo(
        @SerializedName("id")
        val id: Long,
        @SerializedName("tag")
        val tag: String,
        @SerializedName("deviceId")
        val deviceId: Long,
        @SerializedName("type")
        val type: Int,
        @SerializedName("title")
        val title: String,
        @SerializedName("body")
        val body: String,
        @SerializedName("time")
        val time: Long,
        @SerializedName("redirectUrl")
        val redirectUrl: String,
    ) : IJsonEntity, Serializable {
        object MsgInfoType {
            /**
             * 优惠劵即将过期
             */
            const val COUPON_EXPIRING = 1

            /**
             * 优惠劵到账
             */
            const val COUPON_ARRIVE = 2

            /**
             * 云服务购买
             */
            const val VSS_PURCHASE = 3

            /**
             * 云服务即将过期
             */
            const val VSS_EXPIRING = 4

            /**
             * 云服务已经过期
             */
            const val VSS_EXPIRED = 5

            /**
             * 4G购买
             */
            const val FCS_PURCHASE = 6

            /**
             * 4G即将过期
             */
            const val FCS_EXPIRING = 7

            /**
             * 4G已经过期
             */
            const val FCS_EXPIRED = 8

            /**
             * 金币即将过期
             */
            const val COINS_EXPIRING = 9

            /**
             * 签到提醒
             */
            const val COINS_SIGN = 10

            /**
             * 问题反馈
             */
            const val FEEDBACK = 11

            /**
             * 客服消息
             */
            const val CUST_SRV = 12

            /**
             * 免广告提醒消息
             */
            const val ELIMINATE_ADV = 13

            /**
             * 分享访客提醒消息
             */
            const val SHARE_GUEST_ARRIVE = 14

            /**
             * 分享权限修改提醒消息
             */
            const val SHARE_PERMISSION_MODIFY = 15

            /**
             * 分享访客确认
             */
            const val SHARE_GUEST_CONFIRM = 16

            /**
             * 分享访客删除
             */
            const val SHARE_GUEST_DELETE = 17

            /**
             * 分享通知访客（无需访客确认版本）
             */
            const val SHARE_TO = 18
        }
    }

}