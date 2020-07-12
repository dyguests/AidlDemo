package com.fanhl.aidldemo.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log
import com.fanhl.aidldemo.shared.IMsgManager
import com.fanhl.aidldemo.shared.IReceiveMsgListener
import com.fanhl.aidldemo.shared.Msg

class MyService : Service() {
    //AIDL不支持正常的接口回调，使用RemoteCallbackList实现接口回调
    private val mReceiveListener = RemoteCallbackList<IReceiveMsgListener>()

    override fun onBind(intent: Intent): IBinder? {
        return MyBinder()
    }

    inner class MyBinder : IMsgManager.Stub() {
        //发送消息
        override fun sendMsg(msg: Msg) {
            receiveMsg(msg)
        }

        //注册
        @Throws(RemoteException::class)
        override fun registerReceiveListener(receiveListener: IReceiveMsgListener) {
            mReceiveListener.register(receiveListener)
        }

        //解除注册
        @Throws(RemoteException::class)
        override fun unregisterReceiveListener(receiveListener: IReceiveMsgListener) {
            val success = mReceiveListener.unregister(receiveListener)
            if (success) {
                Log.d(TAG, "===  解除注册成功")
            } else {
                Log.d(TAG, "===  解除注册失败 ")
            }
        }

        @Throws(RemoteException::class)
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            return super.onTransact(code, data, reply, flags)
        }
    }

    //收到消息处理
    fun receiveMsg(msg: Msg) {
        //通知Callback循环开始,返回N为实现mReceiveListener回调的个数
        val N = mReceiveListener.beginBroadcast()
        msg.msg = "我是服务器，我收到了：" + msg.msg
        for (i in 0 until N) {
            val listener = mReceiveListener.getBroadcastItem(i)
            if (listener != null) {
                try {
                    listener.onReceive(msg)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }
        //通知通知Callback循环结束
        mReceiveListener.finishBroadcast()
    }

    companion object {
        const val TAG = "MyService"
    }
}