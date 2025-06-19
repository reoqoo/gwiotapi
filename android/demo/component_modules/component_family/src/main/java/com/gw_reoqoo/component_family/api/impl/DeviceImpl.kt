package com.gw_reoqoo.component_family.api.impl

import android.os.Parcel
import android.os.Parcelable
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.lib_room.device.DeviceInfo
import com.gw_reoqoo.lib_room.ktx.isMaster
import com.gw_reoqoo.lib_utils.ktx.bitAt

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 */
data class DeviceImpl(
    override val deviceId: String,
    override val userId: String,
    override val remarkName: String?,
    override val relation: Int,
    override val permission: Int,
    override val modifyTime: String,
    override val isMaster: Boolean,
    override val online: Int?,
    override val powerOn: Boolean?,
    override val productId: String?,
    override val sn: String?,
    override val hasShared: Boolean?,
    override val originJson: String?
) : IDevice, Parcelable {
    constructor(parcel: Parcel) : this(
        deviceId = parcel.readString() ?: "",
        userId = parcel.readString() ?: "",
        remarkName = parcel.readString(),
        relation = parcel.readInt(),
        permission = parcel.readInt(),
        modifyTime = parcel.readString() ?: "",
        isMaster = parcel.readByte() != 0.toByte(),
        online = parcel.readValue(Int::class.java.classLoader) as? Int,
        powerOn = parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        productId = parcel.readString(),
        sn = parcel.readString(),
        hasShared = parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        originJson = parcel.readString()
    )

    constructor(info: DeviceInfo) : this(
        deviceId = info.deviceId,
        userId = info.userId,
        remarkName = info.remarkName,
        relation = info.relation,
        permission = info.permission,
        modifyTime = info.modifyTime,
        isMaster = info.isMaster,
        online = info.online,
        powerOn = info.powerOn,
        productId = info.productId,
        sn = info.sn,
        hasShared = info.status?.bitAt(1) == 1,
        originJson = info.originJson
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(deviceId)
        parcel.writeString(userId)
        parcel.writeString(remarkName)
        parcel.writeInt(relation)
        parcel.writeInt(permission)
        parcel.writeString(modifyTime)
        parcel.writeByte(if (isMaster) 1 else 0)
        parcel.writeValue(online)
        parcel.writeValue(powerOn)
        parcel.writeString(productId)
        parcel.writeString(sn)
        parcel.writeValue(hasShared)
        parcel.writeString(originJson)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DeviceImpl> {
        override fun createFromParcel(parcel: Parcel): DeviceImpl {
            return DeviceImpl(parcel)
        }

        override fun newArray(size: Int): Array<DeviceImpl?> {
            return arrayOfNulls(size)
        }
    }
}