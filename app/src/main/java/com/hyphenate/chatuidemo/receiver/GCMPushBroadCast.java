package com.hyphenate.chatuidemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.hyphenate.chatuidemo.DemoHelper;

public class GCMPushBroadCast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("info", "gcmpush onreceive");
		String alert = intent.getStringExtra("alert");
		DemoHelper.getInstance().getNotifier().onNewMsg(alert);
	}

}
