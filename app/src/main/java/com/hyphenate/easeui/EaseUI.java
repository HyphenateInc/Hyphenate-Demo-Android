package com.hyphenate.easeui;

import android.content.Context;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.model.EaseUser;

/**
 * Created by wei on 2016/9/28.
 */
public class EaseUI {
    /**
     * the global EaseUI instance
     */
    private static EaseUI instance = null;

    /**
     * user profile provider
     */
    private EaseUserProfileProvider userProvider;

    /**
     * message notify settings provider
     */
    private EaseSettingsProvider settingsProvider;

    private EaseUI() {
    }

    /**
     * get instance of EaseUI
     */
    public synchronized static EaseUI getInstance() {
        if (instance == null) {
            instance = new EaseUI();
        }
        return instance;
    }

    public void init(Context context) {

    }

    /**
     * get user profile provider
     */
    public EaseUserProfileProvider getUserProfileProvider() {
        return userProvider;
    }

    /**
     * set user profile provider
     */
    public void setUserProfileProvider(EaseUserProfileProvider userProvider) {
        this.userProvider = userProvider;
    }

    public EaseSettingsProvider getSettingsProvider() {
        return settingsProvider;
    }

    /**
     * set message notify settings provider
     */
    public void setSettingsProvider(EaseSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    /**
     * User profile provider
     *
     * @author wei
     */
    public interface EaseUserProfileProvider {
        /**
         * return EaseUser for input username
         */
        EaseUser getUser(String username);
    }

    /**
     * new message options provider
     */
    public interface EaseSettingsProvider {
        boolean isMsgNotifyAllowed(EMMessage message);

        boolean isMsgSoundAllowed(EMMessage message);

        boolean isMsgVibrateAllowed(EMMessage message);

        boolean isSpeakerOpened();
    }

    /**
     * default settings provider
     */
    protected class DefaultSettingsProvider implements EaseSettingsProvider {

        @Override public boolean isMsgNotifyAllowed(EMMessage message) {
            // TODO Auto-generated method stub
            return true;
        }

        @Override public boolean isMsgSoundAllowed(EMMessage message) {
            return true;
        }

        @Override public boolean isMsgVibrateAllowed(EMMessage message) {
            return true;
        }

        @Override public boolean isSpeakerOpened() {
            return true;
        }
    }
}
