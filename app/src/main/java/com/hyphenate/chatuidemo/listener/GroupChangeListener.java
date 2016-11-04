package com.hyphenate.chatuidemo.listener;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.easeui.EaseConstant;
import java.util.UUID;

/**
 * Created by benson on 2016/10/31.
 */

public class GroupChangeListener implements EMGroupChangeListener {

    Context mContext;

    public GroupChangeListener(Context context){
        this.mContext = context;
    }


    /*!
	 * current user receive group invitation
	 * @param s The group ID.
	 * @param s1 group's name
	 * @param s2 Who invite you join the group
	 * @param s3 Literal message coming with the invitation
	 */
    @Override public void onInvitationReceived(String s, String s1, String s2, String s3) {

        String msgId = s2 + System.currentTimeMillis();

        EMMessage message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
        EMTextMessageBody body = new EMTextMessageBody(" receive invitation to join the group：" + s1);
        message.setChatType(EMMessage.ChatType.GroupChat);
        message.addBody(body);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_GROUP_ID,s);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_USERNAME, s2);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_REASON, s3);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_TYPE, 0);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_GROUP_TYPE,0);
        message.setFrom(EaseConstant.CONVERSATION_NAME_APPLY);
        message.setMsgId(msgId);
        // save message to db
        EMClient.getInstance().chatManager().saveMessage(message);
        // send broadcast
        sendBroadcast(EaseConstant.BROADCAST_ACTION_GROUP);

    }

    /*
     * some one want to join the group.
	 * @param s The group ID
	 * @param s1 group's name
	 * @param s2 The applicant want to join the group
	 * @param s3 Literal message coming with the application
	 */
    @Override public void onRequestToJoinReceived(String s, String s1, String s2, String s3) {

        String msgId = s2 + System.currentTimeMillis();

        EMMessage message = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
        EMTextMessageBody body = new EMTextMessageBody(s2 + " Apply to join group：" + s2);
        message.setChatType(EMMessage.ChatType.GroupChat);
        message.addBody(body);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_GROUP_ID,s);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_USERNAME, s2);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_REASON, s3);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_TYPE, 0);
        message.setFrom(EaseConstant.CONVERSATION_NAME_APPLY);
        message.setAttribute(EaseConstant.MESSAGE_ATTR_GROUP_TYPE,1);
        message.setMsgId(msgId);
        // save message to db
        EMClient.getInstance().chatManager().saveMessage(message);
        // send broadcast
        sendBroadcast(EaseConstant.BROADCAST_ACTION_GROUP);

    }


    /*
	 * Join group's application has been approved
	 * @param s The group ID
	 * @param s1 group's name
	 * @param s2 who approve the application
	 */

    @Override public void onRequestToJoinAccepted(String s, String s1, String s2) {

        EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
        EMTextMessageBody body = new EMTextMessageBody(" Accepted your group application ");
        msg.setChatType(EMMessage.ChatType.GroupChat);
        msg.setFrom(s2);
        msg.setTo(s);
        msg.setMsgId(UUID.randomUUID().toString());
        msg.addBody(body);
        msg.setStatus(EMMessage.Status.SUCCESS);
        // save accept message
        EMClient.getInstance().chatManager().saveMessage(msg);

        sendBroadcast(EaseConstant.BROADCAST_ACTION_GROUP);
    }

    /*!
     * Join group's application has been declined
	 * @param s The group ID
	 * @param s1 group's name
	 * @param s2 decliner's username
	 * @param s3 decline reason
	 */
    @Override public void onRequestToJoinDeclined(String s, String s1, String s2, String s3) {

    }


    /*
     * invite some one to be member of group, and the user has accept the invitation
	 * @param s The group ID
	 * @param s1 invitee
	 * @param s2 reason
	 */

    @Override public void onInvitationAccepted(String s, String s1, String s2) {

        EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
        EMTextMessageBody body = new EMTextMessageBody(" Accepted your group application ");
        msg.setChatType(EMMessage.ChatType.GroupChat);
        msg.setFrom(s);
        msg.setTo(s1);
        msg.setMsgId(UUID.randomUUID().toString());
        msg.addBody(body);
        msg.setStatus(EMMessage.Status.SUCCESS);
        // save accept message
        EMClient.getInstance().chatManager().saveMessage(msg);

        sendBroadcast(EaseConstant.BROADCAST_ACTION_GROUP);
    }

    /*
     * invite some one to be member of group, and the user has decline the invitation
	 * @param groupId The group ID
	 * @param invitee
	 * @param reason refuse reason
	 */
    @Override public void onInvitationDeclined(String s, String s1, String s2) {

        EMMessage msg = EMMessage.createReceiveMessage(EMMessage.Type.TXT);
        EMTextMessageBody body = new EMTextMessageBody(" Declined your group application ");
        msg.setChatType(EMMessage.ChatType.GroupChat);
        msg.setMsgId(s1+0);
        msg.addBody(body);
        msg.setAttribute(EaseConstant.MESSAGE_ATTR_USERNAME, s1);
        msg.setAttribute(EaseConstant.MESSAGE_ATTR_REASON, s2);
        msg.setAttribute(EaseConstant.MESSAGE_ATTR_TYPE, 0);
        msg.setFrom(EaseConstant.CONVERSATION_NAME_APPLY);
        msg.setAttribute(EaseConstant.MESSAGE_ATTR_GROUP_TYPE,1);
        msg.setStatus(EMMessage.Status.SUCCESS);
        // save accept message
        EMClient.getInstance().chatManager().saveMessage(msg);
        sendBroadcast(EaseConstant.BROADCAST_ACTION_GROUP);
    }

    /*!
     * current user has been remove from the group
	 * @param s groupId
	 * @param s1 groupName
	 */
    @Override public void onUserRemoved(String s, String s1) {
        sendBroadcast(EaseConstant.BROADCAST_ACTION_GROUP);
    }

    /*!
     * @param s groupId
     * @param s1 groupName
     *
     * group dissolution
     * SDK will delete the group from local DB and local memory cache, then notify user this group has been destroyed
     */
    @Override public void onGroupDestroyed(String s, String s1) {
        sendBroadcast(EaseConstant.BROADCAST_ACTION_GROUP);
    }

    /*!
     * @param s groupId
     * @param s1 inviter
     * @param s2 inviteMessage
     *
     * When receive group join invitation, will auto accept it, and join the group.
     * Please refer to {@link com.hyphenate.chat.EMOptions.setAutoAcceptGroupInvitation(boolean value)}
     */
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
        sendBroadcast(EaseConstant.BROADCAST_ACTION_GROUP);
    }

    private void sendBroadcast(String action) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        localBroadcastManager.sendBroadcast(new Intent(action));
    }
}
