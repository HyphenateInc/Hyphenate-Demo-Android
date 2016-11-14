package com.hyphenate.chatuidemo.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.ui.MainActivity;
import com.hyphenate.chatuidemo.ui.sign.SignInActivity;
import com.hyphenate.easeui.widget.EaseImageView;
import java.util.List;

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
        getSupportActionBar().setTitle("Account");
        getActionBarToolbar().setNavigationIcon(R.drawable.em_ic_back);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
    }

    @OnClick(R.id.btn_sign_out) void signOut() {
        DemoHelper.getInstance().signOut(true, new EMCallBack() {
            @Override public void onSuccess() {
                startActivity(new Intent(AccountActivity.this, MainActivity.class).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK));
            }

            @Override public void onError(int i, String s) {

            }

            @Override public void onProgress(int i, String s) {

            }
        });
    }
}
