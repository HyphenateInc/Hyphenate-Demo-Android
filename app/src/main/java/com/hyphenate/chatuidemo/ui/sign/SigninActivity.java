package com.hyphenate.chatuidemo.ui.sign;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoApplication;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.ui.MainActivity;
import com.hyphenate.chatuidemo.ui.user.UserDao;
import com.hyphenate.chatuidemo.ui.user.UserEntity;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzan13 on 2016/10/10.
 */
public class SigninActivity extends BaseActivity {

    private BaseActivity mActivity;

    private final String TAG = "SignupAcitivity";

    // Loading dialog
    private ProgressDialog mDialog;

    // Use ButterKnife to get the control
    @BindView(R.id.edt_account) EditText mAccountView;
    @BindView(R.id.edt_password) EditText mPasswordView;
    @BindView(R.id.btn_sign_in) View mSigninBtn;
    @BindView(R.id.btn_sign_up) View mSignupBtn;

    private String mPassword;
    private String mAccount;

    // Use ButterKnife to get the string res
    @BindString(R.string.em_sign_in_begin) String signinBegin;
    @BindString(R.string.em_error_invalid_password) String invalidPassword;
    @BindString(R.string.em_error_invalid_username) String invalidUsername;
    @BindString(R.string.em_error_network_error) String networkError;
    @BindString(R.string.em_error_server_busy) String serverBusy;
    @BindString(R.string.em_error_server_timeout) String serverTimeout;
    @BindString(R.string.em_error_user_not_found) String userNotFound;
    @BindString(R.string.em_error_user_authentication_failed) String userAuthenticationFailed;
    @BindString(R.string.em_error_unknown_error) String unknownError;
    @BindString(R.string.em_error_sign_in_failed) String signinFailed;

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
     * 界面UI初始化方法，一般是为了先通过 findViewById 实例化控件
     */
    private void initView() {
        mActivity = this;

        //mAccountView.setText(mAccount);
        //mPasswordView.setText(mPassword);

    }

    /**
     * Already have an account! Go sign in
     */
    @OnClick(R.id.btn_sign_up) void signup() {
        Intent intent = new Intent(mActivity, SignupActivity.class);
        startActivity(intent);
    }

    /**
     * Verify the input information, Call sign in
     */
    @OnClick(R.id.btn_sign_in) void attemptSignin() {

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
            signin();
        }
    }

    /**
     * Sign in account
     */
    private void signin() {
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(signinBegin);
        mDialog.show();

        EMClient.getInstance().login(mAccount, mPassword, new EMCallBack() {
            /**
             * Sign in success callback
             */
            @Override public void onSuccess() {

                // 登录成功，把用户名保存在本地（可以不保存，根据自己的需求）
                //MLSPUtil.put(mActivity, MLConstants.ML_SHARED_USERNAME, mAccount);

                // 加载所有会话到内存
                EMClient.getInstance().chatManager().loadAllConversations();
                // 加载所有群组到内存
                EMClient.getInstance().groupManager().loadAllGroups();

                try {
                    List<String> contacts = EMClient.getInstance().contactManager().getAllContactsFromServer();
                    List<UserEntity> entityList = new ArrayList<>();
                    for (String name : contacts) {
                        UserEntity user = new UserEntity(name);
                        user.setInitialLetter(name.subSequence(0, 1).toString().toUpperCase());
                        entityList.add(user);
                    }
                    DemoApplication.getInstance().setContactList(entityList);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }

                // 关闭登录进度弹出框
                mDialog.dismiss();

                //Toast.makeText(mActivity, "Sign in success!", Toast.LENGTH_LONG).show();
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
                        /**
                         * More error code
                         * http://www.easemob.com/apidoc/android/chat3.0/classcom_1_1hyphenate_1_1_e_m_error.html
                         */
                        String error = "";
                        switch (i) {
                            // 网络异常 2
                            case EMError.NETWORK_ERROR:
                                error = networkError;
                                break;
                            // 无效的用户名 101
                            case EMError.INVALID_USER_NAME:
                                error = invalidUsername;
                                break;
                            // 无效的密码 102
                            case EMError.INVALID_PASSWORD:
                                error = invalidPassword;
                                break;
                            // 用户认证失败，用户名或密码错误 202
                            case EMError.USER_AUTHENTICATION_FAILED:
                                error = userAuthenticationFailed;
                                break;
                            // 用户不存在 204
                            case EMError.USER_NOT_FOUND:
                                error = userNotFound;
                                break;
                            // 等待服务器响应超时 301
                            case EMError.SERVER_TIMEOUT:
                                error = serverTimeout;
                                break;
                            // 服务器繁忙 302
                            case EMError.SERVER_BUSY:
                                error = serverBusy;
                                break;
                            case EMError.SERVER_UNKNOWN_ERROR:
                                error = unknownError;
                                break;
                            default:
                                error = signinFailed;
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
 
