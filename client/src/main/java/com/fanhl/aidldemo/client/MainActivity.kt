package com.fanhl.aidldemo.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.os.IBinder.DeathRecipient
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fanhl.aidldemo.shared.IMsgManager
import com.fanhl.aidldemo.shared.IReceiveMsgListener
import com.fanhl.aidldemo.shared.MainAdapter
import com.fanhl.aidldemo.shared.Msg
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var myBinder: IMsgManager? = null //定义AIDL
    private val mMsgs: MutableList<Msg> = ArrayList()
    private lateinit var mAdapter: MainAdapter
    private var mIMsgManager: IMsgManager? = null
    private var mMsg: Msg? = null

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                1 -> {
                    mAdapter.setNewData(mMsgs)
                    mAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private var mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myBinder = IMsgManager.Stub.asInterface(iBinder)
            val msgManager = IMsgManager.Stub.asInterface(iBinder)
            mIMsgManager = msgManager
            try {
                //链接到死亡代理，当IBinder死亡时收到回调
                mIMsgManager!!.asBinder().linkToDeath(mDeathRecipient, 0)
                //注册消息监听
                mIMsgManager!!.registerReceiveListener(mReceiveMsgListener)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }


    //消息回调监听
    private val mReceiveMsgListener: IReceiveMsgListener = object : IReceiveMsgListener.Stub() {
        //收到服务端消息
        @Throws(RemoteException::class)
        override fun onReceive(msg: Msg) {
            msg.time = System.currentTimeMillis()
            if (mMsgs.size > 100) {
                mMsgs.clear()
            }
            mMsgs.add(msg)
            mHandler.sendEmptyMessage(1)
        }
    }

    private val mDeathRecipient: DeathRecipient = object : DeathRecipient {
        /**
         * 当承载IBinder的进程消失时接收回调的接口
         */
        override fun binderDied() {
            if (null == mIMsgManager) {
                return
            }
            mIMsgManager!!.asBinder().unlinkToDeath(this, 0)
            mIMsgManager = null
            //在这里重新绑定远程服务
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_send.setOnClickListener {
            try {
                val msg = et_msg!!.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(this@MainActivity, "消息不能为空", Toast.LENGTH_SHORT).show()
                } else {
                    mMsg!!.msg = msg
                    //通过binder将消息传递到service
                    myBinder!!.sendMsg(mMsg)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        mMsg = Msg("")
        mAdapter = MainAdapter()
        recycler_view.adapter = mAdapter


        val intent = Intent()
        //跨进程通信需要使用action启动
        intent.action = "com.fanhl.aidldemo.server.MyService"
        //android5.0之后，如果servicer不在同一个App的包中，需要设置service所在程序的包名
        intent.setPackage("com.fanhl.aidldemo.server")
        //开启Service
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        //解绑
        super.onDestroy()
    }
}