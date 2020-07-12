package com.fanhl.aidldemo.server

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import com.fanhl.aidldemo.shared.IMsgManager
import com.fanhl.aidldemo.shared.IReceiveMsgListener
import com.fanhl.aidldemo.shared.MainAdapter
import com.fanhl.aidldemo.shared.Msg
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var binder: MyService.MyBinder? = null
    private lateinit var mConnection: ServiceConnection
    private val mMsgs: MutableList<Msg> = ArrayList()
    private lateinit var mAdapter: MainAdapter
    private var mIMsgManager: IMsgManager? = null

    private val mHandler: Handler = Handler(Looper.getMainLooper()) {
        mAdapter.setNewData(mMsgs)
        mAdapter.notifyDataSetChanged()
        true
    }

    private val mReceiveMsgListener: IReceiveMsgListener = object : IReceiveMsgListener.Stub() {
        @Throws(RemoteException::class)
        override fun onReceive(msg: Msg) {
            msg.time = System.currentTimeMillis()
            mMsgs.add(msg)
            mHandler.sendEmptyMessage(1)
        }
    }
    private val mDeathRecipient: IBinder.DeathRecipient = object : IBinder.DeathRecipient {
        //当承载IBinder的进程消失时接收回调的接口
        override fun binderDied() {
            if (null == mIMsgManager) {
                return
            }
            mIMsgManager!!.asBinder().unlinkToDeath(this, 0)
            mIMsgManager = null
            //断线重来逻辑
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_send.setOnClickListener { binder?.sendMsg(Msg(et_msg.text.toString().trim { it <= ' ' })) }
        btn_exit.setOnClickListener { finish() }

        mAdapter = MainAdapter()
        recycler_view.adapter = mAdapter

        mConnection = object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
                binder = iBinder as MyService.MyBinder
                mIMsgManager = IMsgManager.Stub.asInterface(iBinder)
                try {
                    mIMsgManager!!.asBinder().linkToDeath(mDeathRecipient, 0)
                    mIMsgManager!!.registerReceiveListener(mReceiveMsgListener)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }

            override fun onServiceDisconnected(componentName: ComponentName) {}
        }

        //注意Activity和Service是同一进程才能使用Intent通信
        bindService(Intent(this@MainActivity, MyService::class.java), mConnection, Context.BIND_AUTO_CREATE) //开启服务
    }

    override fun onDestroy() {
        //解除注册
        if (null != mIMsgManager && mIMsgManager!!.asBinder().isBinderAlive) {
            try {
                mIMsgManager!!.unregisterReceiveListener(mReceiveMsgListener)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
        //解除绑定服务
        unbindService(mConnection)
        super.onDestroy()
    }
}
