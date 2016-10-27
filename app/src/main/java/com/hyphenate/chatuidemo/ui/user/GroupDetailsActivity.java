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
package com.hyphenate.chatuidemo.ui.user;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.R;
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

    public static GroupDetailsActivity instance;

    @BindView(R.id.text_group_details_name) TextView groupNameView;
    @BindView(R.id.text_group_details_member_size) TextView memberSizeView;
    @BindView(R.id.recycler_member) RecyclerView recyclerView;
    @BindView(R.id.text_exit_group) TextView exitGroupView;
    @BindView(R.id.text_allow_member_to_invite) TextView inviteView;
    @BindView(R.id.switch_push_notification) Switch notificationSwitch;

    LinearLayoutManager layoutManager;
    List<String> members = new ArrayList<String>();
    private boolean isOwner = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.em_activity_group_details);
        ButterKnife.bind(this);
        instance = this;

        groupId = getIntent().getStringExtra("groupId");
        group = EMClient.getInstance().groupManager().getGroup(groupId);

        groupNameView.setText(group.getGroupName());
        memberSizeView.setText("(" + group.getMemberCount() + ")");
        if (group.isMemberAllowToInvite()) {
            inviteView.setText("Enable");
        } else {
            inviteView.setText("Disabled");
        }

        if (group.getOwner() == null || "".equals(group.getOwner()) || !group.getOwner()
                .equals(EMClient.getInstance().getCurrentUser())) {
            exitGroupView.setVisibility(View.GONE);
        }
        if (EMClient.getInstance().getCurrentUser().equals(group.getOwner())) {
            isOwner = true;
            exitGroupView.setText("Delete group");
        }

        GroupChangeListener groupChangeListener = new GroupChangeListener();
        EMClient.getInstance().groupManager().addGroupChangeListener(groupChangeListener);

        final List<String> members = new ArrayList<>();
        members.addAll(group.getMembers());

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MembersListAdapter(this, members, LinearLayoutManager.HORIZONTAL,group.isMemberAllowToInvite());
        recyclerView.setAdapter(adapter);

        updateGroup();

        Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.em_ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        adapter.setItemClickListener(new EaseListItemClickListener() {

            @Override public void onItemClick(View view, int position) {
                startActivityForResult(
                        new Intent(GroupDetailsActivity.this, InviteMembersActivity.class).putExtra(
                                "groupId", groupId)
                                .putExtra("isOwner", isOwner)
                                .putStringArrayListExtra("members", (ArrayList<String>) members),
                        REQUEST_CODE_MEMBER_REFRESH);
            }

            @Override public void onLongItemClick(View view, int position) {

            }
        });
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(GroupDetailsActivity.this);
                progressDialog.setMessage("add");
                progressDialog.setCanceledOnTouchOutside(false);
            }
            switch (requestCode) {
                case REQUEST_CODE_MEMBER_REFRESH:
                    progressDialog.show();
                    refreshGroup();
                    break;

                default:
                    break;
            }
        }
    }

    private void refreshMembers() {
        members.clear();
        members.addAll(group.getMembers());
        adapter.notifyDataSetChanged();
    }

    @OnClick({ R.id.text_exit_group, R.id.layout_member_list, R.id.layout_push_notification })
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_exit_group:
                exitGroup();
                break;
            case R.id.layout_member_list:
                startActivity(
                        new Intent(GroupDetailsActivity.this, MembersListActivity.class).putExtra(
                                "isOwner", isOwner)
                                .putExtra("groupId", groupId)
                                .putStringArrayListExtra("members", (ArrayList<String>) members));
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
                .setMessage("leave group")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(GroupDetailsActivity.this);
                            progressDialog.setMessage("group");
                            progressDialog.setCanceledOnTouchOutside(false);
                        }

                        if (group.getOwner() == null || "".equals(group.getOwner()) || !isOwner) {
                            leaveGroup();
                        }
                        if (isOwner) {
                            deleteGroup();
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
                            Toast.makeText(getApplicationContext(),
                                    " leave failure " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(getApplicationContext(), "failure" + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void refreshGroup() {
        refreshMembers();
        groupNameView.setText(group.getGroupName());
        memberSizeView.setText("(" + group.getMemberCount() + ")");
        progressDialog.dismiss();
    }

    protected void updateGroup() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    EMClient.getInstance().groupManager().getGroupFromServer(groupId);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            groupNameView.setText(group.getGroupName());
                            memberSizeView.setText("(" + group.getMemberCount() + ")");
                            refreshMembers();
                            if (EMClient.getInstance().getCurrentUser().equals(group.getOwner())) {
                                isOwner = true;
                                exitGroupView.setText("Delete group");
                            } else {
                                isOwner = false;
                                exitGroupView.setText("leave group");
                            }
                        }
                    });
                } catch (Exception e) {

                }
            }
        }).start();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    private class GroupChangeListener implements EMGroupChangeListener {

        @Override public void onInvitationReceived(String groupId, String groupName, String inviter,
                String reason) {
            // TODO Auto-generated method stub

        }

        @Override public void onRequestToJoinReceived(String s, String s1, String s2, String s3) {

        }

        @Override public void onRequestToJoinAccepted(String s, String s1, String s2) {

        }

        @Override public void onRequestToJoinDeclined(String s, String s1, String s2, String s3) {

        }

        @Override public void onInvitationAccepted(String groupId, String inviter, String reason) {
            runOnUiThread(new Runnable() {

                @Override public void run() {
                    refreshMembers();
                }
            });
        }

        @Override public void onInvitationDeclined(String groupId, String invitee, String reason) {
            // TODO Auto-generated method stub

        }

        @Override public void onUserRemoved(String groupId, String groupName) {
            finish();
        }

        @Override public void onGroupDestroyed(String groupId, String groupName) {
            finish();
        }

        @Override public void onAutoAcceptInvitationFromGroup(String groupId, String inviter,
                String inviteMessage) {
            // TODO Auto-generated method stub

        }
    }
}
