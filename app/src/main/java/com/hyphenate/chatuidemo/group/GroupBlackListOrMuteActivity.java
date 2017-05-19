package com.hyphenate.chatuidemo.group;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.user.model.UserEntity;
import com.hyphenate.chatuidemo.user.model.UserProfileManager;
import com.hyphenate.easeui.widget.EaseSwipeLayout;
import com.hyphenate.easeui.widget.RecyclerSwipeView;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hyphenate.chatuidemo.group.GroupListActivity.toolbar;

/**
 * Created by linan on 17/3/31.
 */

public class GroupBlackListOrMuteActivity extends BaseActivity {

    @BindView(R.id.recycler_members)    RecyclerSwipeView recyclerView;

    String groupId;
    EMGroup group;

    LinearLayoutManager manager;
    GroupMuteListAdapter adapter;
    Handler handler = new Handler();

    List<UserEntity> userEntityList = new ArrayList<>();
    GroupUtils.LoadMoreData<UserEntity> loadMoreData;
    boolean isMuteActivity = false;
    boolean isBlackListActivity = false;


    @Override protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_group_black_list);
        ButterKnife.bind(this);

        groupId = getIntent().getExtras().getString("groupId");
        isBlackListActivity = getIntent().getExtras().getBoolean("is_black_list");
        isMuteActivity = getIntent().getExtras().getBoolean("is_mute");

        group = EMClient.getInstance().groupManager().getGroup(groupId);

        // toolbar
        toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.em_ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        if (isBlackListActivity) {
            toolbar.setTitle(R.string.em_group_black_list);
        } else if (isMuteActivity) {
            toolbar.setTitle(R.string.em_group_mute_list);
        }

        // recyclerView
        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        adapter = new GroupMuteListAdapter(this, userEntityList, false, recyclerView.getSwipeListener());
        recyclerView.setAdapter(adapter);

        EaseSwipeLayout.SwipeAction action = new EaseSwipeLayout.SwipeAction("remove", "", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Integer position = (Integer) view.getTag();
                final UserEntity user = userEntityList.get(position);
                DemoHelper.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (isMuteActivity) {
                                List<String> unMutes = new ArrayList<>();
                                unMutes.add(user.getEaseUsername());
                                EMClient.getInstance().groupManager().unMuteGroupMembers(groupId, unMutes);
                            } else {
                                EMClient.getInstance().groupManager().unblockUser(groupId, user.getEaseUsername());
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    userEntityList.remove(position.intValue());
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        adapter.setSwipeActions(action);

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

        // loadMoreData
        loadMoreData = new GroupUtils.LoadMoreData<>(GroupBlackListOrMuteActivity.this, userEntityList, adapter,
                new GroupUtils.LoadMoreData.onLoadListener() {
                    @Override
                    public void onInitialAction() {
                    }

                    @Override
                    public void onLoadAction() {
                        try {
                            List<String> result = new ArrayList<>();
                            if (isBlackListActivity) {
                                result.addAll(EMClient.getInstance().groupManager().fetchGroupBlackList(groupId, loadMoreData.getPageNumber(), GroupUtils.LoadMoreData.PAGE_SIZE));
                            } else if (isMuteActivity) {
                                Map<String, Long>  mutes = EMClient.getInstance().groupManager().fetchGroupMuteList(groupId, loadMoreData.getPageNumber(), GroupUtils.LoadMoreData.PAGE_SIZE);
                                result.addAll(mutes.keySet());
                            }
                            loadMoreData.setFetchResult(UserProfileManager.convertContactList(result));
                        } catch (HyphenateException e) { e.printStackTrace(); }
                    }

                    @Override
                    public void onNoMoreDataAction() {
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
