package com.hyphenate.easeui;

import com.hyphenate.easeui.model.EaseUser;

import static com.hyphenate.cloud.CloudFileManager.instance;

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



    private EaseUI(){}

    /**
     * get instance of EaseUI
     * @return
     */
    public synchronized static EaseUI getInstance(){
        if(instance == null){
            instance = new EaseUI();
        }
        return instance;
    }

    /**
     * set user profile provider
     * @param userProvider
     */
    public void setUserProfileProvider(EaseUserProfileProvider userProvider){
        this.userProvider = userProvider;
    }

    /**
     * get user profile provider
     * @return
     */
    public EaseUserProfileProvider getUserProfileProvider(){
        return userProvider;
    }

    /**
     * User profile provider
     * @author wei
     *
     */
    public interface EaseUserProfileProvider {
        /**
         * return EaseUser for input username
         * @param username
         * @return
         */
        EaseUser getUser(String username);
    }
}
