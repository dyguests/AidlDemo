package com.fanhl.aidldemo;

import com.fanhl.aidldemo.Msg;
import com.fanhl.aidldemo.IReceiveMsgListener;

interface IMsgManager {
    void sendMsg(in Msg msg);
    void registerReceiveListener(IReceiveMsgListener receiveListener);
    void unregisterReceiveListener(IReceiveMsgListener receiveListener);
}
