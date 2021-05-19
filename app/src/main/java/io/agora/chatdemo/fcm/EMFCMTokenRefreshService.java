package io.agora.chatdemo.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import io.agora.chat.ChatClient;

/**
 * Created by zhangsong on 17-9-15.
 */
public class EMFCMTokenRefreshService extends FirebaseInstanceIdService {
    private static final String TAG = "FCMTokenRefreshService";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, "onTokenRefresh: " + token);
        // Important, send the fcm token to the hyphenate server
        // You should upgrade the sdk version to 3.3.5 or later.
        ChatClient.getInstance().sendFCMTokenToServer(token);
    }
}
