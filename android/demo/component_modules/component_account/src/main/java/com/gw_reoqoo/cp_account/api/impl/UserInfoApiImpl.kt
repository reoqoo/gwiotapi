package com.gw_reoqoo.cp_account.api.impl

import android.os.Parcel
import android.os.Parcelable
import com.gw_reoqoo.cp_account.api.kapi.IUserInfo
import com.gw_reoqoo.lib_room.user.UserInfo
import com.gw_reoqoo.lib_utils.InsensitiveUtils
import com.gw_reoqoo.lib_utils.ktx.isEmptyOrZero

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/15 10:40
 * Description: 用户信息接口实现类
 */
class UserInfoApiImpl(
    override val userId: String,
    override val terminalId: String,
    override val email: String?,
    override val phone: String?,
    override val nickName: String?,
    override val accessId: String,
    override val accessToken: String,
    override val expireTime: String,
    override val area: String?,
    override val headUrl: String?,
    override val firstLogin: Boolean?,
    override val unionIdToken: String?,
    override val hasBindAccount: Boolean?,
    override val showId: String?,
    override val regRegion: String?
) : IUserInfo, Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString()
    ) {
    }

    constructor(info: UserInfo) : this(
        info.id,
        info.terminalId,
        info.email,
        info.phone,
        info.nickname,
        info.accessId,
        info.accessToken,
        info.expireTime,
        info.area,
        info.headUrl,
        info.firstLogin,
        info.unionIdToken,
        info.hasBindAccount,
        info.showId,
        info.regRegion
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(terminalId)
        parcel.writeString(email)
        parcel.writeString(phone)
        parcel.writeString(nickName)
        parcel.writeString(accessId)
        parcel.writeString(accessToken)
        parcel.writeString(expireTime)
        parcel.writeString(area)
        parcel.writeString(headUrl)
        parcel.writeValue(firstLogin)
        parcel.writeString(unionIdToken)
        parcel.writeValue(hasBindAccount)
        parcel.writeValue(showId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserInfoApiImpl> {
        override fun createFromParcel(parcel: Parcel): UserInfoApiImpl {
            return UserInfoApiImpl(parcel)
        }

        override fun newArray(size: Int): Array<UserInfoApiImpl?> {
            return arrayOfNulls(size)
        }
    }

    override fun getInsensitiveName(withNickName: Boolean): String {
        val _nickName = nickName ?: ""
        if (_nickName.isNotEmpty()) {
            if (withNickName && _nickName.length > 6) {
                return _nickName.substring(0, 6) + "..."
            }
            return _nickName
        }
        val mobile = phone ?: ""
        if (!mobile.isEmptyOrZero()) {
            return InsensitiveUtils.mobileEncrypt(mobile)
        }
        val _email = email ?: ""
        if (!_email.isEmptyOrZero()) {
            return InsensitiveUtils.desensitizedEmail(_email)
        }
        return ""
    }

    override fun toString(): String {
        return "UserInfoApiImpl(userId='$userId', terminalId='$terminalId', email=$email, phone=$phone, nickName=$nickName, accessId='$accessId', accessToken='$accessToken', expireTime='$expireTime', area=$area, headUrl=$headUrl, firstLogin=$firstLogin, unionIdToken=$unionIdToken, hasBindAccount=$hasBindAccount, showId=$showId)"
    }
}