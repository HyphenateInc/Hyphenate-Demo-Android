package io.agora.chatdemo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import io.agora.CallBack;
import io.agora.ConnectionListener;
import io.agora.Error;
import io.agora.MessageListener;
import io.agora.chat.ChatClient;
import io.agora.chat.CmdMessageBody;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatMessage.ChatType;
import io.agora.chat.ChatOptions;
import io.agora.chat.TextMessageBody;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.chat.MessageNotifier;
import io.agora.chatdemo.group.AgoraGroupChangeListener;
import io.agora.chatdemo.ui.MainActivity;
import io.agora.chatdemo.user.ContactsChangeListener;
import io.agora.chatdemo.user.model.UserEntity;
import io.agora.chatdemo.user.model.UserProfileManager;
import io.agora.easeui.EaseUI;
import io.agora.easeui.model.EaseUser;
import io.agora.easeui.utils.EaseCommonUtils;
import io.agora.push.PushConfig;
import io.agora.util.EMLog;

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

    // Contacts listener
    private ContactsChangeListener mContactListener = null;

    private DefaultAgoraGroupChangeListener mGroupListener = null;

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
    private final Handler handler;

    private DemoHelper() {
        this.executor = Executors.newCachedThreadPool();
        handler = new Handler(Looper.getMainLooper());
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
            ChatClient.getInstance().init(context, initOptions());
            // set debug mode open:true, close:false
            ChatClient.getInstance().setDebugMode(true);
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
    private ChatOptions initOptions() {
        // set init sdk options
        ChatOptions options = new ChatOptions();
        // set appkey
        options.setAppKey("1193210624041558#chatdemo");
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

        // default use FCM
        options.setUseFCM(true);
        //set fcm project number
        PushConfig.Builder builder = new PushConfig.Builder(mContext);
        builder.enableFCM("142290967082");
        options.setPushConfig(builder.build());

        //custom settings
        //options.setRestServer("a1-hsb.easemob.com");
        //options.setIMServer("106.75.100.247");
        //options.setImPort(6717);

        return options;
    }

    /**
     * init global listener
     */
    private void setGlobalListener() {
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
            mGroupListener = new DefaultAgoraGroupChangeListener();
        }

        ChatClient.getInstance().groupManager().addGroupChangeListener(mGroupListener);
    }

    private void showToast(String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class DefaultAgoraGroupChangeListener extends AgoraGroupChangeListener {
        @Override public void onInvitationReceived(String s, String s1, String s2, String s3) {
            String msgId = s2 + s + ChatClient.getInstance().getCurrentUser();
            ChatMessage message = ChatClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON,
                        " receive invitation to join the group：" + s1);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, "");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                ChatClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
                TextMessageBody body =
                        new TextMessageBody(" receive invitation to join the group：" + s1);
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
                ChatClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
            showToast(" receive invitation to join the group：" + s1);
        }

        @Override public void onRequestToJoinReceived(String s, String s1, String s2, String s3) {
            String msgId = s2 + s + ChatClient.getInstance().getCurrentUser();
            ChatMessage message = ChatClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, " Apply to join group：" + s1);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, "");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                ChatClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
                TextMessageBody body = new TextMessageBody(s2 + " Apply to join group：" + s1);
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
                ChatClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
            showToast(s2 + " Apply to join group：" + s1);
        }

        @Override public void onRequestToJoinAccepted(String s, String s1, String s2) {
            String msgId = s2 + s + ChatClient.getInstance().getCurrentUser();
            ChatMessage message = ChatClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s2 + " Accepted your group apply ");
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s2 + "Agreed");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                ChatClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
                TextMessageBody body = new TextMessageBody(s2 + " Accepted your group apply ");
                message.addBody(body);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_ID, s);
                message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, s1);
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s2 + " Accepted your group apply ");
                message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 1);
                message.setFrom(Constant.CONVERSATION_NAME_APPLY);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE, 1);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s2 + " Agreed");
                message.setStatus(ChatMessage.Status.SUCCESS);
                message.setMsgId(msgId);
                // save accept message
                ChatClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
            showToast(s2 + " Accepted your group apply ");
        }

        @Override public void onRequestToJoinDeclined(String s, String s1, String s2, String s3) {
            String msgId = s2 + s + ChatClient.getInstance().getCurrentUser();
            ChatMessage message = ChatClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s2 + " Declined your group apply ");
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s2 + " Declined");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                ChatClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
                TextMessageBody body = new TextMessageBody(s2 + " Declined your group apply ");
                message.addBody(body);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_ID, s);
                message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, s1);
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s2 + " Declined your group apply ");
                message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 1);
                message.setFrom(Constant.CONVERSATION_NAME_APPLY);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE, 1);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s2 + " Declined");
                message.setStatus(ChatMessage.Status.SUCCESS);
                message.setMsgId(msgId);
                // save accept message
                ChatClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
            showToast(s2 + " Declined your group apply ");
        }

        @Override public void onInvitationAccepted(String s, String s1, String s2) {
            String msgId = s2 + s + ChatClient.getInstance().getCurrentUser();
            ChatMessage message = ChatClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s1 + " Accepted your group invite ");
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s1 + " Accepted");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                ChatClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
                TextMessageBody body = new TextMessageBody(s2 + " Accepted your group invite ");
                message.addBody(body);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_ID, s);
                message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, s1);
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s1 + " Accepted your group invite ");
                message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 1);
                message.setFrom(Constant.CONVERSATION_NAME_APPLY);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE, 0);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s1 + " Accepted");
                message.setStatus(ChatMessage.Status.SUCCESS);
                message.setMsgId(msgId);
                // save accept message
                ChatClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
            showToast(s2 + " Accepted your group invite ");
        }

        @Override public void onInvitationDeclined(String s, String s1, String s2) {
            String msgId = s2 + s + ChatClient.getInstance().getCurrentUser();
            ChatMessage message = ChatClient.getInstance().chatManager().getMessage(msgId);
            if (message != null) {
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s1 + " Declined your group invite ");
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s1 + " Declined");
                message.setMsgTime(System.currentTimeMillis());
                message.setLocalTime(message.getMsgTime());
                message.setUnread(true);
                // update message
                ChatClient.getInstance().chatManager().updateMessage(message);
            } else {
                // Create message save application info
                message = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
                TextMessageBody body = new TextMessageBody(s1 + " Declined your group invite ");
                message.addBody(body);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_ID, s);
                message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, s1);
                message.setAttribute(Constant.MESSAGE_ATTR_REASON, s1 + " Declined your group invite ");
                message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 1);
                message.setFrom(Constant.CONVERSATION_NAME_APPLY);
                message.setAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE, 0);
                message.setAttribute(Constant.MESSAGE_ATTR_STATUS, s1 + " Declined");
                message.setStatus(ChatMessage.Status.SUCCESS);
                message.setMsgId(msgId);
                // save accept message
                ChatClient.getInstance().chatManager().saveMessage(message);
            }
            getNotifier().vibrateAndPlayTone(null);
            showToast(s1 + " Declined your group invite ");
        }

        @Override public void onUserRemoved(String s, String s1) {
            getNotifier().vibrateAndPlayTone(null);
            showToast("You have been removed from the group: "+s1);
        }

        @Override public void onGroupDestroyed(String s, String s1) {
            getNotifier().vibrateAndPlayTone(null);
            showToast("Group "+s1 + " has been disbanded");
        }

        @Override public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
            ChatMessage msg = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
            msg.setChatType(ChatMessage.ChatType.GroupChat);
            msg.setFrom(s1);
            msg.setTo(s);
            msg.setMsgId(UUID.randomUUID().toString());
            msg.addBody(new TextMessageBody(s1 + " Invite you to join this group "));
            msg.setStatus(ChatMessage.Status.SUCCESS);
            // save invitation as messages
            ChatClient.getInstance().chatManager().saveMessage(msg);

            getNotifier().vibrateAndPlayTone(null);
            showToast(s1 + " Invite you to join this group ");
        }

        @Override
        public void onAdminAdded(String groupId, String administrator) {
            showToast(administrator + " has been designated as the group admin by group owner");
        }

        @Override
        public void onAdminRemoved(String groupId, String administrator) {
            showToast(administrator + " has been revoked by the group owner");
        }

        @Override
        public void onMuteListAdded(String groupId, List<String> mutes, long muteExpire) {
            String content = getContentFromList(mutes);
            showToast(content + " been banned by group manager");
        }

        @Override
        public void onMuteListRemoved(String groupId, List<String> mutes) {
            String content = getContentFromList(mutes);
            showToast(content + " been resumed to speak by group manager");
        }

        @Override
        public void onWhiteListAdded(String groupId, List<String> whitelist) {
            String content = getContentFromList(whitelist);
            showToast(content + " been added to whitelist by group manager");
        }

        @Override
        public void onWhiteListRemoved(String groupId, List<String> whitelist) {
            String content = getContentFromList(whitelist);
            showToast(content + " been removed from whitelist by group manager");
        }


        @Override
        public void onAllMemberMuteStateChanged(String groupId, boolean isMuted) {

        }
    }

    private String getContentFromList(List<String> members) {
        StringBuilder sb = new StringBuilder();
        for (String member : members) {
            if(!TextUtils.isEmpty(sb.toString().trim())) {
                sb.append(",");
            }
            sb.append(member);
        }
        String content = sb.append(" has").toString();
        if(content.contains(ChatClient.getInstance().getCurrentUser())) {
            content = "You have";
        }
        return content;
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

                    @Override public String getTitle(ChatMessage message) {
                        //you can update title here
                        return null;
                    }

                    @Override public int getSmallIcon(ChatMessage message) {
                        //you can update icon here
                        return 0;
                    }

                    @Override public String getDisplayedText(ChatMessage message) {
                        // be used on notification bar, different text according the message type.
                        String ticker = EaseCommonUtils.getMessageDigest(message, mContext);
                        if (message.getType() == ChatMessage.Type.TXT) {
                            ticker = ticker.replaceAll("\\[.{2,3}\\]", "[Emoticon]");
                        }
                        EaseUser user = getUserInfo(message.getFrom());
                        if (user != null) {
                            return user.getEaseNickname() + ": " + ticker;
                        } else {
                            return message.getFrom() + ": " + ticker;
                        }
                    }

                    @Override public String getLatestText(ChatMessage message, int fromUsersNum,
                            int messageNum) {
                        // here you can customize the text.
                        // return fromUsersNum + "contacts send " + messageNum + "messages to you";
                        return null;
                    }

                    @Override public Intent getLaunchIntent(ChatMessage message) {
                        // you can set what activity you want display when user click the notification
                        Intent intent = new Intent(mContext, ChatActivity.class);
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
                        return intent;
                    }
                });
        EaseUI.getInstance().setSettingsProvider(new EaseUI.EaseSettingsProvider() {
            @Override public boolean isMsgNotifyAllowed(ChatMessage message) {
                return DemoModel.getInstance(mContext).isNotification();
            }

            @Override public boolean isMsgSoundAllowed(ChatMessage message) {
                return DemoModel.getInstance(mContext).isSoundNotification();
            }

            @Override public boolean isMsgVibrateAllowed(ChatMessage message) {
                return DemoModel.getInstance(mContext).isVibrateNotification();
            }

            @Override public boolean isSpeakerOpened() {
                return false;
            }
        });
    }

    private EaseUser getUserInfo(String username) {
        EaseUser user;
        if (username.equals(ChatClient.getInstance().getCurrentUser())) {
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
     * Set Connection Listener
     */
    private void setConnectionListener() {
        ConnectionListener mConnectionListener = new ConnectionListener() {

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
                if (errorCode == Error.USER_LOGIN_ANOTHER_DEVICE) {
                    onConnectionConflict();
                }
            }
        };
        ChatClient.getInstance().addConnectionListener(mConnectionListener);
    }

    /**
     * new messages listener
     * If this event already handled by an activity, you don't need handle it again
     * activityList.size() <= 0 means all activities already in background or not in Activity Stack
     */
    private void registerMessageListener() {
        MessageListener messageListener = new MessageListener() {

            @Override public void onMessageReceived(List<ChatMessage> messages) {
                for (ChatMessage message : messages) {
                    EMLog.d(TAG, "onMessageReceived id : " + message.getMsgId());
                    // in background, do not refresh UI, notify it in notification bar
                    if (!hasForegroundActivities()) {
                        if (message.getBooleanAttribute(Constant.MESSAGE_ATTR_IS_CALL_PUSH, false)) {
                            ChatClient.getInstance().chatManager().getConversation(message.getFrom()).removeMessage(message.getMsgId());
                        } else {
                            // FIXME: conflict with group push notification if it's turned off, push still come in when phone is unlocked as the app is woken up by the phone to run in the background
                            getNotifier().onNewMsg(message);
                        }
                    }
                }
            }

            @Override public void onCmdMessageReceived(List<ChatMessage> messages) {
                for (ChatMessage message : messages) {
                    EMLog.d(TAG, "onCmdMessageReceived");
                    //get message body
                    CmdMessageBody cmdMsgBody = (CmdMessageBody) message.getBody();
                    final String action = cmdMsgBody.action();

                    //get extension attribute if you need
                    //message.getStringAttribute("");
                    EMLog.d(TAG, String.format("CmdMessage：action:%s,message:%s", action,
                            message.toString()));
                }
            }

            @Override public void onMessageRead(List<ChatMessage> messages) {
            }

            @Override public void onMessageDelivered(List<ChatMessage> message) {
            }

            @Override public void onMessageRecalled(List<ChatMessage> messages) {

            }

            @Override public void onMessageChanged(ChatMessage message, Object change) {

            }
        };

        ChatClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    /**
     * Register contacts listener
     * Listen for changes to contacts
     */
    private void registerContactsListener() {
        if (mContactListener == null) {
            mContactListener = new DefaultContactsChangeListener();
        }
        ChatClient.getInstance().contactManager().setContactListener(mContactListener);
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
    public void signOut(boolean unbindDeviceToken, final CallBack callback) {
        Log.d(TAG, "Sign out: " + unbindDeviceToken);
        ChatClient.getInstance().logout(unbindDeviceToken, new CallBack() {

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
