package io.agora.chatdemo.fcm;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.agora.chat.ChatClient;
import io.agora.chatdemo.DemoHelper;
import io.agora.util.EMLog;

/**
 * Created by zhangsong on 17-9-15.
 */
public class EMFCMMSGService extends FirebaseMessagingService {
    private static final String TAG = "EMFCMMSGService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {
            String message = remoteMessage.getData().get("alert");
            EMLog.i(TAG, "onMessageReceived: " + message);
            DemoHelper.getInstance().getNotifier().onNewMsg(message);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        EMLog.i(TAG, "onTokenRefresh: " + token);
        // Important, send the fcm token to the hyphenate server
        // You should upgrade the sdk version to 3.3.5 or later.
        ChatClient.getInstance().sendFCMTokenToServer(token);
    }
}
