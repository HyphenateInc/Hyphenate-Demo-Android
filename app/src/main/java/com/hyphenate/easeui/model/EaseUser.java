package com.hyphenate.easeui.model;

/**
 * Created by wei on 2016/10/10.
 */

public interface EaseUser{

    /**
     * get the user hyphenate id
     * @return
     */
    String getEaseUsername();

    /**
     * avatar url of the user, also can be drawable id that need convert to String
     * @return
     */
    String getEaseAvatar();

    /**
     * get the user nick
     * @return
     */
    String getEaseNickname();

}
