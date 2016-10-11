package com.hyphenate.chatuidemo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentFilter;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chatuidemo.receiver.CallReceiver;
import com.hyphenate.util.EMLog;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lzan13 on 2016/10/11.
 * The initialization class does some initialization of Hyphenate sdk
 */
public class DemoHelper {

    private final String TAG = "DemoHelper";
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

    /**
     * 单例类，用来初始化环信的sdk
     *
     * @return 返回当前类的实例
     */
    public static DemoHelper getInstance() {
        if (instance == null) {
            instance = new DemoHelper();
        }
        return instance;
    }

    /**
     * 私有的构造方法
     */
    private DemoHelper() {
    }

    /**
     * 初始化环信的SDK
     *
     * @param context 上下文菜单
     * @return 返回初始化状态是否成功
     */
    public synchronized boolean init(Context context) {
        EMLog.d(TAG, "------- init easemob start --------------");

        mContext = context;
        // get process ID
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        /**
         * 如果app启用了远程的service，此application:onCreate会被调用2次
         * 为了防止环信SDK被初始化2次，加此判断会保证SDK被初始化1次
         * 默认的app会在以包名为默认的process name下运行，如果查到的process name不是app的process name就立即返回
         */
        if (processAppName == null || !processAppName.equalsIgnoreCase(context.getPackageName())) {
            // 则此application的onCreate 是被service 调用的，直接返回
            return true;
        }
        if (isInit) {
            return isInit;
        }
        mContext = context;

        // init sdk
        EMClient.getInstance().init(mContext, initOptions());

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
        // 启动私有化配置
        options.enableDNSConfig(true);
        // 设置Appkey，如果配置文件已经配置，这里可以不用设置
        //        options.setAppKey("lzan13#hxsdkdemo");
        // 设置自动登录
        options.setAutoLogin(true);
        // 设置是否按照服务器时间排序，false按照本地时间排序
        options.setSortMessageByServerTime(false);
        // 设置是否需要发送已读回执
        options.setRequireAck(true);
        // 设置是否需要发送回执
        options.setRequireDeliveryAck(true);
        // 设置是否需要服务器收到消息确认
        options.setRequireServerAck(true);
        // 收到好友申请是否自动同意，如果是自动同意就不会收到好友请求的回调，因为sdk会自动处理，默认为true
        options.setAcceptInvitationAlways(false);
        // 设置是否自动接收加群邀请，如果设置了当收到群邀请会自动同意加入
        options.setAutoAcceptGroupInvitation(false);
        // 设置（主动或被动）退出群组时，是否删除群聊聊天记录
        options.setDeleteMessagesAsExitGroup(false);
        // 设置是否允许聊天室的Owner 离开并删除聊天室的会话
        options.allowChatroomOwnerLeave(true);

        // set google GCM id
        // options.setGCMNumber(MLConstants.ML_GCM_NUMBER);

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
     * 设置通话广播监听
     */
    private void setCallReceiverListener() {
        // 设置通话广播监听器过滤内容
        IntentFilter callFilter = new IntentFilter(
                EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        if (mCallReceiver == null) {
            mCallReceiver = new CallReceiver();
        }
        //注册通话广播接收者
        mContext.registerReceiver(mCallReceiver, callFilter);
    }

    /**
     * ------------------------------- Connection Listener --------------------- 链接监听，监听与服务器连接状况
     */
    private void setConnectionListener() {
        mConnectionListener = new EMConnectionListener() {

            /**
             * 链接聊天服务器成功
             */
            @Override public void onConnected() {
                EMLog.d(TAG, "onConnected");
            }

            /**
             * 链接聊天服务器失败
             *
             * @param errorCode 连接失败错误码
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
     * According to Pid to obtain the name of the current process, the general is the current app
     * package name,
     *
     * @param pid Process ID
     * @return Process name
     */
    private String getAppName(int pid) {
        String processName = null;
        ActivityManager activityManager =
                (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List list = activityManager.getRunningAppProcesses();
        Iterator i = list.iterator();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info =
                    (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == pid) {
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
