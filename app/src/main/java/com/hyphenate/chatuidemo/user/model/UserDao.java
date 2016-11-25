package com.hyphenate.chatuidemo.user.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
    private static DBOpenHelper openHelper;

    public static UserDao getInstance(Context context) {
        if (instance == null) {
            instance = new UserDao();
        }
        if (openHelper == null) {
            openHelper = DBOpenHelper.getInstance(context);
        }
        return instance;
    }

    /**
     * Save User to db
     *
     * @param user Save UserEntity
     */
    public void saveContact(UserEntity user) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        if (db.isOpen()) {
            ContentValues values = new ContentValues();
            values.put(UserDao.COLUMN_NAME_ID, user.getUsername());
            if (user.getNickname() != null) {
                values.put(UserDao.COLUMN_NAME_NICK, user.getNickname());
            }
            if (user.getAvatar() != null) {
                values.put(UserDao.COLUMN_NAME_AVATAR, user.getAvatar());
            }
            db.replace(UserDao.TABLE_NAME, null, values);
        }
    }

    public void saveContactList(List<UserEntity> contactList) {
        {
            SQLiteDatabase db = openHelper.getWritableDatabase();
            if (db.isOpen()) {
                db.delete(UserDao.TABLE_NAME, null, null);
                for (UserEntity user : contactList) {
                    ContentValues values = new ContentValues();
                    values.put(UserDao.COLUMN_NAME_ID, user.getUsername());
                    if (user.getNickname() != null) {
                        values.put(UserDao.COLUMN_NAME_NICK, user.getNickname());
                    }
                    if (user.getAvatar() != null) {
                        values.put(UserDao.COLUMN_NAME_AVATAR, user.getAvatar());
                    }
                    db.replace(UserDao.TABLE_NAME, null, values);
                }
            }
        }
    }

    /**
     * Delete user from db
     *
     * @param user delete UserEntity
     */
    public void deleteContact(UserEntity user) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        if (db.isOpen()) {
            String whereClause = COLUMN_NAME_ID + "=?";
            String[] whereArgs = { user.getUsername() };
            db.delete(UserDao.TABLE_NAME, whereClause, whereArgs);
        }
    }

    public Map<String, UserEntity> getContactList() {

        {
            SQLiteDatabase db = openHelper.getReadableDatabase();
            Map<String, UserEntity> users = new HashMap<>();
            if (db.isOpen()) {
                Cursor cursor = db.rawQuery("select * from " + UserDao.TABLE_NAME, null);
                while (cursor.moveToNext()) {
                    String userId = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_ID));
                    String nick = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_NICK));
                    String avatar = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_AVATAR));
                    UserEntity user = new UserEntity(userId);
                    user.setNickname(nick);
                    user.setAvatar(avatar);
                    users.put(userId, user);
                }
                cursor.close();
            }
            return users;
        }
    }

    synchronized public void closeDB() {
        if (openHelper != null) {
            openHelper.closeDB();
        }
    }
}
