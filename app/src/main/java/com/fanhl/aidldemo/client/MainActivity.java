package com.fanhl.aidldemo.client;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.fanhl.aidldemo.IMsgManager;
import com.fanhl.aidldemo.IReceiveMsgListener;
import com.fanhl.aidldemo.Msg;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    MyService.MyBinder binder = null;
    ServiceConnection mConnection;
    private RecyclerView mListView;
    private EditText mEditText;
    private List<Msg> mMsgs = new ArrayList<>();
    private MainAdapter mAdapter;
    private IMsgManager mIMsgManager;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mAdapter.setNewData(mMsgs);
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (RecyclerView) findViewById(R.id.recycler_view);
        mEditText = (EditText) findViewById(R.id.et_msg);
        mAdapter = new MainAdapter();
        mListView.setAdapter(mAdapter);

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                binder = (MyService.MyBinder) iBinder;
                IMsgManager msgManager = IMsgManager.Stub.asInterface(iBinder);
                mIMsgManager = msgManager;
                try {
                    mIMsgManager.asBinder().linkToDeath(mDeathRecipient, 0);
                    mIMsgManager.registerReceiveListener(mReceiveMsgListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };

        //注意Activity和Service是同一进程才能使用Intent通信
        Intent intent = new Intent(MainActivity.this, MyService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);//开启服务

        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mEditText.getText().toString())) {
                    Toast.makeText(MainActivity.this, "消息为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                binder.sendMsg(new Msg(mEditText.getText().toString().trim()));
            }
        });
        findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finish();
            }
        });
    }

    private IReceiveMsgListener mReceiveMsgListener = new IReceiveMsgListener.Stub() {

        @Override
        public void onReceive(Msg msg) throws RemoteException {
            msg.setTime(System.currentTimeMillis());
            mMsgs.add(msg);
            mHandler.sendEmptyMessage(1);
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        //当承载IBinder的进程消失时接收回调的接口
        @Override
        public void binderDied() {
            if (null == mIMsgManager) {
                return;
            }
            mIMsgManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mIMsgManager = null;
            //断线重来逻辑
        }
    };

    @Override
    protected void onDestroy() {
        //解除注册
        if (null != mIMsgManager && mIMsgManager.asBinder().isBinderAlive()) {
            try {
                mIMsgManager.unregisterReceiveListener(mReceiveMsgListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        //解除绑定服务
        unbindService(mConnection);
        super.onDestroy();
    }
}