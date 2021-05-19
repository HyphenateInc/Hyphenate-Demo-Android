package io.agora.chatdemo.apply;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.ChatMessage;
import io.agora.chatdemo.Constant;
import io.agora.chatdemo.R;
import io.agora.chatdemo.group.AgoraGroupChangeListener;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.chatdemo.user.ContactsChangeListener;
import io.agora.exceptions.ChatException;

import java.util.List;

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

    private Conversation mConversation;

    private ApplyAdapter mAdapter;

    private DefaultContactsChangeListener contactsListener;
    private DefaultAgoraGroupChangeListener groupListener;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.em_activity_apply);

        ButterKnife.bind(this);

        contactsListener = new DefaultContactsChangeListener();
        ChatClient.getInstance().contactManager().setContactListener(contactsListener);
        groupListener = new DefaultAgoraGroupChangeListener();
        ChatClient.getInstance().groupManager().addGroupChangeListener(groupListener);

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
        mConversation = ChatClient.getInstance().chatManager().getConversation(Constant.CONVERSATION_NAME_APPLY, null, true);
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
                    ChatMessage message = mConversation.getMessage(msgId, true);
                    if (message.getIntAttribute(Constant.MESSAGE_ATTR_TYPE) == 1) {//0:chat,1:groupChat
                        if (message.getIntAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE) == 0) {//0 : private group,1:public group

                            ChatClient.getInstance()
                                    .groupManager()
                                    .acceptInvitation(message.getStringAttribute(Constant.MESSAGE_ATTR_GROUP_ID),
                                            message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME));
                        } else {
                            ChatClient.getInstance()
                                    .groupManager()
                                    .acceptApplication(message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME),
                                            message.getStringAttribute(Constant.MESSAGE_ATTR_GROUP_ID));
                        }
                    } else {
                        ChatClient.getInstance().contactManager().acceptInvitation(message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME, ""));
                    }

                    // update contacts apply for message status
                    message.setAttribute(Constant.MESSAGE_ATTR_STATUS, mActivity.getString(R.string.em_agreed));
                    ChatClient.getInstance().chatManager().updateMessage(message);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(mActivity, R.string.em_agreed, Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            refresh();
                        }
                    });
                } catch (final ChatException e) {
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
                    ChatMessage message = mConversation.getMessage(msgId, true);
                    if (message.getIntAttribute(Constant.MESSAGE_ATTR_TYPE) == 1) { //0:chat,1:groupChat
                        if (message.getIntAttribute(Constant.MESSAGE_ATTR_GROUP_TYPE) == 0) { //0 : private group,1:public group
                            ChatClient.getInstance()
                                    .groupManager()
                                    .declineInvitation(message.getStringAttribute(Constant.MESSAGE_ATTR_GROUP_ID),
                                            message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME), "");
                        } else {
                            ChatClient.getInstance()
                                    .groupManager()
                                    .declineApplication(message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME),
                                            message.getStringAttribute(Constant.MESSAGE_ATTR_GROUP_ID), "");
                        }
                    } else {
                        ChatClient.getInstance().contactManager().declineInvitation(message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME, ""));
                    }
                    // update contacts apply for message status
                    message.setAttribute(Constant.MESSAGE_ATTR_STATUS, mActivity.getString(R.string.em_rejected));
                    ChatClient.getInstance().chatManager().updateMessage(message);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(mActivity, R.string.em_rejected, Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            refresh();
                        }
                    });
                } catch (final ChatException e) {
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
        ChatClient.getInstance().contactManager().removeContactListener(contactsListener);
        ChatClient.getInstance().groupManager().removeGroupChangeListener(groupListener);
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

    private class DefaultAgoraGroupChangeListener extends AgoraGroupChangeListener {
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
        public void onWhiteListAdded(String groupId, List<String> whitelist) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override
        public void onWhiteListRemoved(String groupId, List<String> whitelist) {
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    refresh();
                }
            });
        }

        @Override
        public void onAllMemberMuteStateChanged(String groupId, boolean isMuted) {
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
