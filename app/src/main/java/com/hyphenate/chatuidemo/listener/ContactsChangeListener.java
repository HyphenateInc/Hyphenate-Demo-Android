package com.hyphenate.chatuidemo.listener;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.hyphenate.EMContactListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.receiver.BroadCastReceiverManager;
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

        DemoHelper.getInstance().addContacts(userEntity);
        // send broadcast
        BroadCastReceiverManager.getInstance(mContext).sendBroadCastReceiver(EaseConstant.BROADCAST_ACTION_APPLY);
    }

    /**
     * Deleted Contacts
     *
     * @param username Deleted username
     */
    @Override public void onContactDeleted(String username) {
        UserEntity userEntity = new UserEntity(username);

        DemoHelper.getInstance().deleteContacts(userEntity);

        // send Broadcast
        BroadCastReceiverManager.getInstance(mContext).sendBroadCastReceiver(EaseConstant.BROADCAST_ACTION_CONTACTS);
    }

    /**
     * Received a friend request
     *
     * @param username The requestor's username
     * @param reason request reason
     */
    @Override public void onContactInvited(String username, String reason) {
        String msgId = username + System.currentTimeMillis();

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
        BroadCastReceiverManager.getInstance(mContext).sendBroadCastReceiver(EaseConstant.BROADCAST_ACTION_APPLY);
    }

    /**
     * Accepted friend request
     *
     * @param username The requestor's username
     */
    @Override public void onFriendRequestAccepted(String username) {
        String msgId = username + System.currentTimeMillis();

        // Create message save application info
        EMMessage message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
        EMTextMessageBody body = new EMTextMessageBody(username + " agrees with your apply");
        message.addBody(body);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_USERNAME, username);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_REASON, username + " agrees with your apply");
        message.setAttribute(EaseConstant.MESSAGE_ATTR_TYPE, 0);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_STATUS, "Agreed");
        message.setFrom(EaseConstant.CONVERSATION_NAME_APPLY);
        message.setMsgId(msgId);
        // save message to db
        EMClient.getInstance().chatManager().saveMessage(message);
        // send broadcast
        BroadCastReceiverManager.getInstance(mContext).sendBroadCastReceiver(EaseConstant.BROADCAST_ACTION_APPLY);
    }

    /**
     * Declined friend request
     *
     * @param username The requestor's username
     */
    @Override public void onFriendRequestDeclined(String username) {

        String msgId = username + System.currentTimeMillis();

        // Create message save application info
        EMMessage message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
        EMTextMessageBody body = new EMTextMessageBody(username + " declined your apply");
        message.addBody(body);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_USERNAME, username);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_REASON, username + "  declined your apply");
        message.setAttribute(EaseConstant.MESSAGE_ATTR_TYPE, 0);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_STATUS, "Rejected");
        message.setFrom(EaseConstant.CONVERSATION_NAME_APPLY);
        message.setMsgId(msgId);
        // save message to db
        EMClient.getInstance().chatManager().saveMessage(message);
        // send broadcast
        BroadCastReceiverManager.getInstance(mContext).sendBroadCastReceiver(EaseConstant.BROADCAST_ACTION_APPLY);
    }
}
