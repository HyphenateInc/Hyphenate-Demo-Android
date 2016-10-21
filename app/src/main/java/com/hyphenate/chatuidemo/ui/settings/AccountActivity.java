package com.hyphenate.chatuidemo.ui.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.easeui.widget.EaseImageView;

/**
 * Created by lzan13 on 2016/10/21.
 * Account info activity
 */

public class AccountActivity extends BaseActivity {

    @BindView(R.id.img_account_avatar) EaseImageView mAvatarView;
    @BindView(R.id.fab_edit_avatar) FloatingActionButton mEditAvatarFab;
    @BindView(R.id.text_hyphenate_id) TextView mHyphenateID;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_account);

        ButterKnife.bind(this);
        
        init();
    }

    /**
     * Init layout view
     */
    private void init() {
        mHyphenateID.setText(EMClient.getInstance().getCurrentUser());
    }
}
