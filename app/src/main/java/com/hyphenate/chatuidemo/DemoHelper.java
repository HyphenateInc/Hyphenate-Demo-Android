package com.hyphenate.chatuidemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
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
import com.hyphenate.chatuidemo.listener.GroupChangeListener;
import com.hyphenate.chatuidemo.model.MessageNotifier;
import com.hyphenate.chatuidemo.ui.call.CallReceiver;
import com.hyphenate.chatuidemo.listener.CallStateChangeListener;
import com.hyphenate.chatuidemo.listener.ContactsChangeListener;
import com.hyphenate.chatuidemo.ui.user.UserDao;
import com.hyphenate.chatuidemo.ui.user.UserEntity;
import com.hyphenate.easeui.EaseUI;
import com.hyphenate.util.EMLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wei on 2016/10/11.
 * Demo app helper class
 */
public class DemoHelper {

    protected static final String TAG = DemoHelper.class.getSimpleName();
    //
    private static DemoHelper instance;
    // context
    private Context mContext;

    // Contacts map
    private Map<String, UserEntity> entityMap = new HashMap<>();

    // Call broadcast receiver
    private CallReceiver mCallReceiver = null;
    // Call state listener
    private CallStateChangeListener mCallStateChangeListener = null;

    // Contacts listener
    private ContactsChangeListener mContactListener = null;

    private DefaultGroupChangeListener mGroupListener = null;

    /**
     * save foreground Activity which registered message listeners
     */
    private List<Activity> activityList = new ArrayList<Activity>();

    private MessageNotifier mNotifier = new MessageNotifier();

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
            //init EaseUI if you want to use it
            EaseUI.getInstance().init(context);

            // set debug mode open:true, close:false
            EMClient.getInstance().setDebugMode(true);
            //init message notifier
            mNotifier.init(context);
            //set events listeners
            setGlobalListener();
            setNotificationType();

