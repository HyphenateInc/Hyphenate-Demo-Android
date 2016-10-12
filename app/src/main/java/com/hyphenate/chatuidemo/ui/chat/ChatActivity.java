package com.hyphenate.chatuidemo.ui.chat;

import android.app.Activity;
import android.os.BaseBundle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.ui.MainActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseChatMessageListView;

/**
 * Chat with someone in this activity
 */
public class ChatActivity extends BaseActivity {
    @BindView(R.id.edt_msg_content) EditText mContentEditText;
    @BindView(R.id.message_list) EaseChatMessageListView mMessageListView;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_chat);
        ButterKnife.bind(this);

        String username = "zw321";

        getSupportActionBar().setTitle(username);

        mMessageListView.init(username, EaseConstant.CHATTYPE_SINGLE, null);
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


    /**
     * on send message
     */
    @OnClick(R.id.btn_send) void onSendMessage(){
        if(!TextUtils.isEmpty(mContentEditText.getText())){
            EMMessage message = EMMessage.createTxtSendMessage(mContentEditText.getText().toString(), "zw321");
            //send message
            EMClient.getInstance().chatManager().sendMessage(message);
            //refresh ui
            mMessageListView.refreshSelectLast();
        }
    }


}
