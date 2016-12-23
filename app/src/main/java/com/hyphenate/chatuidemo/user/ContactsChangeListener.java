package com.hyphenate.chatuidemo.user;

import com.hyphenate.EMContactListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.user.model.UserEntity;

/**
 * Created by lzan13 on 2016/10/24.
 * Contacts change listener
 */

public abstract class ContactsChangeListener implements EMContactListener {

    /**
     * Added contacts
     *
     * @param username Added username
     */
    @Override public void onContactAdded(String username) {
        UserEntity userEntity = new UserEntity(username);

        DemoHelper.getInstance().getUserManager().saveContact(userEntity);
    }

    /**
     * Deleted Contacts
     *
     * @param username Deleted username
     */
    @Override public void onContactDeleted(String username) {
        UserEntity userEntity = new UserEntity(username);

        DemoHelper.getInstance().getUserManager().deleteContact(userEntity);
    }

    /**
     * Received a friend request
     *
     * @param username The requestor's username
     * @param reason request reason
     */
    @Override public void onContactInvited(String username, String reason) {
        String msgId = username + EMClient.getInstance().getCurrentUser();
        EMMessage message = EMClient.getInstance().chatManager().getMessage(msgId);
        if (message != null) {
            message.setAttribute(Constant.MESSAGE_ATTR_REASON, reason);
            message.setAttribute(Constant.MESSAGE_ATTR_STATUS, "");
            message.setMsgTime(System.currentTimeMillis());
            message.setLocalTime(message.getMsgTime());
            message.setUnread(true);
            // update message
            EMClient.getInstance().chatManager().updateMessage(message);
        } else {
            // Create message save application info
            message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            EMTextMessageBody body = new EMTextMessageBody(username + " Apply to become friends");
            message.addBody(body);
            message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, username);
            message.setAttribute(Constant.MESSAGE_ATTR_REASON, reason);
            message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 0);
            message.setFrom(Constant.CONVERSATION_NAME_APPLY);
            message.setMsgId(msgId);
            // save message to db
            EMClient.getInstance().chatManager().saveMessage(message);
        }
    }

    /**
     * Accepted friend request
     *
     * @param username The requestor's username
     */
    @Override public void onFriendRequestAccepted(String username) {
        String msgId = username + EMClient.getInstance().getCurrentUser();
        EMMessage message = EMClient.getInstance().chatManager().getMessage(msgId);
        if (message != null) {
            message.setAttribute(Constant.MESSAGE_ATTR_REASON,
                    username + " agrees with your apply");
            message.setAttribute(Constant.MESSAGE_ATTR_STATUS, "Agreed");
            message.setMsgTime(System.currentTimeMillis());
            message.setLocalTime(message.getMsgTime());
            // update message
            EMClient.getInstance().chatManager().updateMessage(message);
        } else {
            // Create message save application info
            message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            EMTextMessageBody body = new EMTextMessageBody(username + " agrees with your apply");
            message.addBody(body);
            message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, username);
            message.setAttribute(Constant.MESSAGE_ATTR_REASON,
                    username + " agrees with your apply");
            message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 0);
            message.setAttribute(Constant.MESSAGE_ATTR_STATUS, "Agreed");
            message.setFrom(Constant.CONVERSATION_NAME_APPLY);
            message.setMsgId(msgId);
            // save message to db
            EMClient.getInstance().chatManager().saveMessage(message);
        }
    }

    /**
     * Declined friend request
     *
     * @param username The requestor's username
     */
    @Override public void onFriendRequestDeclined(String username) {

        String msgId = username + EMClient.getInstance().getCurrentUser();
        EMMessage message = EMClient.getInstance().chatManager().getMessage(msgId);
        if (message != null) {
            message.setAttribute(Constant.MESSAGE_ATTR_REASON, username + " declined your apply");
            message.setAttribute(Constant.MESSAGE_ATTR_STATUS, "Rejected");
            message.setMsgTime(System.currentTimeMillis());
            message.setLocalTime(message.getMsgTime());
            // update message
            EMClient.getInstance().chatManager().updateMessage(message);
        } else {
            // Create message save application info
            message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
            EMTextMessageBody body = new EMTextMessageBody(username + " declined your apply");
            message.addBody(body);
            message.setAttribute(Constant.MESSAGE_ATTR_USERNAME, username);
            message.setAttribute(Constant.MESSAGE_ATTR_REASON, username + "  declined your apply");
            message.setAttribute(Constant.MESSAGE_ATTR_TYPE, 0);
            message.setAttribute(Constant.MESSAGE_ATTR_STATUS, "Rejected");
            message.setFrom(Constant.CONVERSATION_NAME_APPLY);
            message.setMsgId(msgId);
            // save message to db
            EMClient.getInstance().chatManager().saveMessage(message);
        }
    }
}
