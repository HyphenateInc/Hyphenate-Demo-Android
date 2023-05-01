package io.agora.chatdemo.chatroom;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.agora.chat.ChatRoom;
import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.easeui.EaseConstant;
import io.agora.exceptions.ChatException;
import java.util.List;

/**
 * Created by lzan13 on 2017/5/31.
 * Show ChatRoom deatils
 */
public class ChatRoomDetailsActivity extends BaseActivity {

    private final int REFRESH_CODE = 0;

    private ChatRoomDetailsActivity activity;
    private DefaultAgoraChatRoomChangeListener chatRoomChangeListener;

    @BindView(R.id.layout_root) LinearLayout rootView;
    @BindView(R.id.text_chatroom_name) TextView chatRoomNameView;
    @BindView(R.id.text_chatroom_id) TextView chatRoomIdView;
    @BindView(R.id.text_chatroom_desc) TextView chatRoomDescriptionView;
    @BindView(R.id.text_chatroom_member_size) TextView chatRoomMembersView;
    @BindView(R.id.layout_go_members) View goMembersView;
    @BindView(R.id.layout_chatroom_blacklist) View blacklistView;
    @BindView(R.id.layout_change_chatroom_name) View changeChatRoomNameView;
    @BindView(R.id.layout_change_chatroom_description) View changeChatRoomDescriptionView;
    @BindView(R.id.layout_clear_all_message) View clearAllMessageView;
    @BindView(R.id.layout_destroy_chatroom) View destroyChatRoomView;

    @BindView(R.id.progress_bar) ProgressBar progressBar;

    private String currentUser;
    private String chatRoomId;
    private ChatRoom chatRoom;
    private List<String> adminList;

