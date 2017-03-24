package com.hyphenate.chatuidemo.group;

import com.hyphenate.EMGroupChangeListener;

import java.util.List;

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
    @Override public void onInvitationReceived(String s, String s1, String s2, String s3) {}

    /*
     * some one want to join the group.
     * @param s The group ID
     * @param s1 group's name
     * @param s2 The applicant want to join the group
     * @param s3 Literal message coming with the application
     */
    @Override public void onRequestToJoinReceived(String s, String s1, String s2, String s3) {}


    /*
     * Join group's application has been approved
     * @param s The group ID
     * @param s1 group's name
     * @param s2 who approve the application
     */

    @Override public void onRequestToJoinAccepted(String s, String s1, String s2) {}

    /*!
     * Join group's application has been declined
     * @param s The group ID
     * @param s1 group's name
     * @param s2 decliner's username
     * @param s3 decline reason
     */
    @Override public void onRequestToJoinDeclined(String s, String s1, String s2, String s3) {}


    /*
     * invite some one to be member of group, and the user has accept the invitation
     * @param s The group ID
     * @param s1 invitee
     * @param s2 reason
     */

    @Override public void onInvitationAccepted(String s, String s1, String s2) {}

    /*
     * invite some one to be member of group, and the user has decline the invitation
     * @param groupId The group ID
     * @param invitee
     * @param reason refuse reason
     */
    @Override public void onInvitationDeclined(String s, String s1, String s2) {}

    /*!
     * current user has been remove from the group
     * @param s groupId
     * @param s1 groupName
     */
    @Override public void onUserRemoved(String s, String s1) {}

    /*!
     * @param s groupId
     * @param s1 groupName
     *
     * group dissolution
     * SDK will delete the group from local DB and local memory cache, then notify user this group has been destroyed
     */
    @Override public void onGroupDestroyed(String s, String s1) {}

    /*!
     * @param s groupId
     * @param s1 inviter
     * @param s2 inviteMessage
     *
     * When receive group join invitation, will auto accept it, and join the group.
     * Please refer to {@link com.hyphenate.chat.EMOptions.setAutoAcceptGroupInvitation(boolean value)}
     */
    @Override public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {}

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

    @Override
    public void onMemberJoined(final String groupId, final String member) {}

    @Override
    public void onMemberExited(final String groupId,  final String member) {}

}
