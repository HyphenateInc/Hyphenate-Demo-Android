package com.hyphenate.chatuidemo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.ui.user.UserEntity;
import com.hyphenate.exceptions.HyphenateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wei on 2016/9/27.
 */
public class DemoApplication extends Application {

    private static DemoApplication instance;

    private Context applicationContext;

    private List<UserEntity> entityList = new ArrayList<>();

    @Override public void onCreate() {
        MultiDex.install(this);
        super.onCreate();
        //		Fabric.with(this, new Crashlytics());
        instance = this;
        applicationContext = this;

        //init demo helper
        DemoHelper.getInstance().init(applicationContext);
    }

    public static DemoApplication getInstance() {
        return instance;
    }

    @Override protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    public void setContactList(UserEntity userEntity){
        entityList.add(userEntity);
    }


    public List<UserEntity> getContactList() {
        return entityList;
    }
}
