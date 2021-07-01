package io.agora.chatdemo.chatroom;

import java.util.List;
import io.agora.ChatRoomChangeListener;

/**
 * Created by lzan13 on 2017/5/31.
 * Chat room default change listener
 */
public class AgoraChatRoomChangeListener implements ChatRoomChangeListener {

    /**
     * callback when chat room is destroyed
     *
     * @param roomId chatroom id
     * @param roomName chatroom subject
     */
    @Override public void onChatRoomDestroyed(String roomId, String roomName) {
    }

    /**
     * Callback when a member join the chatroom.
     *
     * @param roomId chatroom id
     * @param participant new member's username
     */
    @Override public void onMemberJoined(final String roomId, final String participant) {
    }

    /**
     * Callback when a member exited the chatroom
     *
     * @param roomId chatroom id
     * @param roomName chatroom's subject
     * @param participant the member who exited the chatroom
     */
    @Override public void onMemberExited(final String roomId, final String roomName, final String participant) {
    }

    @Override
    public void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {

    }

    /**
     * Callback when chat room member(s) is muted (added to mute list), and is not allowed to post message temporarily based on
     * muted time duration
     *
     * @param chatRoomId chatroom id
     * @param mutes muted username
     * @param expireTime mute operation expired time
     * @return NA
     */
    @Override public void onMuteListAdded(final String chatRoomId, final List<String> mutes, final long expireTime) {
    }

    /**
     * Callback when chat room member(s) is unmuted (removed from mute list), and allow to post message now
     *
     * @param chatRoomId chatroom id
     * @param mutes member(s) muted was removed from the mute list
     * @return NA
     */
    @Override public void onMuteListRemoved(final String chatRoomId, final List<String> mutes) {
    }

    @Override
    public void onWhiteListAdded(String chatRoomId, List<String> whitelist) {

    }

    @Override
    public void onWhiteListRemoved(String chatRoomId, List<String> whitelist) {

    }

    @Override
    public void onAllMemberMuteStateChanged(String chatRoomId, boolean isMuted) {

    }

    /**
     * Callback when a member has been changed to admin
     *
     * @param chatRoomId chatroom id
     * @param admin member who has been changed to admin
     * @return NA
     */
    @Override public void onAdminAdded(final String chatRoomId, final String admin) {
    }

    /**
     * Callback when member is removed from admin
     *
     * @param chatRoomId chatroom id
     * @param admin the member whose admin permission is removed
     * @return NA
     */
    @Override public void onAdminRemoved(final String chatRoomId, final String admin) {
    }

    /**
     * Callback when chat room ownership has been transferred
     *
     * @param chatRoomId chatroom id
     * @param newOwner new owner
     * @param oldOwner previous owner
     * @return NA
     */
    @Override public void onOwnerChanged(final String chatRoomId, final String newOwner, final String oldOwner) {
    }

    @Override public void onAnnouncementChanged(String chatRoomId, String announcement) {

    }
}
