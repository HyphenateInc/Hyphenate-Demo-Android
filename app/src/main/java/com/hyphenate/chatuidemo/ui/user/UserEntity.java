package com.hyphenate.chatuidemo.ui.user;

/**
 * Created by benson on 2016/10/10.
 */

public class UserEntity {

    private String header;
    private String userId;

    private String nick;
    private String avatar;



    String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    String getNick() {
        return nick;
    }

    void setNick(String nick) {
        this.nick = nick;
    }

    String getAvatar() {
        return avatar;
    }

    void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
