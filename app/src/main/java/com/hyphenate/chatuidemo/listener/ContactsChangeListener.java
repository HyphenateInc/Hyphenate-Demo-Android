package com.hyphenate.chatuidemo.listener;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.hyphenate.EMContactListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.ui.user.UserDao;
import com.hyphenate.chatuidemo.ui.user.UserEntity;
import com.hyphenate.easeui.EaseConstant;

/**
 * Created by lzan13 on 2016/10/24.
 * Contacts change listener
 */

public class ContactsChangeListener implements EMContactListener {

    private LocalBroadcastManager localBroadcastManager;

    private Context mContext;

    public ContactsChangeListener(Context context) {
        mContext = context;
    }

    /**
     * Added contacts
     *
     * @param username Added username
     */
    @Override public void onContactAdded(String username) {
        UserEntity userEntity = new UserEntity(username);
        UserDao.getInstance(mContext).saveContact(userEntity);

        DemoHelper.getInstance().putContacts(userEntity);
        // send broadcast
        sendBroadcast(EaseConstant.BROADCAST_ACTION_APPLICATION);
    }

    /**
     * Deleted Contacts
     *
     * @param username Deleted username
     */
    @Override public void onContactDeleted(String username) {
        UserEntity userEntity = new UserEntity(username);
        UserDao.getInstance(mContext).deleteContact(userEntity);
        
        DemoHelper.getInstance().popContacts(userEntity);

        // send Broadcast
        sendBroadcast(EaseConstant.BROADCAST_ACTION_CONTACTS);
    }

    /**
     * Received a friend request
     *
     * @param username The requestor's username
     * @param reason request reason
     */
    @Override public void onContactInvited(String username, String reason) {
        // 根据申请者的 username 和当前登录账户 username 拼接出msgId方便后边更新申请信息
        String msgId = username + 0;

        // Create message save application info
        EMMessage message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
        EMTextMessageBody body = new EMTextMessageBody(username + " Apply to become friends");
        message.addBody(body);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_USERNAME, username);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_REASON, reason);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_TYPE, 0);
        message.setFrom(EaseConstant.CONVERSATION_NAME_APPLY);
        message.setMsgId(msgId);
        // save message to db
        EMClient.getInstance().chatManager().saveMessage(message);
        // send broadcast
        sendBroadcast(EaseConstant.BROADCAST_ACTION_APPLICATION);
    }

    /**
     * Accepted friend request
     *
     * @param username The requestor's username
     */
    @Override public void onFriendRequestAccepted(String username) {

        // send broadcast
        sendBroadcast(EaseConstant.BROADCAST_ACTION_APPLICATION);
    }

    /**
     * Declined friend request
     *
     * @param username The requestor's username
     */
    @Override public void onFriendRequestDeclined(String username) {

        // send broadcast
        sendBroadcast(EaseConstant.BROADCAST_ACTION_APPLICATION);
    }

    /**
     * Send local broadcast
     *
     * @param action Broadcast action, the receiver can be filtered according to this action
     */
    private void sendBroadcast(String action) {
        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        localBroadcastManager.sendBroadcast(new Intent(action));
    }
}
