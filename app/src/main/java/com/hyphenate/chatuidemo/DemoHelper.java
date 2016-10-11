package com.hyphenate.chatuidemo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chatuidemo.receiver.CallReceiver;
import com.hyphenate.util.EMLog;
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

    //
    private boolean isInit;

    // call broadcast receiver
    private CallReceiver mCallReceiver = null;

    // connection listener
    private EMConnectionListener mConnectionListener;

    // if unbuild token
    private boolean isUnbuildToken = true;

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
    public synchronized boolean init(Context context) {
        EMLog.d(TAG, "------- init easemob start --------------");

        mContext = context;

        if(isMainProcess()) {
            //init hyphenate sdk with options
            EMClient.getInstance().init(context, initOptions());
        }

        // set debug mode open:true, close:false
        EMClient.getInstance().setDebugMode(true);

        //
        initGlobalListener();

        // init success
        isInit = true;
        EMLog.d(TAG, "------- init easemob end --------------");
        return isInit;
    }

    /**
     * init sdk options
     */
    private EMOptions initOptions() {
        /**
         * init sdk options more
         * http://www.easemob.com/apidoc/android/chat3.0/classcom_1_1hyphenate_1_1chat_1_1_e_m_options.html
         */
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
     *
     * These include:
     * connection monitoring{@link #setConnectionListener()}
     */
    public void initGlobalListener() {
        // set call listener
        setCallReceiverListener();
        // set connection listener
        setConnectionListener();
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
     * Sign out account
     *
     * @param callback to receive the result of the logout
     */
    public void signout(final EMCallBack callback) {
        /**
         * Call sdk sign out, this method requires two parameters
         *
         *  boolean: The first argument is required，Indicates that the push token is to be deallocated,
         *  and if account offline, this parameter is set to false
         *
         *  callback: Optional parameter to receive the result of the logout
         */
        EMClient.getInstance().logout(isUnbuildToken, new EMCallBack() {
            @Override public void onSuccess() {
                isUnbuildToken = true;
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override public void onError(int i, String s) {
                isUnbuildToken = true;
                if (callback != null) {
                    callback.onError(i, s);
                }
            }

            @Override public void onProgress(int i, String s) {
                if (callback != null) {
                    callback.onProgress(i, s);
                }
            }
        });
    }

    /**
     * Check whether the login has been successful
     */
    public boolean isLoginedInBefore() {
        return EMClient.getInstance().isLoggedInBefore();
    }

    /**
     * Determines whether the current app is connected to the chat server
     *
     * @return connection state
     */
    public boolean isConnection() {
        return EMClient.getInstance().isConnected();
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
