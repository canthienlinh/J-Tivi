package com.jtv.app

import android.os.Parcel
import android.os.Parcelable

/** Lightweight Parcelable used to hand the list + resolved headers to PlayerActivity. */
data class ChannelParcel(
    val name: String,
    val url: String,
    val userAgent: String?,
    val referer: String?,
    val isDash: Boolean
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString().orEmpty(),
        parcel.readString().orEmpty(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(url)
        dest.writeString(userAgent)
        dest.writeString(referer)
        dest.writeByte(if (isDash) 1 else 0)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ChannelParcel> {
            override fun createFromParcel(parcel: Parcel) = ChannelParcel(parcel)
            override fun newArray(size: Int): Array<ChannelParcel?> = arrayOfNulls(size)
        }

        fun from(c: Channel) = ChannelParcel(c.name, c.url, c.userAgent, c.referer, c.isDash)
    }
}
