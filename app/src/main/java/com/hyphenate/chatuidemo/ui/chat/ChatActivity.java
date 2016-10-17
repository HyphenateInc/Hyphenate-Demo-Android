package com.hyphenate.chatuidemo.ui.chat;

import android.app.Activity;
import android.os.BaseBundle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.ui.MainActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseChatMessageListView;
import java.util.List;

/**
 * Chat with someone in this activity
 */
public class ChatActivity extends BaseActivity {
    @BindView(R.id.edt_msg_content) EditText mContentEditText;
    @BindView(R.id.message_list) EaseChatMessageListView mMessageListView;

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
        chatType = getIntent().getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE);


        //TODO use nickname to set title
        getSupportActionBar().setTitle(toChatUsername);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        // init message list view
        mMessageListView.init(toChatUsername, chatType, null);

        // received messages code in onResume() method

    }


    /**
     * on send message
     */
    @OnClick(R.id.btn_send) void onSendMessage(){
        if(!TextUtils.isEmpty(mContentEditText.getText())){
            // create a message
            EMMessage message = EMMessage.createTxtSendMessage(mContentEditText.getText().toString(), toChatUsername);
            // send message
            EMClient.getInstance().chatManager().sendMessage(message);
            // refresh ui
            mMessageListView.refreshSelectLast();
            // set edit blank
            mContentEditText.setText("");
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //add the action buttons to toolbar
        Toolbar toolbar = getActionBarToolbar();
        toolbar.inflateMenu(R.menu.em_chat_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override public boolean onMenuItemClick(MenuItem item) {

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
                if (message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom) {
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
