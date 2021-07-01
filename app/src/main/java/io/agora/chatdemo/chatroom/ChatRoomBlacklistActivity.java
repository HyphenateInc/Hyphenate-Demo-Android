package io.agora.chatdemo.chatroom;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.agora.chat.ChatClient;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.easeui.EaseConstant;
import io.agora.exceptions.ChatException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzan13 on 2017/6/2.
 * Chat room blacklist activity
 */
public class ChatRoomBlacklistActivity extends BaseActivity {
    private final int REFRESH_CODE = 0;

    private ChatRoomBlacklistActivity activity;
    private DefaultAgoraChatRoomChangeListener chatRoomChangeListener;

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

        chatRoomChangeListener = new DefaultAgoraChatRoomChangeListener();
        ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(chatRoomChangeListener);
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
                                    ChatClient.getInstance().chatroomManager().unblockChatRoomMembers(chatRoomId, blacklistMembers);
                                    break;
                            }
                        } catch (ChatException e) {
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
                    ChatClient.getInstance().chatroomManager().fetchChatRoomBlackList(chatRoomId, 0, 200);
                    handler.sendMessage(handler.obtainMessage(REFRESH_CODE));
                } catch (ChatException e) {
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
            ChatClient.getInstance().chatroomManager().removeChatRoomListener(chatRoomChangeListener);
        }
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
    }
}
