package io.agora.chatdemo.group;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.easeui.EaseConstant;
import io.agora.easeui.widget.EaseListItemClickListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by benson on 2016/10/10.
 */

public class GroupListActivity extends BaseActivity {

    @BindView(R.id.recycler_group) RecyclerView recyclerView;
    LinearLayoutManager manager;
    GroupListAdapter adapter;
    List<Group> groupList = new ArrayList<>();
    static Toolbar toolbar;
    static MenuItem item;
    static SearchView searchView;

    private DefaultAgoraGroupChangeListener listener;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_group_list);
        ButterKnife.bind(this);
        initToolbar();
        initRecyclerView();

        listener = new DefaultAgoraGroupChangeListener();
        ChatClient.getInstance().groupManager().addGroupChangeListener(listener);
    }

    private void initToolbar() {
        toolbar = getActionBarToolbar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        groupList.clear();
        groupList.addAll(ChatClient.getInstance().groupManager().getAllGroups());
        adapter = new GroupListAdapter(GroupListActivity.this, groupList);
        recyclerView.setAdapter(adapter);

        adapter.setItemClickListener(new EaseListItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                startActivity(
                        new Intent(GroupListActivity.this, ChatActivity.class).putExtra(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_GROUP)
                                .putExtra(EaseConstant.EXTRA_USER_ID, groupList.get(position).getGroupId()));
            }

            @Override public void onItemLongClick(View view, final int position) {

            }
        });
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.em_contacts_menu, menu);
        menu.findItem(R.id.menu_add_contacts).setVisible(false);
        item = menu.findItem(R.id.menu_search);

        searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override public boolean onQueryTextChange(String newText) {
                List<Group> groups = new ArrayList<>();
                groupList.clear();
                groupList.addAll(ChatClient.getInstance().groupManager().getAllGroups());
                for (Group group : groupList) {
                    if (group.getGroupName().contains(newText)) {
                        groups.add(group);
                    }
                }
                groupList.clear();
                groupList.addAll(groups);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        return true;
    }

    @Override protected void onResume() {
        super.onResume();
        if (adapter != null) {
            groupList.clear();
            groupList.addAll(ChatClient.getInstance().groupManager().getAllGroups());
            adapter.notifyDataSetChanged();
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();

        ChatClient.getInstance().groupManager().removeGroupChangeListener(listener);
    }

    private void refresh() {
        groupList.clear();
        groupList.addAll(ChatClient.getInstance().groupManager().getAllGroups());
        runOnUiThread(new Runnable() {
            @Override public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private class DefaultAgoraGroupChangeListener extends AgoraGroupChangeListener {
        @Override public void onUserRemoved(String s, String s1) {
            super.onUserRemoved(s, s1);
            refresh();
        }

        @Override public void onGroupDestroyed(String s, String s1) {
            super.onGroupDestroyed(s, s1);
            refresh();
        }

        @Override public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
            refresh();
        }

        @Override
        public void onWhiteListAdded(String groupId, List<String> whitelist) {

        }

        @Override
        public void onWhiteListRemoved(String groupId, List<String> whitelist) {

        }

        @Override
        public void onAllMemberMuteStateChanged(String groupId, boolean isMuted) {

        }
    }
}
