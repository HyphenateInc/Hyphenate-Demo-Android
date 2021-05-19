package io.agora.chatdemo.group;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import io.agora.chat.ChatClient;
import io.agora.chat.CursorResult;
import io.agora.chat.Group;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.chatdemo.utils.ThreadPoolManager;
import io.agora.easeui.widget.EaseListItemClickListener;
import io.agora.easeui.widget.EaseSwipeLayout;
import io.agora.easeui.widget.RecyclerSwipeView;
import io.agora.exceptions.ChatException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static io.agora.chatdemo.group.GroupListActivity.toolbar;

/**
 * Created by benson on 2016/10/25.
 */

public class GroupMembersListActivity extends BaseActivity {

    @BindView(R.id.recycler_members)    RecyclerSwipeView recyclerView;

    private final int REQUEST_CODE_MEMBER_REFRESH = 1;

    LinearLayoutManager manager;
    List<String> membersList = new ArrayList<>();
    boolean isOwner = false;
    String groupId;
    Group group;
    ProgressDialog progressDialog;
    private boolean isChange;

    MucMembersHorizontalAdapter adapter;

    Handler handler = new Handler();

    GroupUtils.LoadMoreData<String> loadMoreData;

    GroupUtils.MucRoleJudge mucRoleJudge = new GroupUtils.MucRoleJudgeImpl();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_members);
        ButterKnife.bind(this);

        groupId = getIntent().getExtras().getString("groupId");
        group = ChatClient.getInstance().groupManager().getGroup(groupId);
        isOwner = group.getOwner().equals(ChatClient.getInstance().getCurrentUser());

        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        adapter = new MucMembersHorizontalAdapter(this, membersList, mucRoleJudge, recyclerView.getSwipeListener());
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
        toolbar.setTitle(getString(R.string.em_group_members) + "(" + group.getMemberCount() + ")");
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
                                                        ChatClient.getInstance().groupManager().muteGroupMembers(groupId, members, 24 * 60 * 60 * 1000); // mute 24h. prefer mute Long.MAX_VALUE, server error.
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                mucRoleJudge.update(group);
                                                                adapter.notifyDataSetChanged();
                                                            }
                                                        });
                                                        break;
                                                    case 1: // block
                                                        ChatClient.getInstance().groupManager().blockUser(groupId, username);
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
                                            } catch (ChatException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }).show();
                }
            }
        });

        EaseSwipeLayout.SwipeAction muteAction = new EaseSwipeLayout.SwipeAction("mute", "#ADB9C1", new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Toast.makeText(GroupMembersListActivity.this, "mute", Toast.LENGTH_SHORT).show();

                DemoHelper.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        Integer position = (Integer) view.getTag();
                        List<String> members = new ArrayList<>();
                        members.add(membersList.get(position.intValue()));
                        try {
                            ChatClient.getInstance().groupManager().muteGroupMembers(groupId, members, 24 * 60 * 60 * 1000);  //mute 24h. prefer mute Long.MAX_VALUE, current server not support
                            // TODO: update UI
                            updateUIList();
                        } catch (ChatException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        EaseSwipeLayout.SwipeAction blockAction = new EaseSwipeLayout.SwipeAction("block", "#405E7A", new View.OnClickListener() { // block
            @Override
            public void onClick(final View view) {
                Toast.makeText(GroupMembersListActivity.this, "block", Toast.LENGTH_SHORT).show();
                DemoHelper.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        final Integer position = (Integer) view.getTag();
                        try {
                            ChatClient.getInstance().groupManager().blockUser(groupId, membersList.get(position.intValue()));

                            // TODO: update UI
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    membersList.remove(position.intValue());
                                    updateUIList();
                                }
                            });
                        } catch (ChatException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        EaseSwipeLayout.SwipeAction deleteAction = new EaseSwipeLayout.SwipeAction("delete", "#F52700",  new View.OnClickListener() { // delete
            @Override
            public void onClick(final View view) {
                Toast.makeText(GroupMembersListActivity.this, "delete", Toast.LENGTH_SHORT).show();
                DemoHelper.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        final Integer position = (Integer) view.getTag();
                        try {
                            ChatClient.getInstance().groupManager().removeUserFromGroup(groupId, membersList.get(position.intValue()));

                            // TODO: update UI
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    membersList.remove(position.intValue());
                                    updateUIList();
                                }
                            });
                        } catch (ChatException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        mucRoleJudge.update(group);

        String currentUser = ChatClient.getInstance().getCurrentUser();
        if (mucRoleJudge.isAdmin(currentUser) || mucRoleJudge.isOwner(currentUser)) {
            adapter.setSwipeLayoutActions(muteAction, blockAction, deleteAction);
        }

        loadMoreData = new GroupUtils.LoadMoreData<>(this, membersList, adapter, new GroupUtils.LoadMoreData.onLoadListener() {
            @Override
            public void onInitialAction() {
                try {
                    group = ChatClient.getInstance().groupManager().getGroupFromServer(groupId);
                    mucRoleJudge.update(group);

                    List<String> list = new ArrayList<>();
                    list.add(group.getOwner());
                    list.addAll(group.getAdminList());
                    CursorResult<String> result = new CursorResult<>();
                    result.setData(list);
                    loadMoreData.setFetchResult(result);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Toolbar toolbar = getActionBarToolbar();
                            toolbar.setTitle(getString(R.string.em_group_members) + "(" + group.getMemberCount() + ")");

                        }
                    });
                } catch (ChatException e) { e.printStackTrace(); }
            }

            @Override
            public void onLoadAction() {
                try {
                    loadMoreData.setFetchResult(ChatClient.getInstance().groupManager().fetchGroupMembers(groupId, loadMoreData.getCursor(), GroupUtils.LoadMoreData.PAGE_SIZE));
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
        });
        loadMoreData.load();
    }

    private void updateUIList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mucRoleJudge.update(group);
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

                        if (!ChatClient.getInstance().getCurrentUser().equals(member)) {
                            ThreadPoolManager.getInstance().executeTask(new ThreadPoolManager.Task() {

                                @Override
                                public Object onRequest() throws ChatException {
                                    ChatClient.getInstance().groupManager().removeUserFromGroup(groupId, member);
                                    return null;
                                }

                                @Override
                                public void onSuccess(Object o) {
                                    progressDialog.dismiss();
                                    membersList.remove(member);
                                    isChange = true;
                                    adapter.notifyDataSetChanged();
                                    toolbar.setTitle(getResources().getString(R.string.em_group_members) + "(" + membersList.size() + ")");
                                }

                                @Override
                                public void onError(ChatException e) {
                                    e.printStackTrace();
                                    progressDialog.dismiss();
                                    Snackbar.make(toolbar, "delete failure" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            });
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
        menu.findItem(R.id.menu_item_group_members_add).setVisible(GroupUtils.isCanAddMember(group));
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
