package com.hyphenate.chatuidemo.ui.user;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.model.EaseUser;
import com.hyphenate.easeui.utils.EaseCommonUtils;

/**
 * Created by benson on 2016/10/10.
 */

public class UserEntity extends EaseUser {

    public UserEntity(String username) {
        super(username);
    }

    /**
     * get EaseUser according username
     */
    private static UserEntity getUserInfo(String username) {
        // To get instance of EaseUser, here we get it from the user list in memory
        // You'd better cache it if you get it from your server
        UserEntity user;
        if (username.equals(EMClient.getInstance().getCurrentUser())) return DemoHelper.getInstance().getUserProfileManager().getCurrentUserInfo();

        user = DemoHelper.getInstance().getContactList().get(username);

        // if user is not in your contacts, set inital letter for him/her
        if (user == null) {
            user = new UserEntity(username);
            EaseCommonUtils.setUserInitialLetter(user);
        }
        return user;
    }

    /**
     * set user avatar
     */
    public static void setUserAvatar(Context context, String username, ImageView imageView) {
        EaseUser user = getUserInfo(username);
        if (user != null && user.getAvatar() != null) {
            try {
                int avatarResId = Integer.parseInt(user.getAvatar());
                Glide.with(context).load(avatarResId).into(imageView);
            } catch (Exception e) {
                //use default avatar
                Glide.with(context)
                        .load(user.getAvatar())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ease_default_avatar)
                        .into(imageView);
            }
        } else {
            Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);
        }
    }

    /**
     * set user's nickname
     */
    public static void setUserNick(String username, TextView textView) {
        if (textView != null) {
            EaseUser user = getUserInfo(username);
            if (user != null && user.getNickname() != null) {
                textView.setText(user.getNickname());
            } else {
                textView.setText(username);
            }
        }
    }
}
