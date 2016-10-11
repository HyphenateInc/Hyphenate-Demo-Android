package com.hyphenate.chatuidemo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wei on 2016/10/11.
 * Demo app helper class
 */

public class DemoHelper {
    protected static final String TAG = DemoHelper.class.getSimpleName();


    private Context appContext;
    private static DemoHelper instance = null;

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
     * @param context
     *            application context
     */
    public void init(Context context) {
        appContext = context;
        //init chat options
        EMOptions options = initChatOptions();
        if(isMainProcess()) {
            //init hyphenate sdk with options
            EMClient.getInstance().init(context, options);
        }

    }

    private EMOptions initChatOptions(){
        Log.d(TAG, "init Hyphenate Options");

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
     * check the application process name if process name is not qualified, then we think it is a service process and we will not init SDK
     * @return
     */
    private boolean isMainProcess(){
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);

        Log.d(TAG, "process app name : " + processAppName);

        // if there is application has remote service, application:onCreate() maybe called twice
        // this check is to make sure SDK will initialized only once
        // return if process name is not application's name since the package name is the default process name
        if (processAppName == null || !processAppName.equalsIgnoreCase(appContext.getPackageName())) {
            Log.e(TAG, "enter the service process!");
            return false;
        }
        return true;
    }

    private String getAppName(int pID) {
        String processName = null;
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = appContext.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pID) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
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
