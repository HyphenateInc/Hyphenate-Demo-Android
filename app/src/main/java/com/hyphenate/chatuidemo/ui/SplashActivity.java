package com.hyphenate.chatuidemo.ui;

import android.content.Intent;
import android.os.Bundle;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.call.VideoCallActivity;
import com.hyphenate.chatuidemo.call.VoiceCallActivity;
import com.hyphenate.chatuidemo.sign.SignInActivity;
import com.hyphenate.util.EasyUtils;

/**
 * Created by lzan13 on 2016/11/14.
 */

public class SplashActivity extends BaseActivity {
    private static final int sleepTime = 2000;

    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        if (EMClient.getInstance().isLoggedInBefore()) {
            String topActivityName =
                    EasyUtils.getTopActivityName(EMClient.getInstance().getContext());
            if (topActivityName != null && (topActivityName.equals(
                    VideoCallActivity.class.getName()) || topActivityName.equals(
                    VoiceCallActivity.class.getName()))) {
                // nop
                // avoid main screen overlap Calling Activity
            } else {
                //enter main screen
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish();
        } else {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
            }
            startActivity(new Intent(SplashActivity.this, SignInActivity.class));
            finish();
        }
    }
}
