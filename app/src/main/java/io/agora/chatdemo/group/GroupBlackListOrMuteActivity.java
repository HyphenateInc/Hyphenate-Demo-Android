package io.agora.chatdemo.group;

import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.chatdemo.user.model.UserEntity;
import io.agora.chatdemo.user.model.UserProfileManager;
import io.agora.easeui.widget.EaseSwipeLayout;
import io.agora.easeui.widget.RecyclerSwipeView;
import io.agora.exceptions.ChatException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static io.agora.chatdemo.group.GroupListActivity.toolbar;

/**
 * Created by linan on 17/3/31.
 */

public class GroupBlackListOrMuteActivity extends BaseActivity {

    @BindView(R.id.recycler_members)    RecyclerSwipeView recyclerView;

    String groupId;
    Group group;

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

        group = ChatClient.getInstance().groupManager().getGroup(groupId);

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
                                ChatClient.getInstance().groupManager().unMuteGroupMembers(groupId, unMutes);
                            } else {
                                ChatClient.getInstance().groupManager().unblockUser(groupId, user.getEaseUsername());
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    userEntityList.remove(position.intValue());
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } catch (ChatException e) {
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
                                result.addAll(ChatClient.getInstance().groupManager().fetchGroupBlackList(groupId, loadMoreData.getPageNumber(), GroupUtils.LoadMoreData.PAGE_SIZE));
                            } else if (isMuteActivity) {
                                Map<String, Long>  mutes = ChatClient.getInstance().groupManager().fetchGroupMuteList(groupId, loadMoreData.getPageNumber(), GroupUtils.LoadMoreData.PAGE_SIZE);
                                result.addAll(mutes.keySet());
                            }
                            loadMoreData.setFetchResult(UserProfileManager.convertContactList(result));
                        } catch (ChatException e) { e.printStackTrace(); }
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
