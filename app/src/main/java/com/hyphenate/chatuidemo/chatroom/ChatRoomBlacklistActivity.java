package com.hyphenate.chatuidemo.chatroom;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.exceptions.HyphenateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzan13 on 2017/6/2.
 * Chat room blacklist activity
 */
public class ChatRoomBlacklistActivity extends BaseActivity {
    private final int REFRESH_CODE = 0;

    private ChatRoomBlacklistActivity activity;
    private DefaultChatRoomChangeListener chatRoomChangeListener;

    @BindView(R.id.recycler_chatroom_members) RecyclerView recyclerView;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    private LinearLayoutManager layoutManager;
    private ChatRoomBlacklistAdapter adapter;

    private String currentUser;
    private String chatRoomId;
    private String chatRoom;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_chatroom_members);

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

        chatRoomId = getIntent().getStringExtra(EaseConstant.EXTRA_CHATROOM_ID);

        layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatRoomBlacklistAdapter(activity, chatRoomId);
        recyclerView.setAdapter(adapter);

        updateChatRoomData();

        setItemClickListener();

        chatRoomChangeListener = new DefaultChatRoomChangeListener();
        EMClient.getInstance().chatroomManager().addChatRoomChangeListener(chatRoomChangeListener);
    }

    /**
     * set item click callback
     */
    private void setItemClickListener() {
        if (adapter != null) {
            adapter.setItemClickListener(new ChatRoomBlacklistAdapter.ItemClickListener() {
                @Override public void itemClick(String username) {
                    itemClickMenu(username);
                }
            });
        }
    }

    /**
     * item click menu
     *
     * @param username item username
     */
    public void itemClickMenu(final String username) {

        String[] menus = { "Remove blacklist" };

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setItems(menus, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, final int which) {
                DemoHelper.getInstance().execute(new Runnable() {
                    @Override public void run() {
                        showLoading();
                        try {
                            switch (which) {
                                case REFRESH_CODE:
                                    List<String> blacklistMembers = new ArrayList<String>();
                                    blacklistMembers.add(username);
                                    EMClient.getInstance().chatroomManager().unblockChatRoomMembers(chatRoomId, blacklistMembers);
                                    break;
                            }
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        } finally {
                            hideLoading();
                        }
                        updateChatRoomData();
                    }
                });
            }
        });
        alertDialogBuilder.show();
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
                    EMClient.getInstance().chatroomManager().fetchChatRoomBlackList(chatRoomId, 0, 200);
                    handler.sendMessage(handler.obtainMessage(REFRESH_CODE));
                } catch (HyphenateException e) {
                    e.printStackTrace();
                } finally {
                    hideLoading();
                }
            }
        });
    }

    // refresh ui handler
    Handler handler = new Handler() {
        @Override public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_CODE:
                    if (adapter != null) {
                        adapter.refresh();
                    }
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

    @Override protected void onDestroy() {
        super.onDestroy();
        if (chatRoomChangeListener != null) {
            EMClient.getInstance().chatroomManager().removeChatRoomListener(chatRoomChangeListener);
        }
    }

    /**
     * chatroom change listener
     */
    private class DefaultChatRoomChangeListener extends ChatRoomChangeListener {
        @Override public void onChatRoomDestroyed(String roomId, String roomName) {
            super.onChatRoomDestroyed(roomId, roomName);
            finish();
        }

        @Override public void onRemovedFromChatRoom(String roomId, String roomName, String participant) {
            super.onRemovedFromChatRoom(roomId, roomName, participant);
            finish();
        }
    }
}
