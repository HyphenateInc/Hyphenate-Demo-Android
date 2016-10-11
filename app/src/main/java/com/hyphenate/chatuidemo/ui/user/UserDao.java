package com.hyphenate.chatuidemo.ui.user;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.hyphenate.util.HanziToPinyin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by benson on 2016/10/11.
 */

public class UserDao {

    static final String TABLE_NAME = "users";
    static final String COLUMN_NAME_ID = "userId";
    static final String COLUMN_NAME_NICK = "nick";
    static final String COLUMN_NAME_AVATAR = "avatar";

    private static UserDao instance;
    private static OpenHelper openHelper;

    public static UserDao getInstance(Context context) {
        if (instance == null) {
            instance = new UserDao();
        }
        if (openHelper == null) {
            openHelper = OpenHelper.getInstance(context);
        }
        return instance;
    }

    public void saveContactList(List<UserEntity> contactList) {
        {
            SQLiteDatabase db = openHelper.getWritableDatabase();
            if (db.isOpen()) {
                db.delete(UserDao.TABLE_NAME, null, null);
                for (UserEntity user : contactList) {
                    ContentValues values = new ContentValues();
                    values.put(UserDao.COLUMN_NAME_ID, user.getUserId());
                    if (user.getNick() != null) {
                        values.put(UserDao.COLUMN_NAME_NICK, user.getNick());
                    }
                    if (user.getAvatar() != null) {
                        values.put(UserDao.COLUMN_NAME_AVATAR, user.getAvatar());
                    }
                    db.replace(UserDao.TABLE_NAME, null, values);
                }
            }
        }
    }

    public Map<String, UserEntity> getContactList() {

        {
            SQLiteDatabase db = openHelper.getReadableDatabase();
            Map<String, UserEntity> users = new HashMap<>();
            if (db.isOpen()) {
                Cursor cursor =
                        db.rawQuery("select * from " + UserDao.TABLE_NAME, null);
                while (cursor.moveToNext()) {
                    String userId = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_ID));
                    String nick = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_NICK));
                    String avatar =
                            cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_AVATAR));
                    UserEntity user = new UserEntity();
                    user.setUserId(userId);
                    user.setNick(nick);
                    user.setAvatar(avatar);
                    String headerName;
                    if (!TextUtils.isEmpty(user.getNick())) {
                        headerName = user.getNick();
                    } else {
                        headerName = user.getUserId();
                    }

                    if (Character.isDigit(headerName.charAt(0))) {
                        user.setHeader("#");
                    } else {
                        user.setHeader(HanziToPinyin.getInstance()
                                .get(headerName.substring(0, 1))
                                .get(0).target.substring(0, 1).toUpperCase());
                        char header = user.getHeader().toLowerCase().charAt(0);
                        if (header < 'a' || header > 'z') {
                            user.setHeader("#");
                        }
                    }
                    users.put(userId, user);
                }
                cursor.close();
            }
            return users;
        }
    }
}
