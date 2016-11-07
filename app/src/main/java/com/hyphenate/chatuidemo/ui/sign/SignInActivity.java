package com.hyphenate.chatuidemo.ui.sign;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.ui.MainActivity;
import com.hyphenate.chatuidemo.ui.user.UserEntity;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by lzan13 on 2016/10/10.
 */
public class SignInActivity extends BaseActivity {

    private BaseActivity mActivity;

    protected static final String TAG = SignInActivity.class.getSimpleName();

    // Loading dialog
    private ProgressDialog mDialog;

    // Use ButterKnife to get the control
    @BindView(R.id.edt_account) EditText mAccountView;
    @BindView(R.id.edt_password) EditText mPasswordView;

    private String mPassword;
    private String mAccount;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_sign_in);
        ButterKnife.bind(this);
    }

    @Override protected void onResume() {
        super.onResume();
        initView();
    }

    /**
     * Init layout view
     */
    private void initView() {
        mActivity = this;

    }

    /**
     * Already have an account! Go sign in
     */
    @OnClick(R.id.btn_sign_up) void signUp() {
        Intent intent = new Intent(mActivity, SignUpActivity.class);
        startActivity(intent);
    }

    /**
     * Verify the input information, Call sign in
     */
    @OnClick(R.id.btn_sign_in) void attemptSignIn() {

        // reset error
        mAccountView.setError(null);
        mPasswordView.setError(null);

        mAccount = mAccountView.getText().toString().toLowerCase().trim();
        mPassword = mPasswordView.getText().toString().toLowerCase().trim();

        boolean cancel = false;
        View focusView = null;

        // Verify that the user name and password are valid
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.em_hint_input_not_null));
            focusView = mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(mAccount)) {
            mAccountView.setError(getString(R.string.em_hint_input_not_null));
            focusView = mAccountView;
            cancel = true;
        }

        if (cancel) {
            // Let the input box get focus
            focusView.requestFocus();
        } else {
            // Call sign in account
            signIn();
        }
    }

    /**
     * Sign in account
     */
    private void signIn() {
        final Resources res = mActivity.getResources();
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(res.getString(R.string.em_sign_in_begin));
        mDialog.show();

        EMClient.getInstance().login(mAccount, mPassword, new EMCallBack() {
            /**
             * Sign in success callback
             */
            @Override public void onSuccess() {

                // Sign in success save account to shared
                //MLSPUtil.put(mActivity, MLConstants.ML_SHARED_USERNAME, mAccount);

                // Load conversation to memory
                EMClient.getInstance().chatManager().loadAllConversations();
                // Load group to memory
                EMClient.getInstance().groupManager().loadAllGroups();

                try {
                    EMClient.getInstance().groupManager().getJoinedGroupsFromServer();
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }

                try {
                    List<String> contacts =
                            EMClient.getInstance().contactManager().getAllContactsFromServer();
                    List<UserEntity> entityList = new ArrayList<>();
                    for (String name : contacts) {
                        UserEntity user = new UserEntity(name);
                        user.setInitialLetter(name.subSequence(0, 1).toString().toUpperCase());
                        entityList.add(user);
                    }
                    // sync blacklist
                    EMClient.getInstance().contactManager().getBlackListFromServer();
                    DemoHelper.getInstance().setContactList(entityList);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }

                // 关闭登录进度弹出框
                mDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        Toast.makeText(mActivity, "Sign in success!", Toast.LENGTH_LONG).show();
                    }
                });
                // Sign in success jump MainActivity
                Intent intent = new Intent(mActivity, MainActivity.class);
                startActivity(intent);
                // finish activity;
                finish();
            }

            /**
             * Sign in failed callback
             * @param i failed code
             * @param s failed message
             */
            @Override public void onError(final int i, final String s) {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        mDialog.dismiss();
                        EMLog.d(TAG, "Sign in error code: " + i + ", msg: " + s);
                        String error = "";
                        switch (i) {
                            case EMError.NETWORK_ERROR:
                                error = res.getString(R.string.em_error_network_error);
                                break;
                            case EMError.INVALID_USER_NAME:
                                error = res.getString(R.string.em_error_invalid_username);
                                break;
                            case EMError.INVALID_PASSWORD:
                                error = res.getString(R.string.em_error_invalid_password);
                                break;
                            case EMError.USER_AUTHENTICATION_FAILED:
                                error = res.getString(R.string.em_error_user_authentication_failed);
                                break;
                            case EMError.USER_NOT_FOUND:
                                error = res.getString(R.string.em_error_user_not_found);
                                break;
                            case EMError.SERVER_TIMEOUT:
                                error = res.getString(R.string.em_error_server_timeout);
                                break;
                            case EMError.SERVER_BUSY:
                                error = res.getString(R.string.em_error_server_busy);
                                break;
                            case EMError.SERVER_UNKNOWN_ERROR:
                                error = res.getString(R.string.em_error_unknown_error);
                                break;
                            default:
                                error = res.getString(R.string.em_error_sign_in_failed);
                                break;
                        }
                        Toast.makeText(mActivity, error + "-" + i + "-" + s, Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }

            @Override public void onProgress(int i, String s) {
                EMLog.d(TAG, "progress: " + i + ", msg:" + s);
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }
}
 
