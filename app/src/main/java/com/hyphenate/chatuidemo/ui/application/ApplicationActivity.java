package com.hyphenate.chatuidemo.ui.application;

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

/**
 * Created by lzan13 on 2016/10/26.
 * Application info activity
 */

public class ApplicationActivity extends BaseActivity {

    private BaseActivity mActivity;

    @BindView(R.id.recycler_view_application) RecyclerView mRecyclerView;

    private EMConversation mConversation;

    private int mPageSize = 30;

    private ApplicationAdapter mApplicationAdapter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.em_activity_application);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * setup ui
     */
    private void initView() {
        mActivity = this;

        getSupportActionBar().setTitle(R.string.em_contacts_application);
        getActionBarToolbar().setNavigationIcon(R.drawable.em_ic_back);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
        mConversation = EMClient.getInstance()
                .chatManager()
                .getConversation(EaseConstant.CONVERSATION_NAME_APPLICATION, null, true);
        // 设置当前会话未读数为 0
        mConversation.markAllMessagesAsRead();
        int count = mConversation.getAllMessages().size();
        if (count < mConversation.getAllMsgCount() && count < mPageSize) {
            // 获取已经在列表中的最上边的一条消息id
            String msgId = mConversation.getAllMessages().get(0).getMsgId();
            // 分页加载更多消息，需要传递已经加载的消息的最上边一条消息的id，以及需要加载的消息的条数
            mConversation.loadMoreMsgFromDB(msgId, mPageSize - count);
        }

        // 实例化适配器
        mApplicationAdapter = new ApplicationAdapter(mActivity);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        mRecyclerView.setAdapter(mApplicationAdapter);

        setItemClickListener();
    }

    /**
     * Refresh ui
     */
    private void refresh() {
        if (mApplicationAdapter != null) {
            mApplicationAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Set item click listener
     */
    private void setItemClickListener() {
        mApplicationAdapter.setItemClickListener(new ApplicationAdapter.ItemClickListener() {
            /**
             * Item action event
             *
             * @param position item position
             * @param action item action
             */
            @Override public void onItemAction(int position, int action) {
                switch (action) {
                    case 0:
                        agreeApplication(position);
                        break;
                    case 1:
                        rejectApplication(position);
                        break;
                }
            }
        });
    }

    /**
     * Agree application
     */
    private void agreeApplication(final int position) {
        final ProgressDialog dialog = new ProgressDialog(mActivity);
        dialog.setMessage(mActivity.getResources().getString(R.string.em_wait));
        dialog.show();

        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMMessage message = mConversation.getAllMessages().get(position);
                    EMClient.getInstance()
                            .contactManager()
                            .acceptInvitation(
                                    message.getStringAttribute(EaseConstant.MESSAGE_ATTR_USERNAME,
                                            ""));

                    // update application message status
                    message.setAttribute(EaseConstant.MESSAGE_ATTR_STATUS,
                            mActivity.getString(R.string.em_agreed));
                    EMClient.getInstance().chatManager().updateMessage(message);

                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(mActivity, "", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * reject application
     */
    private void rejectApplication(final int positon) {
        final ProgressDialog dialog = new ProgressDialog(mActivity);
        dialog.setMessage(mActivity.getResources().getString(R.string.em_wait));
        dialog.show();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMMessage message = mConversation.getAllMessages().get(positon);
                    EMClient.getInstance()
                            .contactManager()
                            .declineInvitation(
                                    message.getStringAttribute(EaseConstant.MESSAGE_ATTR_USERNAME,
                                            ""));
                    // update application message status
                    message.setAttribute(EaseConstant.MESSAGE_ATTR_STATUS,
                            mActivity.getString(R.string.em_rejected));
                    EMClient.getInstance().chatManager().updateMessage(message);

                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(mActivity, "", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
