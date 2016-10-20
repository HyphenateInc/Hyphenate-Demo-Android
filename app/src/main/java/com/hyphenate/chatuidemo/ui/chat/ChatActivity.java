package com.hyphenate.chatuidemo.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.ui.call.VideoCallActivity;
import com.hyphenate.chatuidemo.ui.call.VoiceCallActivity;
import com.hyphenate.chatuidemo.ui.widget.ChatInputView;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseMessageListView;
import java.util.List;

/**
 * Chat with someone in this activity
 */
public class ChatActivity extends BaseActivity {
    @BindView(R.id.input_view) ChatInputView mInputView;
    @BindView(R.id.message_list) EaseMessageListView mMessageListView;

    /**
     * to chat user id or group id
     */
    private String toChatUsername;

    /**
     * chat type, single chat or group chat
     */
    private int chatType;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_chat);
        ButterKnife.bind(this);

        toChatUsername = getIntent().getStringExtra(EaseConstant.EXTRA_USER_ID);
        chatType =
                getIntent().getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE);

        //TODO use nickname to set title
        getSupportActionBar().setTitle(toChatUsername);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        // init message list view
        mMessageListView.init(toChatUsername, chatType, null);

        mMessageListView.setItemClickListener(
                new EaseMessageListView.MessageListItemClicksListener() {
                    @Override public void onResendClick(EMMessage message) {

                    }

                    @Override public boolean onBubbleClick(EMMessage message) {
                        return false;
                    }

                    @Override public void onBubbleLongClick(EMMessage message) {

                    }

                    @Override public void onUserAvatarClick(String username) {

                    }

                    @Override public void onUserAvatarLongClick(String username) {

                    }
                });

        mInputView.setViewEventListener(new ChatInputView.ChatInputViewEventListener() {
            @Override public void onSendMessage(CharSequence content) {
                if (!TextUtils.isEmpty(content)) {
                    // create a message
                    EMMessage message =
                            EMMessage.createTxtSendMessage(content.toString(), toChatUsername);
                    // send message
                    EMClient.getInstance().chatManager().sendMessage(message);
                    // refresh ui
                    mMessageListView.refreshSelectLast();
                }
            }
        });

        // received messages code in onResume() method

    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //add the action buttons to toolbar
        Toolbar toolbar = getActionBarToolbar();
        toolbar.inflateMenu(R.menu.em_chat_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_take_photo:

                        break;
                    case R.id.menu_gallery:
                        break;
                    case R.id.menu_location:
                        break;
                    case R.id.menu_file:
                        break;
                    case R.id.menu_video_call:
                        Intent videoIntent = new Intent();
                        videoIntent.setClass(ChatActivity.this, VideoCallActivity.class);
                        videoIntent.putExtra(EaseConstant.EXTRA_USER_ID, toChatUsername);
                        videoIntent.putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false);
                        startActivity(videoIntent);
                        break;
                    case R.id.menu_voice_call:
                        Intent voiceIntent = new Intent();
                        voiceIntent.setClass(ChatActivity.this, VoiceCallActivity.class);
                        voiceIntent.putExtra(EaseConstant.EXTRA_USER_ID, toChatUsername);
                        voiceIntent.putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false);
                        startActivity(voiceIntent);
                        break;
                }

                return false;
            }
        });

        return true;
    }

    EMMessageListener mMessageListener = new EMMessageListener() {
        @Override public void onMessageReceived(List<EMMessage> list) {
            for (EMMessage message : list) {
                String username = null;
                // group message
                if (message.getChatType() == EMMessage.ChatType.GroupChat
                        || message.getChatType() == EMMessage.ChatType.ChatRoom) {
                    username = message.getTo();
                } else {
                    // single chat message
                    username = message.getFrom();
                }

                // if the message is for current conversation
                if (username.equals(toChatUsername)) {
                    mMessageListView.refreshSelectLast();
                    //EaseUI.getInstance().getNotifier().vibrateAndPlayTone(message);
                } else {
                    //EaseUI.getInstance().getNotifier().onNewMsg(message);
                }
            }
        }

        @Override public void onCmdMessageReceived(List<EMMessage> list) {
            //cmd messages do not save to the cache in sdk
        }

        @Override public void onMessageRead(List<EMMessage> list) {
            mMessageListView.refresh();
        }

        @Override public void onMessageDelivered(List<EMMessage> list) {
            mMessageListView.refresh();
        }

        @Override public void onMessageChanged(EMMessage emMessage, Object o) {
            mMessageListView.refresh();
        }
    };

    @Override protected void onResume() {
        super.onResume();
        DemoHelper.getInstance().pushActivity(this);
        // register the event listener when enter the foreground
        // remember to remove this listener in onStop()
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
    }

    @Override protected void onStop() {
        super.onStop();
        // unregister this event listener when this activity enters the background
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
        // remove activity from foreground activity list
        DemoHelper.getInstance().popActivity(this);
    }
}
