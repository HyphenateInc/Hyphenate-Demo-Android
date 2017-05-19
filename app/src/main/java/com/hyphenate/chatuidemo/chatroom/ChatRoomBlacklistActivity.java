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
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.chat.EMClient;
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

    private ChatRoomBlacklistActivity activity;

    @BindView(R.id.recycler_chatroom_members) RecyclerView recyclerView;

    private LinearLayoutManager layoutManager;
    private ChatRoomBlacklistAdapter adapter;

    private ProgressDialog progressDialog;

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
                new Thread(new Runnable() {
                    @Override public void run() {
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                showProgressDialog();
                            }
                        });
                        try {
                            switch (which) {
                                case 0:
                                    List<String> blacklistMembers = new ArrayList<String>();
                                    blacklistMembers.add(username);
                                    EMClient.getInstance().chatroomManager().unblockChatRoomMembers(chatRoomId, blacklistMembers);
                                    break;
                            }
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                        updateChatRoomData();
                    }
                }).start();
            }
        });
        alertDialogBuilder.show();
    }

    /**
     * Update chat room data from server
     */
    private void updateChatRoomData() {
        // fetch members from server
        new Thread(new Runnable() {
            @Override public void run() {
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        showProgressDialog();
                    }
                });
                try {
                    EMClient.getInstance().chatroomManager().fetchChatRoomBlackList(chatRoomId, 0, 200);
                    handler.sendMessage(handler.obtainMessage(0));
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * show progress dialog
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage("Please waiting...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    // refresh ui handler
    Handler handler = new Handler() {
        @Override public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if (adapter != null) {
                        adapter.refresh();
                    }
                    break;
            }
        }
    };
}
