package com.fanhl.aidldemo.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.os.IBinder.DeathRecipient
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.fanhl.aidldemo.IMsgManager
import com.fanhl.aidldemo.IReceiveMsgListener
import com.fanhl.aidldemo.Msg
import com.fanhl.aidldemo.client.MyService.MyBinder
import java.util.*

class MainActivity : AppCompatActivity() {
    var binder: MyBinder? = null
    var mConnection: ServiceConnection? = null
    private var mListView: RecyclerView? = null
    private var mEditText: EditText? = null
    private val mMsgs: MutableList<Msg> = ArrayList()
    private var mAdapter: MainAdapter? = null
    private var mIMsgManager: IMsgManager? = null

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            mAdapter!!.setNewData(mMsgs)
            mAdapter!!.notifyDataSetChanged()
        }
    }

    private val mReceiveMsgListener: IReceiveMsgListener = object : IReceiveMsgListener.Stub() {
        @Throws(RemoteException::class)
        override fun onReceive(msg: Msg) {
            msg.time = System.currentTimeMillis()
            mMsgs.add(msg)
            mHandler.sendEmptyMessage(1)
        }
    }
    private val mDeathRecipient: DeathRecipient = object : DeathRecipient {
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
        mListView = findViewById<View>(R.id.recycler_view) as RecyclerView
        mEditText = findViewById<View>(R.id.et_msg) as EditText
        mAdapter = MainAdapter()
        mListView!!.adapter = mAdapter
        mConnection = object : ServiceConnection {
            override fun onServiceConnected(
                componentName: ComponentName,
                iBinder: IBinder
            ) {
                binder = iBinder as MyBinder
                val msgManager = IMsgManager.Stub.asInterface(iBinder)
                mIMsgManager = msgManager
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
        val intent = Intent(this@MainActivity, MyService::class.java)
        bindService(intent, mConnection as ServiceConnection, Context.BIND_AUTO_CREATE) //开启服务
        findViewById<View>(R.id.btn_send).setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(mEditText!!.text.toString())) {
                Toast.makeText(this@MainActivity, "消息为空", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            binder!!.sendMsg(Msg(mEditText!!.text.toString().trim { it <= ' ' }))
        })
        findViewById<View>(R.id.btn_exit).setOnClickListener { finish() }
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
        unbindService(mConnection!!)
        super.onDestroy()
    }
}