// IReceiveMsgListener.aidl
package com.fanhl.aidldemo.shared;

import com.fanhl.aidldemo.shared.Msg;

interface IReceiveMsgListener {
   void onReceive(in Msg msg);
}
