package com.hyphenate.chatuidemo.user.model;

import android.text.TextUtils;
import com.hyphenate.easeui.model.EaseUser;
import com.hyphenate.util.HanziToPinyin;
import java.util.ArrayList;

/**
 * Created by benson on 2016/10/10.
 */

public class UserEntity implements EaseUser {
    private String username; // hyphenate id
    private String nickname;
    private String avatar;
    private String initialLetter;

    public UserEntity(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        if (nickname != null){
            return nickname;
        }
        return username;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getInitialLetter() {
        if(initialLetter == null){
            setUserInitialLetter();
        }
        return initialLetter;
    }

    public void setInitialLetter(String initialLetter) {
        this.initialLetter = initialLetter;
    }

    @Override public String getEaseUsername() {
        return username;
    }

    @Override public String getEaseAvatar() {
        return avatar;
    }

    @Override public String getEaseNickname() {
        return nickname;
    }


    /**
     * set initial letter of according user's nickname( username if no nickname)
     *
     */
    private void setUserInitialLetter() {
        final String DefaultLetter = "#";
        String letter = DefaultLetter;

        final class GetInitialLetter {
            String getLetter(String name) {
                if (TextUtils.isEmpty(name)) {
                    return DefaultLetter;
                }
                char char0 = name.toLowerCase().charAt(0);
                if (Character.isDigit(char0)) {
                    return DefaultLetter;
                }
                ArrayList<HanziToPinyin.Token>
                        l = HanziToPinyin.getInstance().get(name.substring(0, 1));
                if (l != null && l.size() > 0 && l.get(0).target.length() > 0)
                {
                    HanziToPinyin.Token token = l.get(0);
                    String letter = token.target.substring(0, 1).toUpperCase();
                    char c = letter.charAt(0);
                    if (c < 'A' || c > 'Z') {
                        return DefaultLetter;
                    }
                    return letter;
                }
                return DefaultLetter;
            }
        }

        if ( !TextUtils.isEmpty(nickname) ) {
            initialLetter = new GetInitialLetter().getLetter(nickname);
            return;
        }
        if (letter == DefaultLetter && !TextUtils.isEmpty(username)) {
            letter = new GetInitialLetter().getLetter(username);
        }
        initialLetter = letter;
    }
}
