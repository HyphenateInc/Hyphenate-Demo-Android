package com.hyphenate.chatuidemo.user.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.hyphenate.chat.EMClient;


/**
 * Created by benson on 2016/10/11.
 */

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static DBOpenHelper instance;

    private static final String USERNAME_TABLE_CREATE = "CREATE TABLE "
            + UserDao.TABLE_NAME
            + " ("
            + UserDao.COLUMN_NAME_ID
            + " TEXT PRIMARY KEY, "
            + UserDao.COLUMN_NAME_NICK
            + " TEXT, "
            + UserDao.COLUMN_NAME_AVATAR
            + " TEXT);";

    DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
            int version) {
        super(context, name, factory, version);
    }

    public static DBOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBOpenHelper(context,getUserDatabaseName(),null,DATABASE_VERSION);
        }
        return instance;
    }

    private static String getUserDatabaseName() {
        return  EMClient.getInstance().getCurrentUser() + "_demo.db";
    }

    @Override public void onCreate(SQLiteDatabase db) {

        db.execSQL(USERNAME_TABLE_CREATE);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void closeDB() {
        if (instance != null) {
            try {
                SQLiteDatabase db = instance.getWritableDatabase();
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            instance = null;
        }
    }
}
