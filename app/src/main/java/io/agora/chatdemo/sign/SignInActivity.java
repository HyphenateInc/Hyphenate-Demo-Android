package io.agora.chatdemo.sign;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import io.agora.CallBack;
import io.agora.Error;
import io.agora.chat.ChatClient;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.chatdemo.ui.MainActivity;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.agora.util.NetUtils;

/**
 * Created by lzan13 on 2016/10/10.
 */
public class SignInActivity extends BaseActivity {

    private BaseActivity mActivity;

    protected static final String TAG = SignInActivity.class.getSimpleName();
    private static final int MAX_LENGTH = 64;

    // Loading dialog
    private ProgressDialog mDialog;

    // Use ButterKnife to get the control
    @BindView(R.id.edt_account) EditText mAccountView;
    @BindView(R.id.edt_password) EditText mPasswordView;
    @BindView(R.id.til_account) TextInputLayout mTilAccountView;
    @BindView(R.id.til_password) TextInputLayout mTilPasswordView;

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
        mTilAccountView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                byte[] bytes = s.toString().trim().getBytes();
                if(bytes.length > MAX_LENGTH) {
                    mTilAccountView.setError(getString(R.string.em_sign_account_out_limit));
                }else {
                    mTilAccountView.setError(null);
                }
            }
        });
        mTilPasswordView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                byte[] bytes = s.toString().trim().getBytes();
                if(bytes.length > MAX_LENGTH) {
                    mTilPasswordView.setError(getString(R.string.em_sign_password_out_limit));
                }else {
                    mTilPasswordView.setError(null);
                }
            }
        });
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
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        ChatClient.getInstance().login(mAccount, mPassword, new CallBack() {
            /**
             * Sign in success callback
             */
            @Override public void onSuccess() {

                // Sign in success save account to shared
                //MLSPUtil.put(mActivity, MLConstants.ML_SHARED_USERNAME, mAccount);

                // Load mConversation to memory
                ChatClient.getInstance().chatManager().loadAllConversations();
                // Load group to memory
                ChatClient.getInstance().groupManager().loadAllGroups();

                try {
                    ChatClient.getInstance().groupManager().getJoinedGroupsFromServer();
                } catch (ChatException e) {
                    e.printStackTrace();
                }

                try {
                    // sync blacklist
                    ChatClient.getInstance().contactManager().getBlackListFromServer();
                } catch (ChatException e) {
                    e.printStackTrace();
                }

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
                            case Error.NETWORK_ERROR:
                                error = res.getString(R.string.em_error_network_error);
                                break;
                            case Error.INVALID_USER_NAME:
                                error = res.getString(R.string.em_error_invalid_username);
                                break;
                            case Error.INVALID_PASSWORD:
                                error = res.getString(R.string.em_error_invalid_password);
                                break;
                            case Error.USER_AUTHENTICATION_FAILED:
                                error = res.getString(R.string.em_error_user_authentication_failed);
                                break;
                            case Error.USER_NOT_FOUND:
                                error = res.getString(R.string.em_error_user_not_found);
                                break;
                            case Error.SERVER_TIMEOUT:
                                error = res.getString(R.string.em_error_server_timeout);
                                break;
                            case Error.SERVER_BUSY:
                                error = res.getString(R.string.em_error_server_busy);
                                break;
                            case Error.SERVER_UNKNOWN_ERROR:
                                error = res.getString(R.string.em_error_unknown_error);
                                break;
                            default:
                                error = res.getString(R.string.em_error_sign_in_failed);
                                break;
                        }

                        if (!NetUtils.hasNetwork(mActivity)) {
                            Toast.makeText(mActivity, R.string.em_error_network_error,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(mActivity, error + "-" + i + "-" + s, Toast.LENGTH_LONG)
                                    .show();
                        }
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
 
