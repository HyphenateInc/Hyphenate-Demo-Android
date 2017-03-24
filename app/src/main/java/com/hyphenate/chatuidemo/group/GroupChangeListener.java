package com.hyphenate.chatuidemo.group;

import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatuidemo.Constant;

import java.util.List;
import java.util.UUID;

/**
 * Created by benson on 2016/10/31.
 */

public abstract class GroupChangeListener implements EMGroupChangeListener {

    /*!
     * current user receive group invitation
     * @param s The group ID.
     * @param s1 group's name
     * @param s2 Who invite you join the group
     * @param s3 Literal message coming with the invitation
     */
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
    }

    /*
     * some one want to join the group.
     * @param s The group ID
     * @param s1 group's name
     * @param s2 The applicant want to join the group
     * @param s3 Literal message coming with the application
     */
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
    }


    /*
     * Join group's application has been approved
     * @param s The group ID
     * @param s1 group's name
     * @param s2 who approve the application
     */

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
    }

    /*!
     * Join group's application has been declined
     * @param s The group ID
     * @param s1 group's name
     * @param s2 decliner's username
     * @param s3 decline reason
     */
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
    }


    /*
     * invite some one to be member of group, and the user has accept the invitation
     * @param s The group ID
     * @param s1 invitee
     * @param s2 reason
     */

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
    }

    /*
     * invite some one to be member of group, and the user has decline the invitation
     * @param groupId The group ID
     * @param invitee
     * @param reason refuse reason
     */
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
    }

    /*!
     * current user has been remove from the group
     * @param s groupId
     * @param s1 groupName
     */
    @Override public void onUserRemoved(String s, String s1) {

    }

    /*!
     * @param s groupId
     * @param s1 groupName
     *
     * group dissolution
     * SDK will delete the group from local DB and local memory cache, then notify user this group has been destroyed
     */
    @Override public void onGroupDestroyed(String s, String s1) {

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
    }

    @Override
    public void onMuteListAdded(String var1, List<String> var2, long var3){}

    @Override
    public void onMuteListRemoved(String var1, List<String> var2){}

    @Override
    public void onAdminAdded(String var1, String var2) {}

    @Override
    public void onAdminRemoved(String var1, String var2) {}

    @Override
    public void onOwnerChanged(String var1, String var2, String var3) {}
}
