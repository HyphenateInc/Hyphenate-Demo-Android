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
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by lzan13 on 2017/6/1.
 * show ChatRoom members list
 */
public class ChatRoomMembersActivity extends BaseActivity {

    private String TAG = this.getClass().getSimpleName();

    private ChatRoomMembersActivity activity;

    @BindView(R.id.recycler_chatroom_members) RecyclerView recyclerView;

    private LinearLayoutManager layoutManager;
    private ChatRoomMembersAdapter adapter;

    private ProgressDialog progressDialog;

    private String currentUser;
    private String chatRoomId;
    private EMChatRoom chatRoom;
    private List<String> adminList;
    private List<String> muteList = new ArrayList<>();

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

        currentUser = EMClient.getInstance().getCurrentUser();
        chatRoomId = getIntent().getStringExtra(EaseConstant.EXTRA_CHATROOM_ID);
        chatRoom = EMClient.getInstance().chatroomManager().getChatRoom(chatRoomId);
        adminList = chatRoom.getAdminList();
        muteList.addAll(chatRoom.getMuteList().keySet());

        layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatRoomMembersAdapter(activity, chatRoomId);
        recyclerView.setAdapter(adapter);

        updateChatRoomData();
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
                    chatRoom = EMClient.getInstance().chatroomManager().fetchChatRoomFromServer(chatRoomId, true);
                    adminList.clear();
                    adminList.addAll(chatRoom.getAdminList());
                    if (currentUser.equals(chatRoom.getOwner()) || chatRoom.getAdminList().contains(currentUser)) {
                        // Only the administrator and the owner can manage the members
                        setItemClickListener();
                        // fetch mute list from server
                        muteList.clear();
                        muteList.addAll(
                                EMClient.getInstance().chatroomManager().fetchChatRoomMuteList(chatRoomId, 0, 200).keySet());
                    }
                    handler.sendMessage(handler.obtainMessage(0));
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Set item click listener, Manager the members
     */
    private void setItemClickListener() {
        if (adapter != null) {
            adapter.setItemClickListener(new ChatRoomMembersAdapter.ItemClickListener() {
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
        // item menu list
        List<String> menuList = new ArrayList<String>();
        if (username.equals(chatRoom.getOwner())) {
            return;
        }
        if (currentUser.equals(chatRoom.getOwner())) {
            menuList.add("Remove member");
            if (muteList.contains(username)) {
                menuList.add("Remove from mute list");
            } else {
                menuList.add("Add to mute list");
            }
            menuList.add("Add to blacklist");
            menuList.add("Transfer ownership");
            if (adminList.contains(username)) {
                menuList.add("Remove from administrator");
            } else {
                menuList.add("Add to administrator");
            }
        } else {
            if (!adminList.contains(username)) {
                menuList.add("Remove member");
                if (muteList.contains(username)) {
                    menuList.add("Remove from mute list");
                } else {
                    menuList.add("Add to mute list");
                }
                menuList.add("Add to blacklist");
            } else {
                return;
            }
        }

        String[] menus = new String[menuList.size()];
        menuList.toArray(menus);

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
                                    List<String> members = new ArrayList<String>();
                                    members.add(username);
                                    EMClient.getInstance().chatroomManager().removeChatRoomMembers(chatRoomId, members);
                                    break;
                                case 1:
                                    List<String> muteMembers = new ArrayList<String>();
                                    muteMembers.add(username);
                                    if (muteList.contains(username)) {
                                        EMClient.getInstance().chatroomManager().unMuteChatRoomMembers(chatRoomId, muteMembers);
                                    } else {
                                        EMClient.getInstance()
                                                .chatroomManager()
                                                .muteChatRoomMembers(chatRoomId, muteMembers, 10 * 60 * 1000);
                                    }
                                    break;
                                case 2:
                                    List<String> blacklistMembers = new ArrayList<String>();
                                    blacklistMembers.add(username);
                                    EMClient.getInstance().chatroomManager().blockChatroomMembers(chatRoomId, blacklistMembers);
                                    break;
                                case 3:
                                    EMClient.getInstance().chatroomManager().changeOwner(chatRoomId, username);
                                    break;
                                case 4:
                                    if (adminList.contains(username)) {
                                        EMClient.getInstance().chatroomManager().removeChatRoomAdmin(chatRoomId, username);
                                    } else {
                                        EMClient.getInstance().chatroomManager().addChatRoomAdmin(chatRoomId, username);
                                    }
                                    break;
                            }
                            updateChatRoomData();
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        alertDialogBuilder.show();
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
