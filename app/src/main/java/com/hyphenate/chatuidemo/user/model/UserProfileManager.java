package com.hyphenate.chatuidemo.user.model;

import android.content.Context;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.PreferenceManager;
import com.hyphenate.chatuidemo.user.model.parse.ParseManager;
import com.hyphenate.exceptions.HyphenateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileManager {

    private boolean inited = false;

    // Contacts map
    private Map<String, UserEntity> mUsersMap = new HashMap<>();

    private Context mContext;

    private UserEntity currentUser;

    public UserProfileManager() {
    }

    public synchronized void init(Context context) {
        if (inited) {
            return;
        }
        mContext = context;
        ParseManager.getInstance().onInit(context);
        inited = true;
    }

    /**
     * get contact list from cache(memory or db)
     * @return
     */
    public Map<String, UserEntity> getContactList() {
        if (mUsersMap.isEmpty()) {
            mUsersMap = UserDao.getInstance(mContext).getContactList();
        }
        return mUsersMap;
    }

    /**
     * set contact list to cache
     * @param entityList
     */
    public void setContactList(List<UserEntity> entityList) {
        mUsersMap.clear();
        for (UserEntity userEntity : entityList) {
            mUsersMap.put(userEntity.getUsername(), userEntity);
        }
        UserDao.getInstance(mContext).saveContactList(entityList);
    }

    /**
     * save a contact to cache
     * @param userEntity
     */
    public void saveContact(UserEntity userEntity) {
        if (mUsersMap != null) {
            mUsersMap.put(userEntity.getUsername(), userEntity);
        }

        UserDao.getInstance(mContext).saveContact(userEntity);
    }

    /**
     * remove user from db
     */
    public void deleteContact(UserEntity userEntity) {
        if (mUsersMap != null) {
            mUsersMap.remove(userEntity.getUsername());
        }

        UserDao.getInstance(mContext).deleteContact(userEntity);
    }

    /**
     * async fetch contact list from server,
     * will get id list from hyphenate and get details from parse server
     * @param callback
     */
    public void fetchContactsFromServer(final EMCallBack callback) {
        new Thread(new Runnable() {
            @Override public void run() {
                List<String> hyphenateIdList;
                try {
                    hyphenateIdList = EMClient.getInstance().contactManager().getAllContactsFromServer();

                    getContactList().clear();
                    final List<UserEntity> entityList = new ArrayList<>();
                    for (String userId : hyphenateIdList) {
                        UserEntity user = new UserEntity(userId);
                        entityList.add(user);
                    }

                    getContactsInfoFromServer(hyphenateIdList, new EMValueCallBack<List<UserEntity>>() {

                        @Override public void onSuccess(List<UserEntity> uList) {
                            // save the contact list to cache
                            if (uList.size() < entityList.size()) {
                                setContactList(entityList);
                            }else{
                                setContactList(uList);
                            }
                            if (callback != null) {
                                callback.onSuccess();
                            }
                        }

                        @Override public void onError(int error, String errorMsg) {
                            setContactList(entityList);
                            if (callback != null) {
                                callback.onError(error, errorMsg);
                            }
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError(e.getErrorCode(), e.toString());
                    }
                }
            }
        }).start();
    }

    private void getContactsInfoFromServer(List<String> hyphenateIdList, final EMValueCallBack<List<UserEntity>> callback) {
        ParseManager.getInstance().getContactsInfo(hyphenateIdList, new EMValueCallBack<List<UserEntity>>() {

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
        getContactList().clear();
        UserDao.getInstance(mContext).closeDB();
        PreferenceManager.getInstance().removeCurrentUserInfo();
    }

    /**
     * get the current logged user info
     * @return
     */
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

    /**
     * async fetch user info from server
     */
    public void asyncFetchCurrentUserInfo() {
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


    public void asyncGetUserInfo(final String username, final EMValueCallBack<UserEntity> callback) {
        ParseManager.getInstance().asyncGetUserInfo(username, callback);
    }

    private void setCurrentUserNick(String nickname) {
        getCurrentUserInfo().setNickname(nickname);

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

    /**
     * convert username to user entity
     * @return
     */
    public static List<UserEntity> convertContactList(List<String> list) {

        Map<String, UserEntity> mUsersMap = DemoHelper.getInstance().getUserManager().getContactList();

        List<UserEntity> result = new ArrayList<>();
        for (String user : list) {
            if (mUsersMap.containsKey(user)) {
                result.add(mUsersMap.get(user));
            } else {
                result.add(new UserEntity(user));
            }
        }
        return result;
    }
}
