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
package com.hyphenate.chatuidemo.group;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.chat.ChatActivity;
import com.hyphenate.chatuidemo.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupDetailsActivity extends BaseActivity {

    private final int REQUEST_CODE_MEMBER_REFRESH = 1;

    private String groupId;
    private EMGroup group;
    private MucMembersVerticalAdapter adapter;
    private ProgressDialog progressDialog;
    ProgressBar progressBar;
    Toolbar toolbar;

    public static GroupDetailsActivity instance;

    @BindView(R.id.text_group_details_name) TextView groupNameView;
    @BindView(R.id.text_group_details_desc) TextView groupDescView;
    @BindView(R.id.text_group_details_member_size) TextView memberSizeView;
    @BindView(R.id.recycler_member) RecyclerView recyclerView;
    @BindView(R.id.text_exit_group) TextView exitGroupView;
    @BindView(R.id.text_allow_member_to_invite) TextView inviteView;
    @BindView(R.id.text_appear_in_group_search) TextView groupTypeView;
    @BindView(R.id.switch_push_notification) Switch notificationSwitch;
    @BindView(R.id.txt_group_id) TextView groupIdView;
    @BindView(R.id.layout_member_list) RelativeLayout layoutMemberView;

    List<String> members = new ArrayList<>();
    private boolean isOwner = false;
    GroupUtils.MucRoleJudge mucRoleJudge = new GroupUtils.MucRoleJudgeImpl();

    private DefaultGroupChangeListener listener;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.em_activity_group_details);
        ButterKnife.bind(this);
        instance = this;

        progressBar = new ProgressBar(this);
        groupId = getIntent().getStringExtra("groupId");
        groupIdView.setText(groupId);

        group = EMClient.getInstance().groupManager().getGroup(groupId);

        if (group == null) {
            progressDialog.dismiss();
            finish();
        }

        initLocalView();

        updateGroupFromServer();

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

        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        listener = new DefaultGroupChangeListener();
        EMClient.getInstance().groupManager().addGroupChangeListener(listener);
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_item_transfer_owner:
                    break;
                case R.id.menu_item_clear_conversation:
                    break;
                case R.id.menu_item_change_group_name:
                    break;
                case R.id.menu_item_change_group_desc:
                    break;
                case R.id.menu_item_admin_list:
                    startActivity(new Intent(GroupDetailsActivity.this, GroupAdminActivity.class).putExtra("groupId", groupId));
                    break;
                case R.id.menu_item_black_list:
                    break;
                case R.id.menu_item_mute_list:
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.em_group_detail_menu, menu);
        return true;
    }

    private void initLocalView() {
        groupNameView.setText(group.getGroupName());
        groupDescView.setText(group.getDescription());
        memberSizeView.setText("(" + group.getMemberCount() + ")");

        if (EMClient.getInstance().getCurrentUser().equals(group.getOwner())) {
            isOwner = true;
            exitGroupView.setText(getString(R.string.em_delete_group));
        } else {
            isOwner = false;
            exitGroupView.setText(getString(R.string.em_leave_group));
        }

        if (group.isMemberAllowToInvite()) {
            inviteView.setText(getString(R.string.em_enable));
        } else {
            inviteView.setText(getString(R.string.em_disabled));
        }

        if (group.isPublic()) {
            groupTypeView.setText(getString(R.string.em_public));
        } else {
            groupTypeView.setText(getString(R.string.em_private));
        }

        members.clear();
        members.add(group.getOwner());
        members.addAll(group.getAdminList());

        mucRoleJudge.update(group);

        recyclerView.setLayoutManager(new LinearLayoutManager(GroupDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false));

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            adapter = new MucMembersVerticalAdapter(GroupDetailsActivity.this, members, mucRoleJudge);
            recyclerView.setAdapter(adapter);
        }
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

    @OnClick({ R.id.text_exit_group, R.id.layout_member_list, R.id.layout_push_notification, R.id.iv_invite_member }) void onclick(View view) {
        switch (view.getId()) {
            case R.id.iv_invite_member:
                startActivityForResult(new Intent(GroupDetailsActivity.this, InviteMembersActivity.class).putExtra("groupId", groupId)
                        .putExtra("isOwner", isOwner)
                        .putStringArrayListExtra("members", (ArrayList<String>) members), REQUEST_CODE_MEMBER_REFRESH);
                break;
            case R.id.text_exit_group:
                exitGroupUI();
                break;
            case R.id.layout_member_list://show member list
                startActivityForResult(new Intent(GroupDetailsActivity.this, GroupMembersListActivity.class).putExtra("isOwner", isOwner)
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

    public void exitGroupUI() {
        new AlertDialog.Builder(GroupDetailsActivity.this).setTitle(getString(R.string.em_group))
                .setMessage(getString(R.string.em_exit_group))
                .setPositiveButton(getString(R.string.em_ok), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {

                        if (isOwner) {
                            deleteGroup();
                        } else {
                            leaveGroup();
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

    /**
     * leave group
     */
    private void leaveGroup() {
        showDialog(getString(R.string.em_leave_group), getString(R.string.em_waiting));
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
        showDialog(getString(R.string.em_delete_group), getString(R.string.em_waiting));
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

    protected void updateGroupFromServer() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    group = EMClient.getInstance().groupManager().getGroupFromServer(groupId);

                    runOnUiThread(new Runnable() {
                        public void run() {

                            initLocalView();
                            toolbar.removeView(progressBar);
                        }
                    });
                } catch (Exception e) {

                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            toolbar.removeView(progressBar);
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
