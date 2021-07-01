package io.agora.chatdemo.user.model;

import android.content.Context;
import io.agora.CallBack;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.PreferenceManager;
import io.agora.exceptions.ChatException;
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
    public void fetchContactsFromServer(final CallBack callback) {
        new Thread(new Runnable() {
            @Override public void run() {
                List<String> hyphenateIdList;
                try {
                    hyphenateIdList = ChatClient.getInstance().contactManager().getAllContactsFromServer();

                    getContactList().clear();
                    final List<UserEntity> entityList = new ArrayList<>();
                    for (String userId : hyphenateIdList) {
                        UserEntity user = new UserEntity(userId);
                        entityList.add(user);
                    }
                    setContactList(entityList);
                    if(callback != null) {
                        callback.onSuccess();
                    }

                } catch (ChatException e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onError(e.getErrorCode(), e.toString());
                    }
                }
            }
        }).start();
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
            String username = ChatClient.getInstance().getCurrentUser();
            currentUser = new UserEntity(username);
            String nick = getCurrentUserNick();
            currentUser.setNickname((nick != null) ? nick : username);
            currentUser.setAvatar(getCurrentUserAvatar());
        }
        return currentUser;
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
