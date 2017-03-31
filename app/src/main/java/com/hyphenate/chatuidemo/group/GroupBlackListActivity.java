package com.hyphenate.chatuidemo.group;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.user.ContactListAdapter;
import com.hyphenate.chatuidemo.user.model.UserEntity;
import com.hyphenate.chatuidemo.user.model.UserProfileManager;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by linan on 17/3/31.
 */

public class GroupBlackListActivity  extends BaseActivity {

    @BindView(R.id.recycler_members) RecyclerView recyclerView;

    String groupId;
    EMGroup group;

    LinearLayoutManager manager;
    ContactListAdapter adapter;
    Handler handler = new Handler();

    List<UserEntity> userEntityList = new ArrayList<>();
    GroupUtils.LoadMoreData<UserEntity> loadMoreData;

    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_group_black_list);
        ButterKnife.bind(this);

        groupId = getIntent().getExtras().getString("groupId");
        group = EMClient.getInstance().groupManager().getGroup(groupId);

        // recyclerView
        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        adapter = new ContactListAdapter(this, userEntityList, false);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {

                if(scrollState == RecyclerView.SCROLL_STATE_IDLE){
                    if(adapter.getItemCount() != 0){
                        int lastVisibleItem = manager.findLastVisibleItemPosition();
                        int totalItemCount = manager.getItemCount();
                        if(loadMoreData.hasMoreData() && !loadMoreData.isLoading() && lastVisibleItem == totalItemCount-1){
                            loadMoreData.load();
                        }
                    }
                }
            }

        });

        loadMoreData = new GroupUtils.LoadMoreData<>(GroupBlackListActivity.this, userEntityList, adapter,
                null,               // initial data
                new Runnable() {    // fetch data
                    @Override
                    public void run() {
                        try {
                            EMCursorResult<String> result = EMClient.getInstance().groupManager().fetchGroupBlackList(groupId, loadMoreData.getCursor(), GroupUtils.LoadMoreData.PAGE_SIZE);
                            EMCursorResult<UserEntity> fetchResult = new EMCursorResult<>();
                            fetchResult.setCursor(result.getCursor());
                            fetchResult.setData(UserProfileManager.convertContactList(result.getData()));
                            loadMoreData.setFetchResult(fetchResult);
                        } catch (HyphenateException e) { e.printStackTrace(); }
                    }
                },
                new Runnable() {    // no more data
                    @Override
                    public void run() {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(recyclerView, "No more data", Snackbar.LENGTH_LONG).show();
                            }
                        }, 1000);
                    }
                }
        );
        loadMoreData.load();
    }

}