    private AlertDialog.Builder changeChatRoomDialog;
    private AlertDialog.Builder clearMessageDialog;
    private AlertDialog.Builder destroyChatRoomDialog;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_chatroom_details);

        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        activity = this;

        getActionBarToolbar().setNavigationIcon(R.drawable.em_ic_back);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        currentUser = ChatClient.getInstance().getCurrentUser();
        chatRoomId = getIntent().getStringExtra(EaseConstant.EXTRA_CHATROOM_ID);
        chatRoom = ChatClient.getInstance().chatroomManager().getChatRoom(chatRoomId);

        updateChatRoomData();
        refreshUI();

        chatRoomChangeListener = new DefaultAgoraChatRoomChangeListener();
        ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(chatRoomChangeListener);
    }

    private void refreshUI() {
        adminList = chatRoom.getAdminList();
        if (chatRoom != null) {
            chatRoomNameView.setText(chatRoom.getName());
            chatRoomIdView.setText(chatRoom.getId());
            chatRoomDescriptionView.setText(chatRoom.getDescription());
            chatRoomMembersView.setText(String.format(getString(R.string.em_chatroom_members_size), chatRoom.getMemberCount()));
            blacklistView.setVisibility(
                    currentUser.equals(chatRoom.getOwner()) || adminList.contains(currentUser) ? View.VISIBLE : View.GONE);
            destroyChatRoomView.setVisibility(currentUser.equals(chatRoom.getOwner()) ? View.VISIBLE : View.GONE);
            changeChatRoomNameView.setVisibility(currentUser.equals(chatRoom.getOwner()) ? View.VISIBLE : View.GONE);
            changeChatRoomDescriptionView.setVisibility(currentUser.equals(chatRoom.getOwner()) ? View.VISIBLE : View.GONE);
        }
    }

    @OnClick({
            R.id.layout_go_members, R.id.layout_chatroom_blacklist, R.id.layout_change_chatroom_name,
            R.id.layout_change_chatroom_description, R.id.layout_clear_all_message, R.id.layout_destroy_chatroom
    }) void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.layout_go_members:
                intent.setClass(activity, ChatRoomMembersActivity.class);
                intent.putExtra(EaseConstant.EXTRA_CHATROOM_ID, chatRoomId);
                startActivity(intent);
                break;
            case R.id.layout_chatroom_blacklist:
                intent.setClass(activity, ChatRoomBlacklistActivity.class);
                intent.putExtra(EaseConstant.EXTRA_CHATROOM_ID, chatRoomId);
                startActivity(intent);
                break;
            case R.id.layout_change_chatroom_name:
                changeChatRoomName();
                break;
            case R.id.layout_change_chatroom_description:
                changeChatRoomDescription();
                break;
            case R.id.layout_clear_all_message:
                clearAllMessage();
                break;
            case R.id.layout_destroy_chatroom:
                destroyChatRoom();
                break;
        }
    }

    /**
     * Update chat room data from server
     */
    private void updateChatRoomData() {
        // fetch members from server
        DemoHelper.getInstance().execute(new Runnable() {
            @Override public void run() {
                showLoading();
                try {
                    chatRoom = ChatClient.getInstance().chatroomManager().fetchChatRoomFromServer(chatRoomId, false);
                    handler.sendMessage(handler.obtainMessage(REFRESH_CODE));
                } catch (ChatException e) {
                    e.printStackTrace();
                } finally {
                    hideLoading();
                }
            }
        });
    }

    /**
     * change chat room name
     */
    private void changeChatRoomName() {
        changeChatRoomDialog = new AlertDialog.Builder(activity);
        changeChatRoomDialog.setTitle(getString(R.string.em_chatroom_change_name));
        final EditText editText = new EditText(activity);
        editText.setText(chatRoom.getName());
        changeChatRoomDialog.setView(editText);
        changeChatRoomDialog.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                final String name = editText.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    Snackbar.make(rootView, R.string.em_hint_input_not_null, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                DemoHelper.getInstance().execute(new Runnable() {
                    @Override public void run() {
                        showLoading();
                        try {
                            ChatClient.getInstance().chatroomManager().changeChatRoomSubject(chatRoomId, name);
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Snackbar.make(rootView, getString(R.string.em_chatroom_change_name) + "success",
                                            Snackbar.LENGTH_SHORT).show();
                                    chatRoomNameView.setText(name);
                                }
                            });
                        } catch (final ChatException e) {
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Snackbar.make(rootView, getString(R.string.em_chatroom_change_name)
                                            + " error: "
                                            + e.getErrorCode()
                                            + " message: "
                                            + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                        } finally {
                            hideLoading();
                        }
                    }
                });
            }
        });
        changeChatRoomDialog.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {

            }
        });
        changeChatRoomDialog.show();
    }

    /**
     * change chat room description
     */
    private void changeChatRoomDescription() {
        changeChatRoomDialog = new AlertDialog.Builder(activity);
        changeChatRoomDialog.setTitle(getString(R.string.em_chatroom_change_description));
        final EditText editText = new EditText(activity);
        editText.setText(chatRoom.getDescription());
        changeChatRoomDialog.setView(editText);
        changeChatRoomDialog.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                final String description = editText.getText().toString().trim();
                if (TextUtils.isEmpty(description)) {
                    Snackbar.make(rootView, R.string.em_hint_input_not_null, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                DemoHelper.getInstance().execute(new Runnable() {
                    @Override public void run() {
                        showLoading();
                        try {
                            ChatClient.getInstance().chatroomManager().changeChatroomDescription(chatRoomId, description);
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Snackbar.make(rootView, getString(R.string.em_chatroom_change_description) + "success",
                                            Snackbar.LENGTH_SHORT).show();
                                    chatRoomDescriptionView.setText(description);
                                }
                            });
                        } catch (final ChatException e) {
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Snackbar.make(rootView, getString(R.string.em_chatroom_change_description)
                                            + " error: "
                                            + e.getErrorCode()
                                            + " message: "
                                            + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                        } finally {
                            hideLoading();
                        }
                    }
                });
            }
        });
        changeChatRoomDialog.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {

            }
        });
        changeChatRoomDialog.show();
    }

    /**
     * clear all message
     */
    private void clearAllMessage() {
        clearMessageDialog = new AlertDialog.Builder(activity);
        // dialog title
        clearMessageDialog.setTitle(R.string.em_clear_all_message);
        // dialog content
        clearMessageDialog.setMessage(R.string.em_clear_all_message_sure);
        clearMessageDialog.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                // clear all message
                Conversation conversation = ChatClient.getInstance()
                        .chatManager()
                        .getConversation(chatRoomId, Conversation.ConversationType.ChatRoom, true);
                conversation.clearAllMessages();
            }
        });
        clearMessageDialog.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {

            }
        });
        clearMessageDialog.show();
    }

    /**
     * clear all message
     */
    private void destroyChatRoom() {
        destroyChatRoomDialog = new AlertDialog.Builder(activity);
        // dialot title
        destroyChatRoomDialog.setTitle(R.string.em_chatroom_destroy);
        // dialog content
        destroyChatRoomDialog.setMessage(R.string.em_chatroom_destroy_sure);
        destroyChatRoomDialog.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                DemoHelper.getInstance().execute(new Runnable() {
                    @Override public void run() {
                        showLoading();
                        try {
                            ChatClient.getInstance().chatroomManager().destroyChatRoom(chatRoomId);
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Snackbar.make(rootView, getString(R.string.em_chatroom_destroy), Snackbar.LENGTH_SHORT)
                                            .show();
                                    finish();
                                    if (ChatActivity.activityInstance != null) {
                                        ChatActivity.activityInstance.finish();
                                    }
                                }
                            });
                        } catch (final ChatException e) {
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Snackbar.make(rootView, getString(R.string.em_chatroom_destroy)
                                            + " error: "
                                            + e.getErrorCode()
                                            + " message: "
                                            + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                        } finally {
                            hideLoading();
                        }
                    }
                });
            }
        });
        destroyChatRoomDialog.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
            }
        });
        destroyChatRoomDialog.show();
    }

    @Override protected void onResume() {
        super.onResume();
        refreshUI();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (chatRoomChangeListener != null) {
            ChatClient.getInstance().chatroomManager().removeChatRoomListener(chatRoomChangeListener);
        }
    }

    // refresh ui handler
    Handler handler = new Handler() {
        @Override public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_CODE:
                    refreshUI();
                    break;
            }
        }
    };

    private void showLoading() {
        this.runOnUiThread(new Runnable() {
            @Override public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideLoading() {
        this.runOnUiThread(new Runnable() {
            @Override public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * chatroom change listener
     */
    private class DefaultAgoraChatRoomChangeListener extends AgoraChatRoomChangeListener {
        @Override public void onChatRoomDestroyed(String roomId, String roomName) {
            super.onChatRoomDestroyed(roomId, roomName);
            finish();
        }

        @Override
        public void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {
            super.onRemovedFromChatRoom(reason, roomId, roomName, participant);
            finish();
        }

        @Override public void onMemberJoined(String roomId, final String participant) {
            super.onMemberJoined(roomId, participant);
            updateChatRoomData();
        }

        @Override public void onMemberExited(String roomId, String roomName, final String participant) {
            super.onMemberExited(roomId, roomName, participant);
            updateChatRoomData();
        }

        @Override public void onAdminAdded(String chatRoomId, final String admin) {
            super.onAdminAdded(chatRoomId, admin);
            if (admin.equals(currentUser)) {
                updateChatRoomData();
            }
        }

        @Override public void onAdminRemoved(String chatRoomId, final String admin) {
            super.onAdminRemoved(chatRoomId, admin);
            if (admin.equals(currentUser)) {
                updateChatRoomData();
            }
        }

        @Override public void onOwnerChanged(String chatRoomId, final String newOwner, final String oldOwner) {
            super.onOwnerChanged(chatRoomId, newOwner, oldOwner);
            if (newOwner.equals(currentUser) || oldOwner.equals(currentUser)) {
                updateChatRoomData();
            }
        }
    }
}
