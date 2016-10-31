package com.hyphenate.chatuidemo.ui.apply;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.exceptions.HyphenateException;
import java.util.Collections;

/**
 * Created by lzan13 on 2016/10/26.
 * Apply for info activity
 */

public class ApplyActivity extends BaseActivity {

    private BaseActivity mActivity;

    @BindView(R.id.recycler_view_apply) RecyclerView mRecyclerView;

    private EMConversation mConversation;

    private int mPageSize = 30;

    private ApplyAdapter mApplyAdapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.em_activity_apply);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * setup ui
     */
    private void initView() {
        mActivity = this;

        getSupportActionBar().setTitle(R.string.em_contacts_apply);
        getActionBarToolbar().setNavigationIcon(R.drawable.em_ic_back);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
        mConversation = EMClient.getInstance()
                .chatManager()
                .getConversation(EaseConstant.CONVERSATION_NAME_APPLY, null, true);
        mConversation.markAllMessagesAsRead();
        int count = mConversation.getAllMessages().size();
        if (count < mConversation.getAllMsgCount() && count < mPageSize) {
            String msgId = mConversation.getAllMessages().get(0).getMsgId();
            mConversation.loadMoreMsgFromDB(msgId, mPageSize - count);
        }

        mApplyAdapter = new ApplyAdapter(mActivity);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        mRecyclerView.setAdapter(mApplyAdapter);

        setItemClickListener();
    }

    /**
     * Set item click listener
     */
    private void setItemClickListener() {
        mApplyAdapter.setItemClickListener(new ApplyAdapter.ItemClickListener() {
            /**
             * Item action event
             *
             * @param msgId item message id
             * @param action item action
             */
            @Override public void onItemAction(String msgId, int action) {
                switch (action) {
                    case 0:
                        agreeApply(msgId);
                        break;
                    case 1:
                        rejectApply(msgId);
                        break;
                }
            }
        });
    }

    /**
     * Agree contacts apply for
     */
    private void agreeApply(final String msgId) {
        final ProgressDialog dialog = new ProgressDialog(mActivity);
        dialog.setMessage(mActivity.getResources().getString(R.string.em_wait));
        dialog.show();

        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMMessage message = mConversation.getMessage(msgId, false);
                    EMClient.getInstance()
                            .contactManager()
                            .acceptInvitation(
                                    message.getStringAttribute(EaseConstant.MESSAGE_ATTR_USERNAME,
                                            ""));

                    // update contacts apply for message status
                    message.setAttribute(EaseConstant.MESSAGE_ATTR_STATUS,
                            mActivity.getString(R.string.em_agreed));
                    EMClient.getInstance().chatManager().updateMessage(message);

                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(mActivity, R.string.em_agreed, Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            refresh();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * reject contacts apply for
     */
    private void rejectApply(final String msgId) {
        final ProgressDialog dialog = new ProgressDialog(mActivity);
        dialog.setMessage(mActivity.getResources().getString(R.string.em_wait));
        dialog.show();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMMessage message = mConversation.getMessage(msgId, false);
                    EMClient.getInstance()
                            .contactManager()
                            .declineInvitation(
                                    message.getStringAttribute(EaseConstant.MESSAGE_ATTR_USERNAME,
                                            ""));
                    // update contacts apply for message status
                    message.setAttribute(EaseConstant.MESSAGE_ATTR_STATUS,
                            mActivity.getString(R.string.em_rejected));
                    EMClient.getInstance().chatManager().updateMessage(message);

                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(mActivity, R.string.em_rejected, Toast.LENGTH_LONG)
                                    .show();
                            dialog.dismiss();
                            refresh();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Refresh ui
     */
    private void refresh() {
        if (mApplyAdapter != null) {
            mApplyAdapter.notifyDataSetChanged();
        }
    }
}
