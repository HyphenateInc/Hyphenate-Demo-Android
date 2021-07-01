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
import io.agora.chat.ChatRoom;
import io.agora.chat.ChatClient;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.easeui.EaseConstant;
import io.agora.exceptions.ChatException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lzan13 on 2017/6/1.
 * show ChatRoom members list
 */
public class ChatRoomMembersActivity extends BaseActivity {

    private String TAG = this.getClass().getSimpleName();

    private final int REFRESH_CODE = 0;
    private final int MENU_CODE_REMOVE_MEMBER = 0;
    private final int MENU_CODE_MUTE_OPERATION = 1;
    private final int MENU_CODE_BLACK_LIST_OPERATION = 2;
    private final int MENU_CODE_TRANSFER_OWNERSHIP = 3;
    private final int MENU_CODE_ADMIN_OPERATION = 4;

    private ChatRoomMembersActivity activity;
    private DefaultAgoraChatRoomChangeListener chatRoomChangeListener;

    @BindView(R.id.recycler_chatroom_members) RecyclerView recyclerView;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    private LinearLayoutManager layoutManager;
    private ChatRoomMembersAdapter adapter;

    private String currentUser;
    private String chatRoomId;
    private ChatRoom chatRoom;
    private List<String> adminList;
    private List<String> muteList = new ArrayList<>();
    // chatroom member page
    private String cursor = "";
    private int pageSize = 20;
    private boolean isLoading = false;
    private boolean isMoreData = true;

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

        currentUser = ChatClient.getInstance().getCurrentUser();
        chatRoomId = getIntent().getStringExtra(EaseConstant.EXTRA_CHATROOM_ID);
        chatRoom = ChatClient.getInstance().chatroomManager().getChatRoom(chatRoomId);
        adminList = chatRoom.getAdminList();
        muteList.addAll(chatRoom.getMuteList().keySet());

        layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ChatRoomMembersAdapter(activity, chatRoomId);
        recyclerView.setAdapter(adapter);

        initChatRoomData();

        chatRoomChangeListener = new DefaultAgoraChatRoomChangeListener();
        ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(chatRoomChangeListener);
    }

    /**
     * init chat room data from server
     */
    private void initChatRoomData() {
        // fetch members from server
        DemoHelper.getInstance().execute(new Runnable() {
            @Override public void run() {
                showLoading();
                try {
                    // from server fetch chatroom data, boolean params indicates whether to synchronize group members, defaults to 200
                    chatRoom = ChatClient.getInstance().chatroomManager().fetchChatRoomFromServer(chatRoomId, true);
                    adminList.clear();
                    adminList.addAll(chatRoom.getAdminList());
                    if (currentUser.equals(chatRoom.getOwner()) || chatRoom.getAdminList().contains(currentUser)) {
                        // Only the administrator and the owner can manage the members
                        setItemClickListener();
                        // fetch mute list from server
                        muteList.clear();
                        int count = 0;
                        int page = 0;
                        do {
                            Map<String, Long> map =
                                    ChatClient.getInstance().chatroomManager().fetchChatRoomMuteList(chatRoomId, page, pageSize);
                            count = map.size();
                            page++;
                            muteList.addAll(map.keySet());
                        } while (count == pageSize);
                    }
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
                DemoHelper.getInstance().execute(new Runnable() {
                    @Override public void run() {
                        showLoading();
                        try {
                            switch (which) {
                                case MENU_CODE_REMOVE_MEMBER:
                                    List<String> members = new ArrayList<String>();
                                    members.add(username);
                                    ChatClient.getInstance().chatroomManager().removeChatRoomMembers(chatRoomId, members);
                                    break;
                                case MENU_CODE_MUTE_OPERATION:
                                    List<String> muteMembers = new ArrayList<String>();
                                    muteMembers.add(username);
                                    if (muteList.contains(username)) {
                                        ChatClient.getInstance().chatroomManager().unMuteChatRoomMembers(chatRoomId, muteMembers);
                                    } else {
                                        ChatClient.getInstance()
                                                .chatroomManager()
                                                .muteChatRoomMembers(chatRoomId, muteMembers, 10 * 60 * 1000);
                                    }
                                    break;
                                case MENU_CODE_BLACK_LIST_OPERATION:
                                    List<String> blacklistMembers = new ArrayList<String>();
                                    blacklistMembers.add(username);
                                    ChatClient.getInstance().chatroomManager().blockChatroomMembers(chatRoomId, blacklistMembers);
                                    break;
                                case MENU_CODE_TRANSFER_OWNERSHIP:
                                    ChatClient.getInstance().chatroomManager().changeOwner(chatRoomId, username);
                                    break;
                                case MENU_CODE_ADMIN_OPERATION:
                                    if (adminList.contains(username)) {
                                        ChatClient.getInstance().chatroomManager().removeChatRoomAdmin(chatRoomId, username);
                                    } else {
                                        ChatClient.getInstance().chatroomManager().addChatRoomAdmin(chatRoomId, username);
                                    }
                                    break;
                            }
                            initChatRoomData();
                        } catch (ChatException e) {
                            e.printStackTrace();
                        } finally {
                            hideLoading();
                        }
                    }
                });
            }
        });
        alertDialogBuilder.show();
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

        @Override public void onMemberJoined(String roomId, String participant) {
            super.onMemberJoined(roomId, participant);
            initChatRoomData();
        }

        @Override public void onMemberExited(String roomId, String roomName, String participant) {
            super.onMemberExited(roomId, roomName, participant);
            initChatRoomData();
        }

        @Override public void onMuteListAdded(String chatRoomId, List<String> mutes, long expireTime) {
            super.onMuteListAdded(chatRoomId, mutes, expireTime);
            initChatRoomData();
        }

        @Override public void onMuteListRemoved(String chatRoomId, List<String> mutes) {
            super.onMuteListRemoved(chatRoomId, mutes);
            initChatRoomData();
        }

        @Override public void onAdminAdded(String chatRoomId, String admin) {
            super.onAdminAdded(chatRoomId, admin);
            initChatRoomData();
        }

        @Override public void onAdminRemoved(String chatRoomId, String admin) {
            super.onAdminRemoved(chatRoomId, admin);
            initChatRoomData();
        }

        @Override public void onOwnerChanged(String chatRoomId, String newOwner, String oldOwner) {
            super.onOwnerChanged(chatRoomId, newOwner, oldOwner);
            initChatRoomData();
        }
    }
}
