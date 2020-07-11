// IReceiveMsgListener.aidl
package com.fanhl.aidldemo;

import com.fanhl.aidldemo.Msg;

interface IReceiveMsgListener {
   void onReceive(in Msg msg);
}
