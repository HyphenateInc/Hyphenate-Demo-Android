package com.hyphenate.chatuidemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by benson on 2016/11/3.
 */

public class BroadCastReceiverManager {

    private static BroadCastReceiverManager broadCastReceiverManager;
    private static LocalBroadcastManager localBroadcastManager;
    private static LocalBroadCastReceiver receiver;
    private static DefaultLocalBroadCastReceiver defaultLocalBroadCastReceiver;

    public static BroadCastReceiverManager getInstance(Context context) {

        if (broadCastReceiverManager == null) {
            broadCastReceiverManager = new BroadCastReceiverManager();
        }

        if (localBroadcastManager == null) {
            localBroadcastManager = LocalBroadcastManager.getInstance(context);
        }

        if (receiver == null) {
            receiver = new LocalBroadCastReceiver();
        }

        return broadCastReceiverManager;
    }

    public void setDefaultLocalBroadCastReceiver(DefaultLocalBroadCastReceiver receiver) {
        defaultLocalBroadCastReceiver = receiver;
    }

    static class LocalBroadCastReceiver extends BroadcastReceiver {

        @Override public void onReceive(Context context, Intent intent) {

            if (defaultLocalBroadCastReceiver != null) {
                defaultLocalBroadCastReceiver.defaultOnReceive(context, intent);
            }
        }
    }


    /**
     * Send local broadcast
     *
     * @param action Broadcast action, the receiver can be filtered according to this action
     */

    public void sendBroadCastReceiver(String action) {
        Intent intent = new Intent(action);
        localBroadcastManager.sendBroadcast(intent);
    }

    public void registerBroadCastReceiver(String action) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        localBroadcastManager.registerReceiver(receiver, filter);
    }

    public void unRegisterBroadCastReceiver() {
        localBroadcastManager.unregisterReceiver(receiver);
    }

    public interface DefaultLocalBroadCastReceiver {

        void defaultOnReceive(Context context, Intent intent);
    }
}
