package io.agora.chatdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import io.agora.chat.ChatClient;
import io.agora.chatdemo.sign.SignInActivity;

/**
 * Created by lzan13 on 2016/11/14.
 */

public class SplashActivity extends BaseActivity {
    //private static final int sleepTime = 2000;

    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        if (ChatClient.getInstance().isLoggedInBefore()) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        } else {
            //try {
            //    Thread.sleep(sleepTime);
            //} catch (InterruptedException e) {
            //}
            startActivity(new Intent(SplashActivity.this, SignInActivity.class));
            finish();
        }
    }
}
