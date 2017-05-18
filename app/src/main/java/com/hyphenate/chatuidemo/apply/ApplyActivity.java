package com.hyphenate.chatuidemo.apply;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.group.GroupChangeListener;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.user.ContactsChangeListener;
import com.hyphenate.exceptions.HyphenateException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lzan13 on 2016/10/26.
 * Apply for info activity
 */

public class ApplyActivity extends BaseActivity {

    private static String TAG = ApplyActivity.class.getSimpleName();
    private BaseActivity mActivity;

    @BindView(R.id.recycler_view_apply) RecyclerView mRecyclerView;

    private EMConversation mConversation;

    private ApplyAdapter mAdapter;

    private DefaultContactsChangeListener contactsListener;
    private DefaultGroupChangeListener groupListener;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.em_activity_apply);

        ButterKnife.bind(this);

        contactsListener = new DefaultContactsChangeListener();
        EMClient.getInstance().contactManager().setContactListener(contactsListener);
        groupListener = new DefaultGroupChangeListener();
        EMClient.getInstance().groupManager().addGroupChangeListener(groupListener);

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
        mConversation = EMClient.getInstance().chatManager().getConversation(Constant.CONVERSATION_NAME_APPLY, null, true);
        mConversation.markAllMessagesAsRead();

        int count = mConversation.getAllMessages().size();
        int mPageSize = 30;
        if (count < mConversation.getAllMsgCount() && count < mPageSize) {
            String msgId = mConversation.getAllMessages().get(0).getMsgId();
            mConversation.loadMoreMsgFromDB(msgId, mPageSize - count);
        }

        mAdapter = new ApplyAdapter(mActivity);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        mRecyclerView.setAdapter(mAdapter);

        setItemClickListener();
    }

    /**
     * Set item click listener
     */
    private void setItemClickListener() {
        mAdapter.setItemClickListener(new ApplyAdapter.ItemClickListener() {
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
        dialog.setMessage(getString(R.string.em_wait));
        dialog.show();

        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMMessage message = mConversation.getMessage(msgId, true);
                    if (message.getIntAttribute(Constant.MESSAGE_ATTR_TYPE) == 1) {//0:chat,1:groupChat
                        if (message.getIntAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE) == 0) {//0 : private group,1:public group

                            EMClient.getInstance()
                                    .groupManager()
                                    .acceptInvitation(message.getStringAttribute(Constant.MESSAGE_ATTR_GROUP_ID),
                                            message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME));
                        } else {
                            EMClient.getInstance()
                                    .groupManager()
                                    .acceptApplication(message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME),
                                            message.getStringAttribute(Constant.MESSAGE_ATTR_GROUP_ID));
                        }
                    } else {
                        EMClient.getInstance().contactManager().acceptInvitation(message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME, ""));
                    }

                    // update contacts apply for message status
                    message.setAttribute(Constant.MESSAGE_ATTR_STATUS, mActivity.getString(R.string.em_agreed));
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
        dialog.setMessage(getString(R.string.em_wait));
        dialog.show();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMMessage message = mConversation.getMessage(msgId, true);
                    if (message.getIntAttribute(Constant.MESSAGE_ATTR_TYPE) == 1) { //0:chat,1:groupChat
                        if (message.getIntAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE) == 0) { //0 : private group,1:public group
                            EMClient.getInstance()
                                    .groupManager()
                                    .declineInvitation(message.getStringAttribute(Constant.MESSAGE_ATTR_GROUP_ID),
                                            message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME), "");
                        } else {
                            EMClient.getInstance()
                                    .groupManager()
                                    .declineApplication(message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME),
                                            message.getStringAttribute(Constant.MESSAGE_ATTR_GROUP_ID), "");
                        }
                    } else {
                        EMClient.getInstance().contactManager().declineInvitation(message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME, ""));
                    }
                    // update contacts apply for message status
                    message.setAttribute(Constant.MESSAGE_ATTR_STATUS, mActivity.getString(R.string.em_rejected));
                    EMClient.getInstance().chatManager().updateMessage(message);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(mActivity, R.string.em_rejected, Toast.LENGTH_LONG).show();
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
        if (mAdapter != null) {
            mAdapter.refresh();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        // refresh ui
        refresh();
    }

    @Override protected void onStop() {
        super.onStop();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().contactManager().removeContactListener(contactsListener);
        EMClient.getInstance().groupManager().removeGroupChangeListener(groupListener);
    }

    private class DefaultContactsChangeListener extends ContactsChangeListener {
        @Override public void onContactInvited(String username, String reason) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override public void onFriendRequestAccepted(String username) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override public void onFriendRequestDeclined(String username) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }
    }

    private class DefaultGroupChangeListener extends GroupChangeListener {
        @Override public void onInvitationReceived(String s, String s1, String s2, String s3) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override public void onRequestToJoinReceived(String s, String s1, String s2, String s3) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override public void onRequestToJoinAccepted(String s, String s1, String s2) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override public void onRequestToJoinDeclined(String s, String s1, String s2, String s3) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override public void onInvitationAccepted(String s, String s1, String s2) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override public void onInvitationDeclined(String s, String s1, String s2) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override public void onUserRemoved(String s, String s1) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override public void onGroupDestroyed(String s, String s1) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override
        public void onMemberJoined(String groupId, String member) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override
        public void onMemberExited(String groupId, String member) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }
    }
}
