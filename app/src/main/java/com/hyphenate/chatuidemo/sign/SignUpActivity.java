package com.hyphenate.chatuidemo.sign;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

/**
 * Created by lzan13 on 2016/10/10.
 */
public class SignUpActivity extends BaseActivity {

    private BaseActivity mActivity;

    protected static final String TAG = SignUpActivity.class.getSimpleName();

    // Alert dialog
    private ProgressDialog mDialog;

    // Use ButterKnife to get the control
    @BindView(R.id.edt_account) EditText mAccountView;
    @BindView(R.id.edt_password) EditText mPasswordView;

    private String mPassword;
    private String mAccount;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_sign_up);
        ButterKnife.bind(this);

        initView();
    }

    /**
     * Init layout view
     */
    private void initView() {
        mActivity = this;
    }

    /**
     * Don't have an account? Go sign up
     */
    @OnClick(R.id.btn_sign_in) void signIn() {
        finish();
    }

    /**
     * Verify the input information, Call sign up
     */
    @OnClick(R.id.btn_sign_up) void attemptSignUp() {

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
            // Call sign up account
            signUp();
        }
    }

    /**
     * Sign up account
     */
    private void signUp() {
        final Resources res = mActivity.getResources();
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(res.getString(R.string.em_sign_up_begin));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMClient.getInstance().createAccount(mAccount, mPassword);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (!mActivity.isFinishing()) {
                                mDialog.dismiss();
                            }
                            // Sign up success, save account to shared
                            //MLSPUtil.put(mActivity, MLConstants.ML_SHARED_USERNAME, mAccount);
                            Toast.makeText(mActivity, "Sign up success!", Toast.LENGTH_LONG).show();
                            // Sign up succeed, go to sign in
                            finish();
                        }
                    });
                } catch (final HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            if (!mActivity.isFinishing()) {
                                mDialog.dismiss();
                            }
                            int errorCode = e.getErrorCode();
                            EMLog.d(TAG,
                                    "Sign up error code:" + errorCode + ", msg:" + e.getMessage());
                            String error = "";
                            switch (errorCode) {
                                case EMError.NETWORK_ERROR:
                                    error = res.getString(R.string.em_error_network_error);
                                    break;
                                case EMError.USER_ALREADY_EXIST:
                                    error = res.getString(R.string.em_error_user_already_exit);
                                    break;
                                case EMError.USER_ILLEGAL_ARGUMENT:
                                    error = res.getString(R.string.em_error_illegal_argument);
                                    break;
                                case EMError.SERVER_UNKNOWN_ERROR:
                                    error = res.getString(R.string.em_error_unknown_error);
                                    break;
                                case EMError.USER_REG_FAILED:
                                    error = res.getString(R.string.em_error_sign_up_failed);
                                    break;
                                default:
                                    error = res.getString(R.string.em_error_sign_up_failed);
                                    break;
                            }
                            Toast.makeText(mActivity,
                                    error + "-" + errorCode + "-" + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    @Override protected void onResume() {
        super.onResume();
    }
}
