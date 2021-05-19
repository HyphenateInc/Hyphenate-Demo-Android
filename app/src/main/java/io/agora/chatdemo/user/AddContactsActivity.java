package io.agora.chatdemo.user;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.agora.chat.ChatClient;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;

/**
 * Created by lzan13 on 2016/10/24.
 */

public class AddContactsActivity extends BaseActivity {

    private String TAG = AddContactsActivity.class.getSimpleName();

    private BaseActivity mActivity;

    @BindView(R.id.edit_search_contacts) EditText mSearchView;
    @BindView(R.id.btn_add_contacts) Button mAddContactsBtn;

    private String addUsername;

    private ProgressDialog mDialog;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_add_contacts);

        ButterKnife.bind(this);

        mActivity = this;

        getSupportActionBar().setTitle(R.string.em_contacts_add);
        getActionBarToolbar().setNavigationIcon(R.drawable.em_ic_back);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
    }

    @OnClick(R.id.btn_add_contacts) void addContacts() {
        addUsername = mSearchView.getText().toString().trim();
        if (TextUtils.isEmpty(addUsername)) {
            Toast.makeText(mActivity, R.string.em_hint_input_not_null, Toast.LENGTH_LONG).show();
            return;
        }
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(getString(R.string.em_wait));
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    ChatClient.getInstance().contactManager().addContact(addUsername, "Add friends");
                    mDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(mActivity, R.string.em_contacts_apply_send_success,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (ChatException e) {
                    e.printStackTrace();
                    mDialog.dismiss();
                    int errorCode = e.getErrorCode();
                    String errorMsg = e.getMessage();
                    EMLog.e(TAG, "add contacts: error - " + errorCode + ", msg - " + errorMsg);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(mActivity, R.string.em_contacts_apply_send_failed,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}
