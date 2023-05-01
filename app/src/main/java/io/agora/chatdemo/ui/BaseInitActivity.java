package io.agora.chatdemo.ui;

import android.view.View;

import androidx.appcompat.widget.Toolbar;

public class BaseInitActivity extends BaseActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        // Add setNavigationOnClickListener
        Toolbar toolbar = getActionBarToolbar();
        if(toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }
}

