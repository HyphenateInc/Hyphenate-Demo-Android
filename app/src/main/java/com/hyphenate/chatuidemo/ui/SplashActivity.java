package com.hyphenate.chatuidemo.ui;

import android.content.Intent;
import android.os.Bundle;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.sign.SignInActivity;

/**
 * Created by lzan13 on 2016/11/14.
 */

public class SplashActivity extends BaseActivity {
    //private static final int sleepTime = 2000;

    @Override protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        if (EMClient.getInstance().isLoggedInBefore()) {
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
