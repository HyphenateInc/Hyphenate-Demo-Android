package com.hyphenate.chatuidemo.chatroom;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMPageResult;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.chat.ChatActivity;
import com.hyphenate.chatuidemo.group.GroupUtils;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by linan on 17/5/24.
 * Show Chatroom list
 */
public class ChatRoomListActivity extends BaseActivity {

    @BindView(R.id.recycler_chatroom) RecyclerView recyclerView;
    @BindView(R.id.progressbar) ProgressBar progressBar;
    LinearLayoutManager manager;
    ChatRoomListAdapter adapter;
    List<EMChatRoom> chatRoomList = new ArrayList<>();
    Toolbar toolbar;

    GroupUtils.LoadMoreData<EMChatRoom> loadMoreData;
    Handler handler = new Handler();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_chatroom_list);
        ButterKnife.bind(this);
        initToolbar();
        initRecyclerView();
    }

    private void initToolbar() {
        toolbar = getActionBarToolbar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
        toolbar.setTitle(R.string.em_chatrooms);
    }

    private void initRecyclerView() {
        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        adapter = new ChatRoomListAdapter(ChatRoomListActivity.this, chatRoomList);
        recyclerView.setAdapter(adapter);

        adapter.setItemClickListener(new EaseListItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                startActivity(new Intent(ChatRoomListActivity.this, ChatActivity.class).putExtra(EaseConstant.EXTRA_CHAT_TYPE,
                        EaseConstant.CHATTYPE_CHATROOM).putExtra(EaseConstant.EXTRA_USER_ID, chatRoomList.get(position).getId()));
            }

            @Override public void onItemLongClick(View view, final int position) {

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override public void onScrollStateChanged(RecyclerView view, int scrollState) {
                if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (adapter.getItemCount() != 0) {
                        int lastVisibleItem = manager.findLastVisibleItemPosition();
                        int totalItemCount = manager.getItemCount();
                        if (loadMoreData.hasMoreData() && !loadMoreData.isLoading() && lastVisibleItem == totalItemCount - 1) {
                            loadMoreData.load();
                        }
                    }
                }
            }
        });

        loadMoreData = new GroupUtils.LoadMoreData<>(this, chatRoomList, adapter, new GroupUtils.LoadMoreData.onLoadListener() {

            @Override public void onInitialAction() {
            }

            @Override public void onLoadAction() {
                showLoading();
                try {
                    EMPageResult<EMChatRoom> chatRooms = EMClient.getInstance().chatroomManager().
                            fetchPublicChatRoomsFromServer(loadMoreData.getPageNumber(), GroupUtils.LoadMoreData.PAGE_SIZE);

                    loadMoreData.setFetchResult(chatRooms.getData());
                } catch (HyphenateException e) {
                    e.printStackTrace();
                } finally {
                    hideLoading();
                }
            }

            @Override public void onNoMoreDataAction() {
                handler.postDelayed(new Runnable() {
                    @Override public void run() {
                        Snackbar.make(recyclerView, "No more data", Snackbar.LENGTH_LONG).show();
                    }
                }, 1000);
            }
        });
        // it is weird, chat room page number start from 1 rather than 0
        loadMoreData.setPageNumber(1);
        loadMoreData.load();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    void showLoading() {
        this.runOnUiThread(new Runnable() {
            @Override public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    void hideLoading() {
        this.runOnUiThread(new Runnable() {
            @Override public void run() {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
