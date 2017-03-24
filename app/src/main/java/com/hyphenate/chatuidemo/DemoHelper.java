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
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatuidemo.call.CallReceiver;
import com.hyphenate.chatuidemo.call.CallStateChangeListener;
import com.hyphenate.chatuidemo.call.VideoCallActivity;
import com.hyphenate.chatuidemo.call.VoiceCallActivity;
import com.hyphenate.chatuidemo.chat.ChatActivity;
import com.hyphenate.chatuidemo.chat.MessageNotifier;
import com.hyphenate.chatuidemo.group.GroupChangeListener;
import com.hyphenate.chatuidemo.ui.MainActivity;
import com.hyphenate.chatuidemo.user.ContactsChangeListener;
import com.hyphenate.chatuidemo.user.model.UserEntity;
import com.hyphenate.chatuidemo.user.model.UserProfileManager;
import com.hyphenate.easeui.EaseUI;
import com.hyphenate.easeui.model.EaseUser;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    // Call broadcast receiver
    private CallReceiver mCallReceiver = null;

    // Call state listener
    private CallStateChangeListener mCallStateChangeListener = null;

    // Contacts listener
    private ContactsChangeListener mContactListener = null;

    private DefaultGroupChangeListener mGroupListener = null;

    private UserProfileManager mUserManager;

    //whether in calling
    public boolean isVoiceCalling;
    public boolean isVideoCalling;
    private ExecutorService executor = null;

    /**
     * save foreground Activity which registered message listeners
     */
    private List<Activity> activityList = new ArrayList<Activity>();

    private MessageNotifier mNotifier = new MessageNotifier();

    private DemoHelper() {
        this.executor = Executors.newCachedThreadPool();
    }

    public synchronized static DemoHelper getInstance() {
        if (instance == null) {
            instance = new DemoHelper();
        }
        return instance;
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
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
            // init call options
            initCallOptions();
            // set debug mode open:true, close:false
            EMClient.getInstance().setDebugMode(true);
            //init EaseUI if you want to use it
            EaseUI.getInstance().init(context);
            PreferenceManager.init(context);
            //init user manager
            getUserManager().init(context);
            //init message notifier
            mNotifier.init(context);
            //set events listeners
            setGlobalListener();
            setEaseUIProviders();

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
        // set auto accept group invitation
        SharedPreferences preferences =
                android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
        options.setAutoAcceptGroupInvitation(preferences.getBoolean(
                mContext.getString(R.string.em_pref_key_accept_group_invite_automatically), false));

        //set gcm project number
        options.setGCMNumber("324169311137");

        return options;
    }

    /**
     * init call options
     */
    private void initCallOptions() {
        // set video call bitrate, default(150)
        EMClient.getInstance().callManager().getCallOptions().setMaxVideoKbps(800);

        // set video call resolution, default(320, 240)
        EMClient.getInstance().callManager().getCallOptions().setVideoResolution(640, 480);

        // send push notification when user offline
        EMClient.getInstance().callManager().getCallOptions().setIsSendPushIfOffline(true);
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
            String msgId = s2 + s + EMClient.getInstance().getCurrentUser();
            EMMessage message = EMClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON,
                        " receive invitation to join the group：" + s1);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, "");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                EMClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                EMTextMessageBody body =
                        new EMTextMessageBody(" receive invitation to join the group：" + s1);
                message.addBody(body);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_ID, s);
                message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, s2);
                message.setAttribute(Constant.MESSAGE_ATTR_REASON,
                        " receive invitation to join the group：" + s1);
                message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 1);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE, 0);
                message.setFrom(Constant.CONVERSATION_NAME_APPLY);
                message.setMsgId(msgId);
                // save message to db
                EMClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onRequestToJoinReceived(String s, String s1, String s2, String s3) {
            String msgId = s2 + s + EMClient.getInstance().getCurrentUser();
            EMMessage message = EMClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, " Apply to join group：" + s1);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, "");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                EMClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody(s2 + " Apply to join group：" + s1);
                message.addBody(body);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_ID, s);
                message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, s2);
                message.setAttribute(Constant.MESSAGE_ATTR_REASON,
                        s2 + " Apply to join public group：" + s1);
                message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 1);
                message.setFrom(Constant.CONVERSATION_NAME_APPLY);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE, 1);
                message.setMsgId(msgId);
                // save message to db
                EMClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onRequestToJoinAccepted(String s, String s1, String s2) {
            String msgId = s2 + s + EMClient.getInstance().getCurrentUser();
            EMMessage message = EMClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s2 + " Accepted your group apply ");
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s2 + "Agreed");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                EMClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody(s2 + " Accepted your group apply ");
                message.addBody(body);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_ID, s);
                message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, s1);
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s2 + " Accepted your group apply ");
                message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 1);
                message.setFrom(Constant.CONVERSATION_NAME_APPLY);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE, 1);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s2 + " Agreed");
                message.setStatus(EMMessage.Status.SUCCESS);
                message.setMsgId(msgId);
                // save accept message
                EMClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onRequestToJoinDeclined(String s, String s1, String s2, String s3) {
            String msgId = s2 + s + EMClient.getInstance().getCurrentUser();
            EMMessage message = EMClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s2 + " Declined your group apply ");
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s2 + " Declined");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                EMClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody(s2 + " Declined your group apply ");
                message.addBody(body);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_ID, s);
                message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, s1);
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s2 + " Declined your group apply ");
                message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 1);
                message.setFrom(Constant.CONVERSATION_NAME_APPLY);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE, 1);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s2 + " Declined");
                message.setStatus(EMMessage.Status.SUCCESS);
                message.setMsgId(msgId);
                // save accept message
                EMClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onInvitationAccepted(String s, String s1, String s2) {
            String msgId = s2 + s + EMClient.getInstance().getCurrentUser();
            EMMessage message = EMClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s1 + " Accepted your group invite ");
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s1 + " Accepted");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                EMClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody(s2 + " Accepted your group invite ");
                message.addBody(body);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_ID, s);
                message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, s1);
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s1 + " Accepted your group invite ");
                message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 1);
                message.setFrom(Constant.CONVERSATION_NAME_APPLY);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE, 0);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s1 + " Accepted");
                message.setStatus(EMMessage.Status.SUCCESS);
                message.setMsgId(msgId);
                // save accept message
                EMClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onInvitationDeclined(String s, String s1, String s2) {
            String msgId = s2 + s + EMClient.getInstance().getCurrentUser();
            EMMessage message = EMClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s1 + " Declined your group invite ");
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s1 + " Declined");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                EMClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
                EMTextMessageBody body = new EMTextMessageBody(s1 + " Declined your group invite ");
                message.addBody(body);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_ID, s);
                message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, s1);
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s1 + " Declined your group invite ");
                message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 1);
                message.setFrom(Constant.CONVERSATION_NAME_APPLY);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE, 0);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s1 + " Declined");
                message.setStatus(EMMessage.Status.SUCCESS);
                message.setMsgId(msgId);
                // save accept message
                EMClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onUserRemoved(String s, String s1) {
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onGroupDestroyed(String s, String s1) {
            getNotifier().vibrateAndPlayTone(null);
        }

        @Override public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
            EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            msg.setChatType(EMMessage.ChatType.GroupChat);
            msg.setFrom(s1);
            msg.setTo(s);
            msg.setMsgId(UUID.randomUUID().toString());
            msg.addBody(new EMTextMessageBody(s1 + " Invite you to join this group "));
            msg.setStatus(EMMessage.Status.SUCCESS);
            // save invitation as messages
            EMClient.getInstance().chatManager().saveMessage(msg);

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
        //set notification options, will use default if you don't set it
        getNotifier().setNotificationInfoProvider(
                new MessageNotifier.EaseNotificationInfoProvider() {

                    @Override public String getTitle(EMMessage message) {
                        //you can update title here
                        return null;
                    }

                    @Override public int getSmallIcon(EMMessage message) {
                        //you can update icon here
                        return 0;
                    }

                    @Override public String getDisplayedText(EMMessage message) {
                        // be used on notification bar, different text according the message type.
                        String ticker = EaseCommonUtils.getMessageDigest(message, mContext);
                        if (message.getType() == EMMessage.Type.TXT) {
                            ticker = ticker.replaceAll("\\[.{2,3}\\]", "[Emoticon]");
                        }
                        EaseUser user = getUserInfo(message.getFrom());
                        if (user != null) {
                            return user.getEaseNickname() + ": " + ticker;
                        } else {
                            return message.getFrom() + ": " + ticker;
                        }
                    }

                    @Override public String getLatestText(EMMessage message, int fromUsersNum,
                            int messageNum) {
                        // here you can customize the text.
                        // return fromUsersNum + "contacts send " + messageNum + "messages to you";
                        return null;
                    }

                    @Override public Intent getLaunchIntent(EMMessage message) {
                        // you can set what activity you want display when user click the notification
                        Intent intent = new Intent(mContext, ChatActivity.class);
                        // open calling activity if there is call
                        if (isVideoCalling) {
                            intent = new Intent(mContext, VideoCallActivity.class);
                        } else if (isVoiceCalling) {
                            intent = new Intent(mContext, VoiceCallActivity.class);
                        } else {
                            ChatType chatType = message.getChatType();
                            if (chatType == ChatType.Chat) { // single chat message
                                intent.putExtra("userId", message.getFrom());
                                intent.putExtra("chatType", Constant.CHATTYPE_SINGLE);
                            } else { // group chat message
                                // message.getTo() is the group id
                                intent.putExtra("userId", message.getTo());
                                if (chatType == ChatType.GroupChat) {
                                    intent.putExtra("chatType", Constant.CHATTYPE_GROUP);
                                } else {
                                    intent.putExtra("chatType", Constant.CHATTYPE_CHATROOM);
                                }
                            }
                        }
                        return intent;
                    }
                });
        EaseUI.getInstance().setSettingsProvider(new EaseUI.EaseSettingsProvider() {
            @Override public boolean isMsgNotifyAllowed(EMMessage message) {
                return DemoModel.getInstance(mContext).isNotification();
            }

            @Override public boolean isMsgSoundAllowed(EMMessage message) {
                return DemoModel.getInstance(mContext).isSoundNotification();
            }

            @Override public boolean isMsgVibrateAllowed(EMMessage message) {
                return DemoModel.getInstance(mContext).isVibrateNotification();
            }

            @Override public boolean isSpeakerOpened() {
                return false;
            }
        });
    }

    private EaseUser getUserInfo(String username) {
        EaseUser user;
        if (username.equals(EMClient.getInstance().getCurrentUser())) {
            return getUserManager().getCurrentUserInfo();
        }
        user = mUserManager.getContactList().get(username);

        //TODO Get not in the buddy list of group members in the specific information, that stranger information, demo not implemented

        // if user is not in your contacts, set initial letter for him/her
        if (user == null) {
            user = new UserEntity(username);
        }
        return user;
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
            mCallStateChangeListener = new CallStateChangeListener(mContext);
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
                    final String action = cmdMsgBody.action();

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

    public UserProfileManager getUserManager() {
        if (mUserManager == null) {
            mUserManager = new UserProfileManager();
        }
        return mUserManager;
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
    protected void onConnectionConflict() {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constant.ACCOUNT_CONFLICT, true);
        mContext.startActivity(intent);
    }

    private synchronized void reset() {
        getUserManager().reset();
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
        return null;
    }
}
