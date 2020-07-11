package com.fanhl.aidldemo;

import android.os.Parcel;
import android.os.Parcelable;

public class Msg implements Parcelable {

    private String msg;
    private long time;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.msg);
        dest.writeLong(this.time);
    }

    public Msg() {
    }

    public Msg(String msg) {
        this.msg = msg;
    }

    public Msg(String msg, long time) {
        this.msg = msg;
        this.time = time;
    }

    public Msg(Parcel in) {
        this.msg = in.readString();
        this.time = in.readLong();
    }

    public static final Creator<Msg> CREATOR = new Creator<Msg>() {
        @Override
        public Msg createFromParcel(Parcel source) {
            return new Msg(source);
        }

        @Override
        public Msg[] newArray(int size) {
            return new Msg[size];
        }
    };
}