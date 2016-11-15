package com.hyphenate.chatuidemo.ui.user.parse;

import android.content.Context;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.PreferenceManager;
import com.hyphenate.chatuidemo.ui.user.UserEntity;
import java.util.List;

public class UserProfileManager {

    /**
     * init flag: test if the sdk has been inited before, we don't need to init
     * again
     */
    private boolean sdkInited = false;

    private UserEntity currentUser;

    public UserProfileManager() {
    }

    public synchronized boolean init(Context context) {
        if (sdkInited) {
            return true;
        }
        ParseManager.getInstance().onInit(context);
        sdkInited = true;
        return true;
    }

    public void asyncFetchContactsInfoFromServer(List<String> hxIdList, final EMValueCallBack<List<UserEntity>> callback) {
        ParseManager.getInstance().getContactsInfo(hxIdList, new EMValueCallBack<List<UserEntity>>() {

            @Override public void onSuccess(List<UserEntity> value) {
                // in case that logout already before server returns,we should
                // return immediately
                if (!EMClient.getInstance().isLoggedInBefore()) {
                    return;
                }
                if (callback != null) {
                    callback.onSuccess(value);
                }
            }

            @Override public void onError(int error, String errorMsg) {
                if (callback != null) {
                    callback.onError(error, errorMsg);
                }
            }
        });
    }

    public synchronized void reset() {
        currentUser = null;
        PreferenceManager.getInstance().removeCurrentUserInfo();
    }

    public synchronized UserEntity getCurrentUserInfo() {
        if (currentUser == null) {
            String username = EMClient.getInstance().getCurrentUser();
            currentUser = new UserEntity(username);
            String nick = getCurrentUserNick();
            currentUser.setNickname((nick != null) ? nick : username);
            currentUser.setAvatar(getCurrentUserAvatar());
        }
        return currentUser;
    }

    public boolean updateCurrentUserNickName(final String nickname) {
        boolean isSuccess = ParseManager.getInstance().updateParseNickName(nickname);
        if (isSuccess) {
            setCurrentUserNick(nickname);
        }
        return isSuccess;
    }

    public String uploadUserAvatar(byte[] data) {
        String avatarUrl = ParseManager.getInstance().uploadParseAvatar(data);
        if (avatarUrl != null) {
            setCurrentUserAvatar(avatarUrl);
        }
        return avatarUrl;
    }

    public void asyncGetCurrentUserInfo() {
        ParseManager.getInstance().asyncGetCurrentUserInfo(new EMValueCallBack<UserEntity>() {

            @Override public void onSuccess(UserEntity value) {
                if (value != null) {
                    setCurrentUserNick(value.getNickname());
                    setCurrentUserAvatar(value.getAvatar());
                }
            }

            @Override public void onError(int error, String errorMsg) {

            }
        });
    }

    public void asyncGetUserInfo(final String username, final EMValueCallBack<UserEntity> callback) {
        ParseManager.getInstance().asyncGetUserInfo(username, callback);
    }

    private void setCurrentUserNick(String nickname) {
        getCurrentUserInfo().setNickname(nickname);
        PreferenceManager.getInstance().setCurrentUserNick(nickname);
    }

    private void setCurrentUserAvatar(String avatar) {
        getCurrentUserInfo().setAvatar(avatar);
        PreferenceManager.getInstance().setCurrentUserAvatar(avatar);
    }

    private String getCurrentUserNick() {
        return PreferenceManager.getInstance().getCurrentUserNick();
    }

    private String getCurrentUserAvatar() {
        return PreferenceManager.getInstance().getCurrentUserAvatar();
    }
}
