package com.fanhl.aidldemo

import android.os.Parcel
import android.os.Parcelable

class Msg : Parcelable {
    var msg: String? = null
    var time: Long = 0

    constructor(msg: String?, time: Long = 0L) {
        this.msg = msg
        this.time = time
    }

    constructor(input: Parcel) {
        msg = input.readString()
        time = input.readLong()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(msg)
        dest.writeLong(time)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Msg?> = object : Parcelable.Creator<Msg?> {
            override fun createFromParcel(source: Parcel): Msg? {
                return Msg(source)
            }

            override fun newArray(size: Int): Array<Msg?> {
                return arrayOfNulls(size)
            }
        }
    }
}