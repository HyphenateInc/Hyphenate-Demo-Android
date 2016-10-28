package com.hyphenate.chatuidemo.ui.user;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.ui.chat.ChatActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by benson on 2016/10/10.
 */

public class GroupListActivity extends BaseActivity {

    @BindView(R.id.recycler_group) RecyclerView recyclerView;
    LinearLayoutManager manager;
    GroupListAdapter adapter;
    List<EMGroup> groupList = new ArrayList<>();
    private ProgressDialog progressDialog;
    Toolbar toolbar;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_group_list);
        ButterKnife.bind(this);
        initToolbar();
        initRecyclerView();
        loadGroupList();
    }

    private void initToolbar() {
        toolbar = getActionBarToolbar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
    }

    private void initRecyclerView() {
        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
    }

    public void loadGroupList() {
        groupList.clear();
        groupList.addAll(EMClient.getInstance().groupManager().getAllGroups());
        adapter = new GroupListAdapter(GroupListActivity.this, groupList);
        recyclerView.setAdapter(adapter);

        adapter.setItemClickListener(new EaseListItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                startActivity(new Intent(GroupListActivity.this, ChatActivity.class).putExtra(
                        EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_GROUP)
                        .putExtra(EaseConstant.EXTRA_USER_ID,
                                groupList.get(position).getGroupId()));
            }

            @Override public void onLongItemClick(View view, final int position) {

                new AlertDialog.Builder(GroupListActivity.this).setTitle("group")
                        .setMessage("delete group")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                EMGroup group = groupList.get(position);
                                if (progressDialog == null) {
                                    progressDialog = new ProgressDialog(GroupListActivity.this);
                                    progressDialog.setMessage("group");
                                    progressDialog.setCanceledOnTouchOutside(false);
                                }

                                if (group.getOwner() == null
                                        || "".equals(group.getOwner())
                                        || !group.getOwner()
                                        .equals(EMClient.getInstance().getCurrentUser())) {
                                    leaveGroup(group);
                                }
                                if (EMClient.getInstance()
                                        .getCurrentUser()
                                        .equals(group.getOwner())) {
                                    deleteGroup(group);
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.em_contacts_menu, menu);
        menu.findItem(R.id.menu_add_contacts).setVisible(false);
        return true;
    }

    private void leaveGroup(final EMGroup group) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().groupManager().leaveGroup(group.getGroupId());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            groupList.remove(group);
                            adapter = new GroupListAdapter(GroupListActivity.this, groupList);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    "exit failure " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void deleteGroup(final EMGroup group) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().groupManager().destroyGroup(group.getGroupId());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            groupList.remove(group);
                            //adapter = new GroupListAdapter(GroupListActivity.this, groupList);
                            //recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            progressDialog.dismiss();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),
                                    "delete failure" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }
}
