package com.hyphenate.chatuidemo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.IntentFilter;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chatuidemo.receiver.CallReceiver;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.util.EMLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lzan13 on 2016/10/11.
 * The initialization class does some initialization of Hyphenate sdk
 */
public class DemoHelper {

    private final String TAG = "DemoHelper";
    // 上下文对象
    private Context mContext;

    // MLEasemob 单例对象
    private static DemoHelper instance;

    // 保存当前运行的 activity 对象，可用来判断程序是否处于前台，以及完全退出app等操作
    private List<BaseActivity> mActivityList = new ArrayList<BaseActivity>();

    // 记录sdk是否初始化
    private boolean isInit;

    // 通话广播监听器
    private CallReceiver mCallReceiver = null;
    // 通话状态监听
    private EMCallStateChangeListener callStateListener;
    // 是否正在通话中
    public int isBus;

    // 环信的消息监听器
    private EMMessageListener mMessageListener;
    // 环信联系人监听
    private EMContactListener mContactListener;
    // 环信连接监听
    private EMConnectionListener mConnectionListener;
    // 环信群组变化监听
    private EMGroupChangeListener mGroupChangeListener;

    // 表示是是否解绑Token，一般离线状态都要设置为false
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
        // 获取当前进程 id 并取得进程名
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

        // 调用初始化方法初始化sdk
        EMClient.getInstance().init(mContext, initOptions());

        // 设置开启debug模式
        EMClient.getInstance().setDebugMode(true);

        // 初始化全局监听
        initGlobalListener();

        // 初始化完成
        isInit = true;
        EMLog.d(TAG, "------- init easemob end --------------");
        return isInit;
    }

    private EMOptions initOptions() {
        /**
         * SDK初始化的一些配置
         * 关于 EMOptions 可以参考官方的 API 文档
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

        // 设置google GCM推送id，国内可以不用设置
        // options.setGCMNumber(MLConstants.ML_GCM_NUMBER);

        // 设置集成小米推送的appid和appkey
        //options.setMipushConfig(MLConstants.ML_MI_APP_ID, MLConstants.ML_MI_APP_KEY);

        // 设置华为推送appid
        //options.setHuaweiPushAppId(MLConstants.ML_HUAWEI_APP_ID);
        return options;
    }

    /**
     * 初始化全局监听，其中包括： 连接监听 {@link #setConnectionListener()} 消息监听 {@link #setMessageListener()}
     */
    public void initGlobalListener() {
        EMLog.d(TAG, "------- listener start --------------");
        // 设置通话广播监听
        setCallReceiverListener();
        // 通话状态监听，TODO 这里不直接调用，只需要在有通话时调用
        // setCallStateChangeListener();
        // 设置全局的连接监听
        setConnectionListener();
        // 初始化全局消息监听
        setMessageListener();
        EMLog.d(TAG, "------- listener end ----------------");
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
     * ---------------------------------- Message Listener ---------------------------- 初始化全局的消息监听
     */
    protected void setMessageListener() {
        mMessageListener = new EMMessageListener() {
            /**
             * 收到新消息，离线消息也都是在这里获取
             *
             * @param list 收到的新消息集合，离线和在线都是走这个监听
             */
            @Override public void onMessageReceived(List<EMMessage> list) {

            }

            /**
             * 收到新的 CMD 消息
             *
             * @param list 收到的透传消息集合
             */
            @Override public void onCmdMessageReceived(List<EMMessage> list) {

            }

            /**
             * 收到新的已读回执
             *
             * @param list 收到消息已读回执
             */
            @Override public void onMessageRead(List<EMMessage> list) {

            }

            /**
             * 收到新的发送回执
             *
             * @param list 收到发送回执的消息集合
             */
            @Override public void onMessageDelivered(List<EMMessage> list) {

            }

            /**
             * 消息的状态改变
             *
             * @param message 发生改变的消息
             * @param object  包含改变的消息
             */
            @Override public void onMessageChanged(EMMessage message, Object object) {
            }
        };
        // 注册消息监听
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
    }

    /**
     * 退出登录环信
     *
     * @param callback 退出登录的回调函数，用来给上次回调退出状态
     */
    public void signOut(final EMCallBack callback) {
        /**
         * 调用sdk的退出登录方法，此方法需要两个参数
         * boolean 第一个是必须的，表示要解绑Token，如果离线状态这个参数要设置为false
         * callback 可选参数，用来接收推出的登录的结果
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
     * 判断是否登录成功过，并且没有调用logout和被踢
     *
     * @return 返回一个boolean值 表示是否登录成功过
     */
    public boolean isLoginedInBefore() {
        return EMClient.getInstance().isLoggedInBefore();
    }

    /**
     * 判断当前app是否连接聊天服务器
     *
     * @return 返回连接服务器状态
     */
    public boolean isConnection() {
        return EMClient.getInstance().isConnected();
    }

    /**
     * 根据Pid获取当前进程的名字，一般就是当前app的包名
     *
     * @param pid 进程的id
     * @return 返回进程的名字
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
                    // 根据进程的信息获取当前进程的名字
                    processName = info.processName;
                    // 返回当前进程名
                    return processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 没有匹配的项，返回为null
        return null;
    }

    /**
     * 获取当前运行启动的 activity 的列表
     *
     * @return 返回保存列表
     */
    public List<BaseActivity> getActivityList() {
        return mActivityList;
    }

    /**
     * 获取当前运行的 activity
     *
     * @return 返回当前活动的activity
     */
    public BaseActivity getTopActivity() {
        if (mActivityList.size() > 0) {
            return mActivityList.get(0);
        }
        return null;
    }

    /**
     * 添加当前activity到集合
     *
     * @param activity 需要添加的 activity
     */
    public void addActivity(BaseActivity activity) {
        if (!mActivityList.contains(activity)) {
            mActivityList.add(0, activity);
        }
    }

    /**
     * 从 Activity 运行列表移除当前要退出的 activity
     *
     * @param activity 要移除的 activity
     */
    public void removeActivity(BaseActivity activity) {
        if (mActivityList.contains(activity)) {
            mActivityList.remove(activity);
        }
    }
}
