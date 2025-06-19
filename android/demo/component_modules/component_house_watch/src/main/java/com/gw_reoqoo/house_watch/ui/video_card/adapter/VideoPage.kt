package com.gw_reoqoo.house_watch.ui.video_card.adapter

import android.os.Parcel
import android.os.Parcelable
import com.gw_reoqoo.house_watch.entities.DevicePack


/**
 * 视频显示的包装封闭类
 */
interface VideoPage : Parcelable {
    val devices: List<DevicePack>
    val getId: Int

    /**
     * 单个显示模式
     *
     * @param device 需要显示视频的device
     */
    class SinglePage(val device: DevicePack) : VideoPage, Parcelable {
        override val devices: List<DevicePack> get() = listOf(device)
        override val getId: Int
            get() {
                val deviceStr = buildString {
                    append("VideoPage.SinglePage-")
                    append("\n")
                    devices.map {
                        append(it.offView)
                        append("-")
                        append(it.deviceId)
                        append("-")
                        append(it.remarkName)
                        append("-")
                        append(it.powerOn)
                        append("-")
                        append(it.isOnline)
                        append("\n")
                    }
                }
                return deviceStr.hashCode()
            }

        constructor(parcel: Parcel) : this(DevicePack(parcel.readBundle()))

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeBundle(device.toBundle())
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SinglePage> {
            override fun createFromParcel(parcel: Parcel): SinglePage {
                return SinglePage(parcel)
            }

            override fun newArray(size: Int): Array<SinglePage?> {
                return arrayOfNulls(size)
            }
        }
    }

    /**
     * 多个显示模式
     *
     * @param ltDevice 左上角的视频对应的device
     * @param rtDevice 右上角的视频对应的device
     * @param lbDevice 左下角的视频对应的device
     * @param rbDevice 右下角的视频对应的device
     */
    class MultiPage(
        val ltDevice: DevicePack,
        val rtDevice: DevicePack?,
        val lbDevice: DevicePack?,
        val rbDevice: DevicePack?,
    ) : VideoPage, Parcelable {

        override val devices: List<DevicePack>
            get() = listOfNotNull(
                ltDevice,
                rtDevice,
                lbDevice,
                rbDevice,
            )
        override val getId: Int
            get() {
                val deviceStr = buildString {
                    append("VideoPage.MultiPage-")
                    append("\n")
                    devices.map {
                        append(it.offView)
                        append("-")
                        append(it.deviceId)
                        append("-")
                        append(it.remarkName)
                        append("-")
                        append(it.powerOn)
                        append("-")
                        append(it.isOnline)
                        append("\n")
                    }
                }
                return deviceStr.hashCode()
            }

        constructor(parcel: Parcel) : this(
            DevicePack(parcel.readBundle()),
            parcel.readBundle()?.let(::DevicePack),
            parcel.readBundle()?.let(::DevicePack),
            parcel.readBundle()?.let(::DevicePack),
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeBundle(ltDevice.toBundle())
            parcel.writeBundle(rtDevice?.toBundle())
            parcel.writeBundle(lbDevice?.toBundle())
            parcel.writeBundle(rbDevice?.toBundle())
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<MultiPage> {
            override fun createFromParcel(parcel: Parcel): MultiPage {
                return MultiPage(parcel)
            }

            override fun newArray(size: Int): Array<MultiPage?> {
                return arrayOfNulls(size)
            }
        }
    }
}