package com.hyphenate.chatuidemo.group;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hyphenate.chatuidemo.group.GroupListActivity.toolbar;

/**
 * Created by benson on 2016/10/25.
 */

public class GroupMembersListActivity extends BaseActivity {

    @BindView(R.id.recycler_members) RecyclerView recyclerView;

    private final int REQUEST_CODE_MEMBER_REFRESH = 1;

    LinearLayoutManager manager;
    List<String> membersList = new ArrayList<>();
    boolean isOwner = false;
    String groupId;
    EMGroup group;
    ProgressDialog progressDialog;
    private boolean isChange;

    MucMembersHorizontalAdapter adapter;
    private Snackbar snackbar;

    Handler handler = new Handler();

    GroupUtils.LoadMoreData loadMoreData;

    GroupUtils.MucRoleJudge mucRoleJudge = new GroupUtils.MucRoleJudgeImpl();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_members);
        ButterKnife.bind(this);

        groupId = getIntent().getExtras().getString("groupId");
        group = EMClient.getInstance().groupManager().getGroup(groupId);
        isOwner = getIntent().getExtras().getBoolean("isOwner");

        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        adapter = new MucMembersHorizontalAdapter(this, membersList, mucRoleJudge);
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

        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setTitle(getString(R.string.em_group_members) + "(" + membersList.size() + ")");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        adapter.setItemClickListener(new EaseListItemClickListener() {
            @Override public void onItemClick(View view, int position) {
            }

            @Override public void onItemLongClick(View view, final int position) {

                String[] menus = { getString(R.string.em_group_member_alert_item_add_mute),
                        getString(R.string.em_group_member_alert_item_add_black),
                        getString(R.string.em_group_member_alert_item_remove) };

                if (isOwner || group.getAdminList().contains(membersList.get(position))) {
                    new AlertDialog.Builder(GroupMembersListActivity.this).setTitle(getString(R.string.em_group_member))
                            .setItems(menus, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, final int i) {
                                    DemoHelper.getInstance().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                final String username = membersList.get(i);
                                                switch (i) {
                                                    case 0: // mute
                                                        List<String> members = new ArrayList<>();
                                                        members.add(username);
                                                        EMClient.getInstance().groupManager().muteGroupMembers(groupId, members, Long.MAX_VALUE);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                mucRoleJudge.update(group);
                                                                adapter.notifyDataSetChanged();
                                                            }
                                                        });
                                                        break;
                                                    case 1: // block
                                                        EMClient.getInstance().groupManager().blockUser(groupId, username);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                membersList.remove(username);
                                                                mucRoleJudge.update(group);
                                                                adapter.notifyDataSetChanged();
                                                            }
                                                        });
                                                        break;
                                                    case 2: // remove
                                                        confirmRemoveMember(username);
                                                        break;
                                                    default:
                                                        break;
                                                }
                                                updateUIList();
                                            } catch (HyphenateException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }).show();
                }
            }
        });

        loadMoreData = new GroupUtils.LoadMoreData(this, membersList, adapter,
                new Runnable() { // initial load
                    @Override
                    public void run() {
                        try {
                            group = EMClient.getInstance().groupManager().getGroupFromServer(groupId);
                            mucRoleJudge.update(group);

                            List<String> list = new ArrayList<>();
                            list.add(group.getOwner());
                            list.addAll(group.getAdminList());
                            EMCursorResult<String> result = new EMCursorResult<>();
                            result.setData(list);
                            loadMoreData.updateResult(result);
                        } catch (HyphenateException e) { e.printStackTrace(); }
                    }
                },
                new Runnable() { // load data
                    @Override
                    public void run() {
                        try {
                            loadMoreData.updateResult(EMClient.getInstance().groupManager().fetchGroupMembers(groupId, loadMoreData.getCursor(), GroupUtils.LoadMoreData.PAGE_SIZE));
                        } catch (HyphenateException e) { e.printStackTrace(); }
                    }
                },
                new Runnable() { // no more data
                    @Override
                    public void run() {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                snackbar = Snackbar.make(recyclerView, "No more data", Snackbar.LENGTH_INDEFINITE);
                                snackbar.show();
                            }
                        }, 1000);
                    }
        });
        loadMoreData.load();
    }

    private void updateUIList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void confirmRemoveMember(final String member) {
        new AlertDialog.Builder(GroupMembersListActivity.this).setTitle(getString(R.string.em_group_member))
                .setMessage(getString(R.string.em_group_delete_member))
                .setPositiveButton(getString(R.string.em_ok), new DialogInterface.OnClickListener() {
                    @Override public void onClick(final DialogInterface dialog, int which) {
                        progressDialog = ProgressDialog.show(GroupMembersListActivity.this, getResources().getString(R.string.em_group_delete_member), getString(R.string.em_waiting), false);

                        if (!EMClient.getInstance().getCurrentUser().equals(member)) {
                            new Thread(new Runnable() {
                                @Override public void run() {
                                    try {
                                        EMClient.getInstance().groupManager().removeUserFromGroup(groupId, member);
                                        runOnUiThread(new Runnable() {
                                            @Override public void run() {
                                                progressDialog.dismiss();
                                                membersList.remove(member);
                                                isChange = true;
                                                adapter.notifyDataSetChanged();
                                                toolbar.setTitle(getResources().getString(R.string.em_group_members) + "(" + membersList.size() + ")");
                                            }
                                        });
                                    } catch (final HyphenateException e) {
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {
                                            @Override public void run() {
                                                progressDialog.dismiss();
                                                Snackbar.make(toolbar, "delete failure" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }).start();
                        } else {
                            Snackbar.make(toolbar, "you can not delete yourself", Snackbar.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.em_cancel), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.em_group_members_menu, menu);
        return true;
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_item_group_members_add:
                    startActivityForResult(new Intent(GroupMembersListActivity.this, InviteMembersActivity.class).putExtra("groupId", groupId)
                            .putExtra("isOwner", isOwner)
                            .putStringArrayListExtra("members", (ArrayList<String>) membersList), REQUEST_CODE_MEMBER_REFRESH);
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (REQUEST_CODE_MEMBER_REFRESH == requestCode) {
                if (data != null) {
                    List<String> list = data.getStringArrayListExtra("selectedMembers");
                    membersList.clear();

                    if (!group.getMembers().contains(list.get(0))) {
                        membersList.addAll(group.getMembers());
                    }
                    membersList.addAll(list);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }


    @Override public void onBackPressed() {
        if (isChange){
            Intent intent = new Intent();
            intent.putExtra("selectedMembers", (ArrayList<String>) membersList);
            setResult(RESULT_OK,intent);
        }
        finish();
    }

}
