package com.hyphenate.easeui.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.hyphenate.chatuidemo.DemoApplication;

/**
 * Created by wei on 2016/11/9.
 */

public class Utils {
    public static void hideKeyboard(View view){
        InputMethodManager
                imm = (InputMethodManager) DemoApplication.getInstance().getApplicationContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) DemoApplication.getInstance().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }
}
