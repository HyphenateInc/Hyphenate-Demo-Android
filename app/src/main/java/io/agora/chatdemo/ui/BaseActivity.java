package io.agora.chatdemo.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;

/**
 * Created by wei on 2016/9/27.
 */
public class BaseActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
    private Toolbar mActionBarToolbar;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //http://stackoverflow.com/questions/4341600/how-to-prevent-multiple-instances-of-an-activity-when-it-is-launched-with-differ/
        // should be in launcher activity, but all app use this can avoid the problem
        if (!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    /**
     * get the actionbar(toolbar) which view id is R.id.toolbar_actionbar
     */
    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
                mActionBarToolbar.setTitleTextColor(getResources().getColor(R.color.colorTextPrimary));
            }
        }
        return mActionBarToolbar;
    }

    @Override protected void onResume() {
        super.onResume();
        DemoHelper.getInstance().getNotifier().reset();
    }

    /**
     * set overflow icon's color
     * @param toolbar
     * @param colorId
     */
    public static void setToolbarMoreIconCustomColor(Toolbar toolbar, int colorId) {
        if(toolbar == null) {
            return;
        }
        Drawable moreIcon = ContextCompat.getDrawable(toolbar.getContext(), R.drawable.abc_ic_menu_overflow_material);
        if(moreIcon != null) {
            moreIcon.setColorFilter(ContextCompat.getColor(toolbar.getContext(), colorId), PorterDuff.Mode.SRC_ATOP);
            toolbar.setOverflowIcon(moreIcon);
        }
    }

    /**
     * hide keyboard
     */
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm!=null&&getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null){
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

}