            EMLog.d(TAG, "------- init hyphenate end --------------");
        }
    }

    private void setNotificationType() {
        mNotifier.setNotificationInfoProvider(new MessageNotifier.EaseNotificationInfoProvider() {
            @Override public String getDisplayedText(EMMessage message) {
                return null;
            }

            @Override public String getLatestText(EMMessage message, int fromUsersNum, int messageNum) {
                return null;
            }

            @Override public String getTitle(EMMessage message) {
                return null;
            }

            @Override public int getSmallIcon(EMMessage message) {
                return 0;
            }

            @Override public Intent getLaunchIntent(EMMessage message) {
                return null;
            }
        });

        EaseUI.getInstance().setSettingsProvider(new EaseUI.EaseSettingsProvider() {
            @Override public boolean isMsgNotifyAllowed(EMMessage message) {
                return false;
            }

            @Override public boolean isMsgSoundAllowed(EMMessage message) {
                return false;
            }

            @Override public boolean isMsgVibrateAllowed(EMMessage message) {
                return false;
            }

            @Override public boolean isSpeakerOpened() {
                return false;
            }
        });
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
        //options.setAutoAcceptGroupInvitation(false);

        //set gcm project number
        options.setGCMNumber("998166487724");

        return options;
    }

    /**
     * init global listener
     */
    private void setGlobalListener() {
        // set call listener
        setCallReceiverListener();
        // set connection listener
        setConnectionListener();

        // register message listener
        registerMessageListener();

        // register contacts listener
        registerContactsListener();

        registerGroupListener();
    }

    private void registerGroupListener() {
        if (mGroupListener == null) {
            mGroupListener = new DefaultGroupChangeListener();
        }

        EMClient.getInstance().groupManager().addGroupChangeListener(mGroupListener);
    }

    private class DefaultGroupChangeListener extends GroupChangeListener {
        @Override public void onInvitationReceived(String s, String s1, String s2, String s3) {
            super.onInvitationReceived(s, s1, s2, s3);
            getNotifier().vibrateAndPlayTone(null);

        }

        @Override public void onRequestToJoinReceived(String s, String s1, String s2, String s3) {
            super.onRequestToJoinReceived(s, s1, s2, s3);
        }

        @Override public void onRequestToJoinAccepted(String s, String s1, String s2) {
            super.onRequestToJoinAccepted(s, s1, s2);
        }

        @Override public void onRequestToJoinDeclined(String s, String s1, String s2, String s3) {
            super.onRequestToJoinDeclined(s, s1, s2, s3);

        }

        @Override public void onInvitationAccepted(String s, String s1, String s2) {
            super.onInvitationAccepted(s, s1, s2);
        }

        @Override public void onInvitationDeclined(String s, String s1, String s2) {
            super.onInvitationDeclined(s, s1, s2);

        }

        @Override public void onUserRemoved(String s, String s1) {
            super.onUserRemoved(s, s1);
        }

        @Override public void onGroupDestroyed(String s, String s1) {
            super.onGroupDestroyed(s, s1);
        }

        @Override public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
            super.onAutoAcceptInvitationFromGroup(s, s1, s2);
            getNotifier().vibrateAndPlayTone(null);
        }
    }

    /**
     * Set call broadcast listener
     */
    private void setCallReceiverListener() {
        // Set the call broadcast listener to filter the action
        IntentFilter callFilter = new IntentFilter(EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
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
            mCallStateChangeListener = new CallStateChangeListener(mContext);
        }

        EMClient.getInstance().callManager().addCallStateChangeListener(mCallStateChangeListener);
    }

    /**
     * Remove call state listener
     */
    public void removeCallStateChangeListener() {
        if (mCallStateChangeListener != null) {
            EMClient.getInstance().callManager().removeCallStateChangeListener(mCallStateChangeListener);
            mCallStateChangeListener = null;
        }
    }

    /**
     * Set Connection Listener
     */
    private void setConnectionListener() {
        EMConnectionListener mConnectionListener = new EMConnectionListener() {

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
    private void registerMessageListener() {
        EMMessageListener messageListener = new EMMessageListener() {

            @Override public void onMessageReceived(List<EMMessage> messages) {
                for (EMMessage message : messages) {
                    EMLog.d(TAG, "onMessageReceived id : " + message.getMsgId());
                    // in background, do not refresh UI, notify it in notification bar
                    if (!hasForegroundActivities()) {
                        getNotifier().onNewMsg(message);
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
                    EMLog.d(TAG, String.format("CmdMessage：action:%s,message:%s", action, message.toString()));
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

    /**
     * Register contacts listener
     * Listen for changes to contacts
     */
    private void registerContactsListener() {
        if (mContactListener == null) {
            mContactListener = new ContactsChangeListener(mContext);
        }
        EMClient.getInstance().contactManager().setContactListener(mContactListener);
    }

    private boolean hasForegroundActivities() {
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

    public MessageNotifier getNotifier() {
        return mNotifier;
    }

    public Map<String, UserEntity> getContactList() {
        if (entityMap.isEmpty()) {
            entityMap = UserDao.getInstance(mContext).getContactList();
        }
        return entityMap;
    }

    public void setContactList(List<UserEntity> entityList) {
        UserDao.getInstance(mContext).saveContactList(entityList);
    }

    public void addContacts(UserEntity userEntity) {
        if (entityMap != null) {
            entityMap.put(userEntity.getUsername(), userEntity);
        }

        UserDao.getInstance(mContext).saveContact(userEntity);
    }

    /**
     * remove user from db
     */
    public void deleteContacts(UserEntity userEntity) {
        if (entityMap != null) {
            entityMap.remove(userEntity.getUsername());
        }

        UserDao.getInstance(mContext).deleteContact(userEntity);
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
                entityMap.clear();
                UserDao.getInstance(mContext).closeDB();
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
        String processName;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = mContext.getPackageManager();
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
        return null;
    }
}
