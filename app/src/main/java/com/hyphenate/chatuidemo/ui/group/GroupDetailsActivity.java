/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.chatuidemo.ui.group;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.listener.GroupChangeListener;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.ui.chat.ChatActivity;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import java.util.ArrayList;
import java.util.List;

public class GroupDetailsActivity extends BaseActivity {

    private final int REQUEST_CODE_MEMBER_REFRESH = 1;

    private String groupId;
    private EMGroup group;
    private MembersListAdapter adapter;
    private ProgressDialog progressDialog;
    ProgressBar progressBar;
    Toolbar toolbar;

    public static GroupDetailsActivity instance;

    @BindView(R.id.text_group_details_name) TextView groupNameView;
    @BindView(R.id.text_group_details_member_size) TextView memberSizeView;
    @BindView(R.id.recycler_member) RecyclerView recyclerView;
    @BindView(R.id.text_exit_group) TextView exitGroupView;
    @BindView(R.id.text_allow_member_to_invite) TextView inviteView;
    @BindView(R.id.text_appear_in_group_search) TextView groupTypeView;
    @BindView(R.id.switch_push_notification) Switch notificationSwitch;

    LinearLayoutManager layoutManager;
    List<String> members = new ArrayList<>();
    private boolean isOwner = false;

    private DefaultGroupChangeListener listener;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.em_activity_group_details);
        ButterKnife.bind(this);
        instance = this;

        groupId = getIntent().getStringExtra("groupId");
        updateGroup();

        progressBar = new ProgressBar(this);

        toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.em_ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        toolbar.addView(progressBar, params);


        listener = new DefaultGroupChangeListener();
        EMClient.getInstance().groupManager().addGroupChangeListener(listener);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (REQUEST_CODE_MEMBER_REFRESH == requestCode) {
                if (data != null) {
                    List<String> list = data.getStringArrayListExtra("selectedMembers");
                    members.clear();
                    if (!group.getMembers().contains(list.get(0))) {
                        members.addAll(group.getMembers());
                    }
                    members.addAll(list);
                    memberSizeView.setText("(" + members.size() + ")");
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @OnClick({ R.id.text_exit_group, R.id.layout_member_list, R.id.layout_push_notification }) void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_exit_group:
                exitGroup();
                break;
            case R.id.layout_member_list://show member list
                startActivityForResult(new Intent(GroupDetailsActivity.this, MembersListActivity.class).putExtra("isOwner", isOwner)
                        .putExtra("groupId", groupId)
                        .putStringArrayListExtra("members", (ArrayList<String>) members), REQUEST_CODE_MEMBER_REFRESH);
                break;

            case R.id.layout_push_notification:
                if (notificationSwitch.isChecked()) {
                    notificationSwitch.setChecked(false);
                } else {
                    notificationSwitch.setChecked(true);
                }

                break;
        }
    }

    public void exitGroup() {
        new AlertDialog.Builder(GroupDetailsActivity.this).setTitle("group")
                .setMessage("exit group")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {

                        if (isOwner) {
                            deleteGroup();
                        } else {
                            leaveGroup();
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

    /**
     * leave group
     */
    private void leaveGroup() {
        showDialog("leave group", "waiting...");
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().groupManager().leaveGroup(groupId);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            finish();
                            if (ChatActivity.activityInstance != null) {
                                ChatActivity.activityInstance.finish();
                            }
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), " leave failure " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * delete group
     */
    private void deleteGroup() {
        showDialog("delete group", "waiting...");
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().groupManager().destroyGroup(groupId);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            finish();
                            if (ChatActivity.activityInstance != null) {
                                ChatActivity.activityInstance.finish();
                            }
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "failure" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    protected void updateGroup() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    group = EMClient.getInstance().groupManager().getGroupFromServer(groupId);

                    runOnUiThread(new Runnable() {
                        public void run() {

                            if (group == null) {
                                progressDialog.dismiss();
                                finish();
                            }

                            groupNameView.setText(group.getGroupName());
                            memberSizeView.setText("(" + group.getMemberCount() + ")");

                            if (EMClient.getInstance().getCurrentUser().equals(group.getOwner())) {
                                isOwner = true;
                                exitGroupView.setText("Delete group");
                            } else {
                                isOwner = false;
                                exitGroupView.setText("leave group");
                            }

                            if (group.isMemberAllowToInvite()) {
                                inviteView.setText("Enable");
                            } else {
                                inviteView.setText("Disabled");
                            }

                            if (group.isPublic()){
                                groupTypeView.setText("Public");
                            }else {
                                groupTypeView.setText("Private");
                            }

                            members.clear();
                            members.addAll(group.getMembers());

                            layoutManager = new LinearLayoutManager(GroupDetailsActivity.this);
                            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                            recyclerView.setLayoutManager(layoutManager);
                            if (isOwner) {
                                adapter = new MembersListAdapter(GroupDetailsActivity.this, members, LinearLayoutManager.HORIZONTAL, true);
                            } else {
                                adapter = new MembersListAdapter(GroupDetailsActivity.this, members, LinearLayoutManager.HORIZONTAL,
                                        group.isMemberAllowToInvite());
                            }
                            recyclerView.setAdapter(adapter);

                            adapter.setItemClickListener(new EaseListItemClickListener() {

                                @Override public void onItemClick(View view, int position) {
                                    startActivityForResult(
                                            new Intent(GroupDetailsActivity.this, InviteMembersActivity.class).putExtra("groupId", groupId)
                                                    .putExtra("isOwner", isOwner)
                                                    .putStringArrayListExtra("members", (ArrayList<String>) members), REQUEST_CODE_MEMBER_REFRESH);
                                }

                                @Override public void onItemLongClick(View view, int position) {

                                }
                            });
                            toolbar.removeView(progressBar);
                        }
                    });
                } catch (Exception e) {

                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        }).start();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        instance = null;
        EMClient.getInstance().groupManager().removeGroupChangeListener(listener);
    }

    private void showDialog(String title, String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(GroupDetailsActivity.this);
            progressDialog.setTitle(title);
            progressDialog.setMessage(msg);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private class DefaultGroupChangeListener extends GroupChangeListener {
        @Override public void onUserRemoved(String s, String s1) {
            super.onUserRemoved(s, s1);
            finish();
            if (ChatActivity.activityInstance != null) {
                ChatActivity.activityInstance.finish();
            }
        }

        @Override public void onGroupDestroyed(String s, String s1) {
            super.onGroupDestroyed(s, s1);
            finish();
            if (ChatActivity.activityInstance != null) {
                ChatActivity.activityInstance.finish();
            }
        }

        @Override public void onAutoAcceptInvitationFromGroup(String s, String s1, String s2) {
            members.clear();
            members.addAll(EMClient.getInstance().groupManager().getGroup(s).getMembers());
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    memberSizeView.setText("(" + members.size() + ")");
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}
