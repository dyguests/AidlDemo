package com.fanhl.aidldemo.shared;

import com.fanhl.aidldemo.shared.Msg;
import com.fanhl.aidldemo.shared.IReceiveMsgListener;

interface IMsgManager {
    void sendMsg(in Msg msg);
    void registerReceiveListener(IReceiveMsgListener receiveListener);
    void unregisterReceiveListener(IReceiveMsgListener receiveListener);
}
