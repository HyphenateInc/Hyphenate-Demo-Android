package com.hyphenate.chatuidemo.call;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.ui.BaseActivity;

/**
 * Created by lzan13 on 2016/8/8.
 *
 * call super activity
 */
public class CallActivity extends BaseActivity {

    protected CallActivity activity;
    protected int OVERLAY_PERMISSION_REQUEST_CODE = 1001;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    /**
     * init ui
     */
    protected void initView() {
        activity = this;

        initCallPushProvider();

        if (CallManager.getInstance().getCallState() == CallManager.CallState.DISCONNECTED) {
            CallManager.getInstance().setCallState(CallManager.CallState.CONNECTING);
            CallManager.getInstance().registerCallStateListener();
            CallManager.getInstance().attemptPlayCallSound();

            if (!CallManager.getInstance().isInComingCall()) {
                CallManager.getInstance().makeCall();
            }
        }
    }

    /**
     * Init call push provider
     */
    private void initCallPushProvider() {
        CallPushProvider pushProvider = new CallPushProvider();
        EMClient.getInstance().callManager().setPushProvider(pushProvider);
    }

    /**
     * End call
     */
    protected void endCall() {
        CallManager.getInstance().endCall();
        onFinish();
    }

    /**
     * Reject call
     */
    protected void rejectCall() {
        CallManager.getInstance().rejectCall();
        onFinish();
    }

    /**
     * Answer call
     */
    protected void answerCall() {
        CallManager.getInstance().answerCall();
    }

    /**
     * check alert window permission
     */
    protected boolean ackForFloatWindowPermission() {
        boolean result = true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                result = false;
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Check float qindow permission");
                builder.setMessage(
                        "Application does not have the suspension window permission, need to manually open, is it open? Open the next time effective!");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }else{
                result = true;
            }
        }
        return result;
    }

    protected void onFinish() {
        activity.finish();
    }

    @Override protected void onResume() {
        if (CallManager.getInstance().getCallState() == CallManager.CallState.DISCONNECTED) {
            onFinish();
            return;
        } else {
            CallManager.getInstance().removeFloatWindow();
            CallManager.getInstance().cancelCallNotification();
        }
        super.onResume();
    }
}
