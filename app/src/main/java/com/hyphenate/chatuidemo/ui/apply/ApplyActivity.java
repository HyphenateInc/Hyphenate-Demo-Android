package com.hyphenate.chatuidemo.ui.apply;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
import com.hyphenate.util.EMLog;

/**
 * Created by lzan13 on 2016/10/26.
 * Apply for info activity
 */

public class ApplyActivity extends BaseActivity {

    private static String TAG = ApplyActivity.class.getSimpleName();
    private BaseActivity mActivity;

    @BindView(R.id.recycler_view_apply) RecyclerView mRecyclerView;

    private EMConversation mConversation;

    private ApplyAdapter mApplyAdapter;

    private LocalBroadcastManager localBroadcastManager;
    private ApplyBroadcastReceiver broadcastReceiver;

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
        int mPageSize = 30;
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
                    if (message.getIntAttribute(EaseConstant.MESSAGE_ATTR_TYPE) == 1) {
                        if (message.getIntAttribute(EaseConstant.MESSAGE_ATTR_GROUP_TYPE) == 0) {

                            EMClient.getInstance()
                                    .groupManager()
                                    .acceptInvitation(message.getStringAttribute(
                                            EaseConstant.MESSAGE_ATTR_GROUP_ID),
                                            message.getStringAttribute(
                                                    EaseConstant.MESSAGE_ATTR_USERNAME));
                        } else {
                            EMClient.getInstance()
                                    .groupManager()
                                    .acceptApplication(message.getStringAttribute(
                                            EaseConstant.MESSAGE_ATTR_USERNAME),
                                            message.getStringAttribute(
                                                    EaseConstant.MESSAGE_ATTR_GROUP_ID));
                        }
                    } else {
                        EMClient.getInstance()
                                .contactManager()
                                .acceptInvitation(message.getStringAttribute(
                                        EaseConstant.MESSAGE_ATTR_USERNAME, ""));
                    }

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
                } catch (final HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
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
                    if (message.getIntAttribute(EaseConstant.MESSAGE_ATTR_TYPE) == 1) {
                        if (message.getIntAttribute(EaseConstant.MESSAGE_ATTR_GROUP_TYPE) == 0) {
                            EMClient.getInstance()
                                    .groupManager()
                                    .declineInvitation(message.getStringAttribute(
                                            EaseConstant.MESSAGE_ATTR_GROUP_ID),
                                            message.getStringAttribute(
                                                    EaseConstant.MESSAGE_ATTR_USERNAME), "");
                        } else {
                            EMClient.getInstance()
                                    .groupManager()
                                    .declineApplication(message.getStringAttribute(
                                            EaseConstant.MESSAGE_ATTR_GROUP_ID),
                                            EaseConstant.MESSAGE_ATTR_USERNAME, "");
                        }
                    } else {
                        EMClient.getInstance()
                                .contactManager()
                                .declineInvitation(message.getStringAttribute(
                                        EaseConstant.MESSAGE_ATTR_USERNAME, ""));
                    }
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
                } catch (final HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
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

    /**
     * Contacts broadcast receiver
     */
    private class ApplyBroadcastReceiver extends BroadcastReceiver {

        @Override public void onReceive(Context context, Intent intent) {
            EMLog.d(TAG, "contact action");
            refresh();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        // register broadcast register
        localBroadcastManager = LocalBroadcastManager.getInstance(mActivity);
        broadcastReceiver = new ApplyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EaseConstant.BROADCAST_ACTION_APPLY);
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);
        // refresh ui
        refresh();
    }

    @Override protected void onStop() {
        super.onStop();
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }
}
