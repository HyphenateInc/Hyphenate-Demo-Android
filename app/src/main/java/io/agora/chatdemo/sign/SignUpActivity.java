package io.agora.chatdemo.sign;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.agora.Error;
import io.agora.chat.ChatClient;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;

/**
 * Created by lzan13 on 2016/10/10.
 */
public class SignUpActivity extends BaseActivity {

    private BaseActivity mActivity;

    protected static final String TAG = SignUpActivity.class.getSimpleName();
    private static final int MAX_LENGTH = 64;

    // Alert dialog
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
        setContentView(R.layout.em_activity_sign_up);
        ButterKnife.bind(this);

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
                    ChatClient.getInstance().createAccount(mAccount, mPassword);
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
                } catch (final ChatException e) {
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
                                case Error.NETWORK_ERROR:
                                    error = res.getString(R.string.em_error_network_error);
                                    break;
                                case Error.USER_ALREADY_EXIST:
                                    error = res.getString(R.string.em_error_user_already_exit);
                                    break;
                                case Error.USER_ILLEGAL_ARGUMENT:
                                    error = res.getString(R.string.em_error_illegal_argument);
                                    break;
                                case Error.SERVER_UNKNOWN_ERROR:
                                    error = res.getString(R.string.em_error_unknown_error);
                                    break;
                                case Error.USER_REG_FAILED:
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
