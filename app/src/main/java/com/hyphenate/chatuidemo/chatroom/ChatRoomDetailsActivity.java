package com.hyphenate.chatuidemo.chatroom;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.exceptions.HyphenateException;
import java.util.List;

/**
 * Created by lzan13 on 2017/5/31.
 * Show ChatRoom deatils
 */
public class ChatRoomDetailsActivity extends BaseActivity {

    private ChatRoomDetailsActivity activity;

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

    private String currentUser;
    private String chatRoomId;
    private EMChatRoom chatRoom;
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

        currentUser = EMClient.getInstance().getCurrentUser();
        chatRoomId = getIntent().getStringExtra(EaseConstant.EXTRA_CHATROOM_ID);
        chatRoom = EMClient.getInstance().chatroomManager().getChatRoom(chatRoomId);
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
                    Toast.makeText(activity, R.string.em_hint_input_not_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override public void run() {
                        try {
                            EMClient.getInstance().chatroomManager().changeChatRoomSubject(chatRoomId, name);
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Toast.makeText(activity, getString(R.string.em_chatroom_change_name) + "success",
                                            Toast.LENGTH_LONG).show();
                                    chatRoomNameView.setText(name);
                                }
                            });
                        } catch (final HyphenateException e) {
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Toast.makeText(activity, getString(R.string.em_chatroom_change_name)
                                            + " error: "
                                            + e.getErrorCode()
                                            + " message: "
                                            + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                }).start();
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
                    Toast.makeText(activity, R.string.em_hint_input_not_null, Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override public void run() {
                        try {
                            EMClient.getInstance().chatroomManager().changeChatroomDescription(chatRoomId, description);
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Toast.makeText(activity, getString(R.string.em_chatroom_change_description) + "success",
                                            Toast.LENGTH_LONG).show();
                                    chatRoomDescriptionView.setText(description);
                                }
                            });
                        } catch (final HyphenateException e) {
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Toast.makeText(activity, getString(R.string.em_chatroom_change_description)
                                            + " error: "
                                            + e.getErrorCode()
                                            + " message: "
                                            + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                }).start();
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
                EMConversation conversation = EMClient.getInstance()
                        .chatManager()
                        .getConversation(chatRoomId, EMConversation.EMConversationType.ChatRoom, true);
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
                new Thread(new Runnable() {
                    @Override public void run() {
                        try {
                            EMClient.getInstance().chatroomManager().destroyChatRoom(chatRoomId);
                        } catch (final HyphenateException e) {
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Toast.makeText(activity, getString(R.string.em_chatroom_destroy)
                                            + " error: "
                                            + e.getErrorCode()
                                            + " message: "
                                            + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        destroyChatRoomDialog.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
            }
        });
        destroyChatRoomDialog.show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }
}
