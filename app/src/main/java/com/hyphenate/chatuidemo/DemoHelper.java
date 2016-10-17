package com.hyphenate.chatuidemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chatuidemo.ui.chat.call.CallReceiver;
import com.hyphenate.chatuidemo.ui.chat.call.CallStateChangeListener;
import com.hyphenate.util.EMLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wei on 2016/10/11.
 * Demo app helper class
 */
public class DemoHelper {

    protected static final String TAG = DemoHelper.class.getSimpleName();
    // context
    private Context mContext;

    //
    private static DemoHelper instance;

    // Call broadcast receiver
    private CallReceiver mCallReceiver = null;
    // Call state listener
    private CallStateChangeListener mCallStateChangeListener = null;

    // connection listener
    private EMConnectionListener mConnectionListener;

    private EMMessageListener messageListener = null;
    /**
     * save foreground Activity which registered message listeners
     */
    private List<Activity> activityList = new ArrayList<Activity>();

    private DemoHelper() {
    }

    public synchronized static DemoHelper getInstance() {
        if (instance == null) {
            instance = new DemoHelper();
        }
        return instance;
    }

    /**
     * init helper
     *
     * @param context application context
     */
    public void init(Context context) {
        mContext = context;

        if (isMainProcess()) {
            EMLog.d(TAG, "------- init hyphenate start --------------");
            //init hyphenate sdk with options
            EMClient.getInstance().init(context, initOptions());

            // set debug mode open:true, close:false
            EMClient.getInstance().setDebugMode(true);

            setGlobalListener();

            EMLog.d(TAG, "------- init hyphenate end --------------");
        }
    }

    /**
     * init sdk options
     */
    private EMOptions initOptions() {
        // set init sdk options
        EMOptions options = new EMOptions();
        // change to need confirm contact invitation
        options.setAcceptInvitationAlways(false);
        // set if need read ack
        options.setRequireAck(true);
        // set if need delivery ack
        options.setRequireDeliveryAck(false);

        //set gcm project number
        options.setGCMNumber("998166487724");

        return options;
    }

    /**
     * init global listener
     */
    public void setGlobalListener() {
        // set call listener
        setCallReceiverListener();
        // set connection listener
        setConnectionListener();

        registerMessageListener();
    }

    /**
     * Set call broadcast listener
     */
    private void setCallReceiverListener() {
        // Set the call broadcast listener to filter the action
        IntentFilter callFilter = new IntentFilter(
                EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        if (mCallReceiver == null) {
            mCallReceiver = new CallReceiver();
        }
        // Register the call receiver
        mContext.registerReceiver(mCallReceiver, callFilter);
    }

    /**
     * Add call state listener
     */
    public void addCallStateChangeListener() {
        if (mCallStateChangeListener == null) {
            mCallStateChangeListener = new CallStateChangeListener();
        }
        EMClient.getInstance().callManager().addCallStateChangeListener(mCallStateChangeListener);
    }

    /**
     * Remove call state listener
     */
    public void removeCallStateChangeListener() {
        if (mCallStateChangeListener != null) {
            EMClient.getInstance()
                    .callManager()
                    .removeCallStateChangeListener(mCallStateChangeListener);
            mCallStateChangeListener = null;
        }
    }

    /**
     * Set Connection Listener
     */
    private void setConnectionListener() {
        mConnectionListener = new EMConnectionListener() {

            /**
             * The connection to the server is successful
             */
            @Override public void onConnected() {
                EMLog.d(TAG, "onConnected");
            }

            /**
             * Disconnected from the server
             *
             * @param errorCode Disconnected error code
             */
            @Override public void onDisconnected(final int errorCode) {
                EMLog.d(TAG, "onDisconnected: " + errorCode);
            }
        };
        EMClient.getInstance().addConnectionListener(mConnectionListener);
    }

    /**
     * new messages listener
     * If this event already handled by an activity, you don't need handle it again
     * activityList.size() <= 0 means all activities already in background or not in Activity Stack
     */
    protected void registerMessageListener() {
        messageListener = new EMMessageListener() {
            private BroadcastReceiver broadCastReceiver = null;

            @Override public void onMessageReceived(List<EMMessage> messages) {
                for (EMMessage message : messages) {
                    EMLog.d(TAG, "onMessageReceived id : " + message.getMsgId());
                    // in background, do not refresh UI, notify it in notification bar
                    if (hasForegroundActivies()) {
                        //getNotifier().onNewMsg(message);
                    }
                }
            }

            @Override public void onCmdMessageReceived(List<EMMessage> messages) {
                for (EMMessage message : messages) {
                    EMLog.d(TAG, "onCmdMessageReceived");
                    //get message body
                    EMCmdMessageBody cmdMsgBody = (EMCmdMessageBody) message.getBody();
                    final String action = cmdMsgBody.action();//获取自定义action

                    //get extension attribute if you need
                    //message.getStringAttribute("");
                    EMLog.d(TAG, String.format("CmdMessage：action:%s,message:%s", action,
                            message.toString()));
                }
            }

            @Override public void onMessageRead(List<EMMessage> messages) {
            }

            @Override public void onMessageDelivered(List<EMMessage> message) {
            }

            @Override public void onMessageChanged(EMMessage message, Object change) {

            }
        };

        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    public boolean hasForegroundActivies() {
        return activityList.size() != 0;
    }

    public void pushActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(0, activity);
        }
    }

    public void popActivity(Activity activity) {
        activityList.remove(activity);
    }

    /**
     * Sign out account
     *
     * @param callback to receive the result of the logout
     */
    public void signOut(boolean unbindDeviceToken, final EMCallBack callback) {
        Log.d(TAG, "Sign out: " + unbindDeviceToken);
        EMClient.getInstance().logout(unbindDeviceToken, new EMCallBack() {

            @Override public void onSuccess() {
                Log.d(TAG, "Sign out: onSuccess");
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override public void onProgress(int progress, String status) {
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }

            @Override public void onError(int code, String error) {
                Log.d(TAG, "Sign out: onSuccess");
                if (callback != null) {
                    callback.onError(code, error);
                }
            }
        });
    }

    /**
     * check the application process name if process name is not qualified, then we think it is a
     * service process and we will not init SDK
     */
    private boolean isMainProcess() {
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);

        Log.d(TAG, "process app name : " + processAppName);

        // if there is application has remote service, application:onCreate() maybe called twice
        // this check is to make sure SDK will initialized only once
        // return if process name is not application's name since the package name is the default process name
        if (processAppName == null || !processAppName.equalsIgnoreCase(mContext.getPackageName())) {
            Log.e(TAG, "enter the service process!");
            return false;
        }
        return true;
    }

    /**
     * According to Pid to obtain the name of the current process, the general is the current app
     * package name,
     *
     * @param pID Process ID
     * @return Process name
     */
    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = mContext.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info =
                    (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(
                            pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    // Log.d("Process", "Id: "+ info.pid +" ProcessName: "+
                    // info.processName +"  Label: "+c.toString());
                    // processName = c.toString();
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                // Log.d("Process", "Error>> :"+ e.toString());
            }
        }
        return processName;
    }
}
