package com.hyphenate.chatuidemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chatuidemo.call.CallReceiver;
import com.hyphenate.chatuidemo.call.CallStateChangeListener;
import com.hyphenate.chatuidemo.chat.MessageNotifier;
import com.hyphenate.chatuidemo.group.GroupChangeListener;
import com.hyphenate.chatuidemo.ui.MainActivity;
import com.hyphenate.chatuidemo.user.ContactsChangeListener;
import com.hyphenate.chatuidemo.user.model.UserDao;
import com.hyphenate.chatuidemo.user.model.UserEntity;
import com.hyphenate.chatuidemo.user.model.UserProfileManager;
import com.hyphenate.easeui.EaseUI;
import com.hyphenate.easeui.model.EaseUser;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.exceptions.HyphenateException;
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

    private UserProfileManager userProManager;

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
            PreferenceManager.init(context);
            //init user manager
            getUserProfileManager().init(context);

            // set debug mode open:true, close:false
            EMClient.getInstance().setDebugMode(true);
            //init message notifier
            mNotifier.init(context);
            //set events listeners
            setGlobalListener();
            setNotificationType();
            setEaseUIProviders();

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

        //EaseUI.getInstance().setSettingsProvider(new EaseUI.EaseSettingsProvider() {
        //    @Override public boolean isMsgNotifyAllowed(EMMessage message) {
        //        return false;
        //    }
        //
        //    @Override public boolean isMsgSoundAllowed(EMMessage message) {
        //        return false;
        //    }
        //
        //    @Override public boolean isMsgVibrateAllowed(EMMessage message) {
        //        return false;
        //    }
        //
        //    @Override public boolean isSpeakerOpened() {
        //        return false;
        //    }
        //});
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

        SharedPreferences preferences =
                android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
        options.setAutoAcceptGroupInvitation(
                preferences.getBoolean("accept_group_invite_automatically", false));

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
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onRequestToJoinAccepted(String s, String s1, String s2) {
            super.onRequestToJoinAccepted(s, s1, s2);
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onRequestToJoinDeclined(String s, String s1, String s2, String s3) {
            super.onRequestToJoinDeclined(s, s1, s2, s3);
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onInvitationAccepted(String s, String s1, String s2) {
            super.onInvitationAccepted(s, s1, s2);
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onInvitationDeclined(String s, String s1, String s2) {
            super.onInvitationDeclined(s, s1, s2);
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onUserRemoved(String s, String s1) {
            super.onUserRemoved(s, s1);
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onGroupDestroyed(String s, String s1) {
            super.onGroupDestroyed(s, s1);
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
            super.onAutoAcceptInvitationFromGroup(s, s1, s2);
            getNotifier().vibrateAndPlayTone(null);
        }
    }

    protected void setEaseUIProviders() {
        // set profile provider if you want easeUI to handle avatar and nickname
        EaseUI.getInstance().setUserProfileProvider(new EaseUI.EaseUserProfileProvider() {

            @Override public EaseUser getUser(String username) {
                return getUserInfo(username);
            }
        });
    }

    private EaseUser getUserInfo(String username) {
        // To get instance of EaseUser, here we get it from the user list in memory
        // You'd better cache it if you get it from your server
        EaseUser user;
        if (username.equals(EMClient.getInstance().getCurrentUser())) {
            return getUserProfileManager().getCurrentUserInfo();
        }
        user = getContactList().get(username);

        // if user is not in your contacts, set initial letter for him/her
        if (user == null) {
            user = new EaseUser(username);
            EaseCommonUtils.setUserInitialLetter(user);
        }
        return user;
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
            @Override public void onDisconnected(int errorCode) {
                EMLog.d(TAG, "onDisconnected: " + errorCode);
                if (errorCode == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    onConnectionConflict();
                }
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
            mContactListener = new DefaultContactsChangeListener();
        }
        EMClient.getInstance().contactManager().setContactListener(mContactListener);
    }

    private class DefaultContactsChangeListener extends ContactsChangeListener {
        @Override public void onContactAdded(String username) {
            super.onContactAdded(username);
        }

        @Override public void onContactDeleted(String username) {
            super.onContactDeleted(username);
        }

        @Override public void onContactInvited(String username, String reason) {
            super.onContactInvited(username, reason);
        }

        @Override public void onFriendRequestAccepted(String username) {
            super.onFriendRequestAccepted(username);
        }

        @Override public void onFriendRequestDeclined(String username) {
            super.onFriendRequestDeclined(username);
        }
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
        entityMap.clear();
        for (UserEntity userEntity : entityList) {
            entityMap.put(userEntity.getUsername(), userEntity);
        }
        UserDao.getInstance(mContext).saveContactList(entityList);
    }

    public void saveContact(UserEntity userEntity) {
        if (entityMap != null) {
            entityMap.put(userEntity.getUsername(), userEntity);
        }

        UserDao.getInstance(mContext).saveContact(userEntity);
    }

    public UserProfileManager getUserProfileManager() {
        if (userProManager == null) {
            userProManager = new UserProfileManager();
        }
        return userProManager;
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

    public void asyncFetchContactsFromServer(final EMValueCallBack<List<UserEntity>> callback) {
        new Thread(new Runnable() {
            @Override public void run() {
                List<String> hxIdList;
                try {
                    hxIdList = EMClient.getInstance().contactManager().getAllContactsFromServer();

                    // save the contact list to cache
                    getContactList().clear();
                    final List<UserEntity> entityList = new ArrayList<>();
                    for (String userId : hxIdList) {
                        UserEntity user = new UserEntity(userId);
                        EaseCommonUtils.setUserInitialLetter(user);
                        entityList.add(user);
                    }

                    getUserProfileManager().asyncFetchContactsInfoFromServer(hxIdList, new EMValueCallBack<List<UserEntity>>() {

                        @Override public void onSuccess(List<UserEntity> uList) {
                            // save the contact list to database
                            setContactList(uList);
                            if (callback != null) {
                                callback.onSuccess(uList);
                            }
                        }

                        @Override public void onError(int error, String errorMsg) {
                            setContactList(entityList);
                            if (callback != null) {
                                callback.onError(error, errorMsg);
                            }
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError(e.getErrorCode(), e.toString());
                    }
                }
            }
        }).start();
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

                reset();
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
     * user has logged into another device
     */
    protected void onConnectionConflict(){
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(DemoConstant.ACCOUNT_CONFLICT, true);
        mContext.startActivity(intent);
    }


    private synchronized void reset() {
        entityMap.clear();
        getUserProfileManager().reset();
        UserDao.getInstance(mContext).closeDB();
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
