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
package io.agora.chatdemo.group;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.PushConfigs;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.bus.BusEvent;
import io.agora.chatdemo.bus.LiveDataBus;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.chatdemo.utils.ThreadPoolManager;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupDetailsActivity extends BaseActivity {

    private final int REQUEST_CODE_MEMBER_REFRESH = 1;
    private final int REQUEST_CODE_TRANSFER_OWNER = 110;

    private String groupId;
    private Group group;
    private PushConfigs pushConfigs;

    private MucMembersVerticalAdapter adapter;
    private ProgressDialog progressDialog;
    ProgressBar progressBar;
    Toolbar toolbar;

    public static GroupDetailsActivity instance;
    ChangeTextDialogFragment changeDialog;


    @BindView(R.id.text_group_details_name) TextView groupNameView;
    @BindView(R.id.text_group_details_desc) TextView groupDescView;
    @BindView(R.id.text_group_details_member_size) TextView memberSizeView;
    @BindView(R.id.recycler_member) RecyclerView recyclerView;
    @BindView(R.id.text_exit_group) TextView exitGroupView;
    @BindView(R.id.text_allow_member_to_invite) TextView inviteView;
    @BindView(R.id.text_appear_in_group_search) TextView groupTypeView;
    @BindView(R.id.switch_push_notification) Switch notificationSwitch;
    @BindView(R.id.txt_group_id) TextView groupIdView;
    @BindView(R.id.layout_member_list) LinearLayout layoutMemberView;
    @BindView(R.id.iv_invite_member) ImageView invite_member;
    @BindView(R.id.switch_block_group_message) Switch switch_block_group_message;

    List<String> members = new ArrayList<>();
    private boolean isOwner = false;
    GroupUtils.MucRoleJudge mucRoleJudge = new GroupUtils.MucRoleJudgeImpl();

    private DefaultAgoraGroupChangeListener listener;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.em_activity_group_details);
        ButterKnife.bind(this);
        instance = this;

        progressBar = new ProgressBar(this);
        groupId = getIntent().getStringExtra("groupId");
        groupIdView.setText(groupId);

        group = ChatClient.getInstance().groupManager().getGroup(groupId);

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
        setToolbarMoreIconCustomColor(toolbar, R.color.white_background);

        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        toolbar.addView(progressBar, params);

        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        listener = new DefaultAgoraGroupChangeListener();
        ChatClient.getInstance().groupManager().addGroupChangeListener(listener);
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_item_transfer_owner:
                    startActivityForResult(new Intent(GroupDetailsActivity.this, GroupTransferOwnerActivity.class).putExtra("groupId", groupId)
                            , REQUEST_CODE_TRANSFER_OWNER);
                    break;
                case R.id.menu_item_clear_conversation:
                    ChatClient.getInstance().chatManager().getConversation(groupId).clearAllMessages();
                    finish();
                    break;
                 case R.id.menu_item_change_group_name:
                     changeGroupTitle();
                    break;
                case R.id.menu_item_change_group_desc:
                    changeGroupDescription();
                    break;
                case R.id.menu_item_admin_list:
                    startActivity(new Intent(GroupDetailsActivity.this, GroupAdminActivity.class).putExtra("groupId", groupId));
                    break;
                case R.id.menu_item_black_list:
                    startActivity(new Intent(GroupDetailsActivity.this, GroupBlackListOrMuteActivity.class).putExtra("groupId", groupId).putExtra("is_black_list", true));
                    break;
                case R.id.menu_item_mute_list:
                    startActivity(new Intent(GroupDetailsActivity.this, GroupBlackListOrMuteActivity.class).putExtra("groupId", groupId).putExtra("is_mute", true));
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
        String currentUser = ChatClient.getInstance().getCurrentUser();
        if (!mucRoleJudge.isOwner(currentUser)) {
            menu.findItem(R.id.menu_item_transfer_owner).setEnabled(false);
            menu.findItem(R.id.menu_item_change_group_name).setEnabled(false);
            menu.findItem(R.id.menu_item_change_group_desc).setEnabled(false);
            menu.findItem(R.id.menu_item_admin_list).setEnabled(false);
        }
        if (!mucRoleJudge.isOwner(currentUser) && !mucRoleJudge.isAdmin(currentUser)) {
            menu.findItem(R.id.menu_item_black_list).setEnabled(false);
            menu.findItem(R.id.menu_item_mute_list).setEnabled(false);
        }
        return true;
    }

    private void initLocalView() {
        groupNameView.setText(group.getGroupName());
        groupDescView.setText(group.getDescription());
        memberSizeView.setText("(" + group.getMemberCount() + ")");
        invite_member.setVisibility(GroupUtils.isCanAddMember(group) ? View.VISIBLE : View.INVISIBLE);

        if (ChatClient.getInstance().getCurrentUser().equals(group.getOwner())) {
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
        List<String> noPushGroups = ChatClient.getInstance().pushManager().getNoPushGroups();
        if (noPushGroups != null && noPushGroups.contains(groupId)) {
            notificationSwitch.setChecked(true);
        }else{
            notificationSwitch.setChecked(false);
        }

        switch_block_group_message.setChecked(group.isMsgBlocked());
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
            }else if(requestCode == REQUEST_CODE_TRANSFER_OWNER) {
                finish();
            }
        }
    }

    @OnClick({ R.id.text_exit_group, R.id.layout_member_list, R.id.layout_push_notification, R.id.switch_push_notification,
            R.id.iv_invite_member, R.id.switch_block_group_message }) void onclick(View view) {
        switch (view.getId()) {
            case R.id.iv_invite_member:

                List<String> existsMembers = new ArrayList<>();
                existsMembers.addAll(members);
                existsMembers.addAll(group.getMembers());
                startActivityForResult(new Intent(GroupDetailsActivity.this, InviteMembersActivity.class).putExtra("groupId", groupId)
                        .putExtra("isOwner", isOwner)
                        .putStringArrayListExtra("members", (ArrayList<String>) existsMembers), REQUEST_CODE_MEMBER_REFRESH);
                break;
            case R.id.text_exit_group:
                exitGroupUI();
                break;
            case R.id.layout_member_list://show member list
                startActivityForResult(new Intent(GroupDetailsActivity.this, GroupMembersListActivity.class).putExtra("isOwner", isOwner)
                        .putExtra("groupId", groupId)
                        .putStringArrayListExtra("members", (ArrayList<String>) members), REQUEST_CODE_MEMBER_REFRESH);
                break;

            case R.id.switch_push_notification:
                //if (notificationSwitch.isChecked()) {
                //    notificationSwitch.setChecked(false);
                //} else {
                //    notificationSwitch.setChecked(true);
                //}
                toggleBlockPushGroupNotification();
                break;
            case R.id.switch_block_group_message:
                toggleBlockGroup();
                break;
        }
    }

    private void toggleBlockPushGroupNotification(){
        if(notificationSwitch.isChecked()){
            showDialog(getString(R.string.em_group_block_group_push), getString(R.string.em_waiting));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        List<String> noPushGroups = new ArrayList<String>();
                        noPushGroups.add(groupId);
                        ChatClient.getInstance().pushManager().updatePushServiceForGroup(noPushGroups, true);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.em_group_block_group_push) + " failed", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).start();

        } else {
            showDialog(getString(R.string.em_group_unblock_group_push), getString(R.string.em_waiting));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        List<String> noPushGroups = new ArrayList<String>();
                        noPushGroups.add(groupId);
                        ChatClient.getInstance().pushManager().updatePushServiceForGroup(noPushGroups, false);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.em_group_unblock_group_push) + " failed", Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }
            }).start();
        }
    }

    private void toggleBlockGroup() {
        EMLog.d("GroupDetail", "toggleBlockGroup:" + switch_block_group_message.isChecked());
        if(switch_block_group_message.isChecked()){
            showDialog(getString(R.string.em_group_block_group_message), getString(R.string.em_waiting));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        ChatClient.getInstance().groupManager().blockGroupMessage(groupId);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.em_group_unblock_group_message) + " failed", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }).start();

        } else {
            showDialog(getString(R.string.em_group_unblock_group_message), getString(R.string.em_waiting));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        ChatClient.getInstance().groupManager().unblockGroupMessage(groupId);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.em_group_block_group_message) + " failed", Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                }
            }).start();
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
        DemoHelper.getInstance().execute(new Runnable() {
            public void run() {
                try {
                    ChatClient.getInstance().groupManager().leaveGroup(groupId);
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
        });
    }

    /**
     * delete group
     */
    private void deleteGroup() {
        showDialog(getString(R.string.em_delete_group), getString(R.string.em_waiting));
        DemoHelper.getInstance().execute(new Runnable() {
            public void run() {
                try {
                    ChatClient.getInstance().groupManager().destroyGroup(groupId);
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
        });
    }

    private void changeGroupTitle() {
        changeDialog = new ChangeTextDialogFragment();
        changeDialog.setDialogListener(new ChangeTextDialogFragment.DialogListener() {
            @Override
            public String getTitle() {
                return "Change group title";
            }

            @Override
            public String getContent() {
                return group.getGroupName();
            }

            @Override
            public void onChangeTo(final String content) {
                ThreadPoolManager.getInstance().executeTask(new ThreadPoolManager.Task() {

                    @Override
                    @WorkerThread
                    public Object onRequest() throws ChatException {
                        ChatClient.getInstance().groupManager().changeGroupName(groupId, content);
                        return null;
                    }

                    @Override
                    @UiThread
                    public void onSuccess(Object o) {
                        LiveDataBus.get().with(BusEvent.REFRESH_GROUP).setValue(true);
                        changeDialog.dismiss();
                        updateGroupFromServer();
                    }

                    @Override
                    @UiThread
                    public void onError(ChatException exception) {
                        Snackbar.make(recyclerView, "Failed to change group name", Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancel() {
                changeDialog.dismiss();
            }
        });
        changeDialog.show(getSupportFragmentManager(), "change group name");
    }

    private void changeGroupDescription() {
        changeDialog = new ChangeTextDialogFragment();
        changeDialog.setDialogListener(new ChangeTextDialogFragment.DialogListener() {
            @Override
            public String getTitle() {
                return "Change group description";
            }

            @Override
            public String getContent() {
                return group.getDescription();
            }

            @Override
            public void onChangeTo(final String content) {
                ThreadPoolManager.getInstance().executeTask(new ThreadPoolManager.Task() {

                    @Override
                    @WorkerThread
                    public Object onRequest() throws ChatException {
                        ChatClient.getInstance().groupManager().changeGroupDescription(groupId, content);
                        return null;
                    }

                    @Override
                    @UiThread
                    public void onSuccess(Object o) {
                        LiveDataBus.get().with(BusEvent.REFRESH_GROUP_DES).setValue(true);
                        changeDialog.dismiss();
                        updateGroupFromServer();
                    }

                    @Override
                    @UiThread
                    public void onError(ChatException exception) {
                        Snackbar.make(recyclerView, "Failed to change group description", Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancel() {
                changeDialog.dismiss();
            }
        });

        FragmentManager fg = getSupportFragmentManager();
        changeDialog.show(fg, "change group description");
    }

    protected void updateGroupFromServer() {
        ThreadPoolManager.getInstance().executeTask(new ThreadPoolManager.Task() {

            @Override
            public Object onRequest() throws ChatException {
                ChatClient.getInstance().pushManager().getPushConfigsFromServer();
                group = ChatClient.getInstance().groupManager().getGroupFromServer(groupId);
                return null;
            }

            @Override
            public void onSuccess(Object o) {
                initLocalView();
                toolbar.removeView(progressBar);
            }

            @Override
            public void onError(ChatException exception) {
                toolbar.removeView(progressBar);
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        instance = null;
        ChatClient.getInstance().groupManager().removeGroupChangeListener(listener);
    }

    private void showDialog(String title, String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(GroupDetailsActivity.this);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.setTitle(title);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    private class DefaultAgoraGroupChangeListener extends AgoraGroupChangeListener {
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

        @Override public void onAutoAcceptInvitationFromGroup(final String s, String s1, String s2) {
            if (!groupId.equals(s)) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override public void run() {
                    members.clear();
                    members.addAll(ChatClient.getInstance().groupManager().getGroup(s).getMembers());
                    memberSizeView.setText("(" + group.getMemberCount() + ")");
                    adapter.notifyDataSetChanged();
                }
            });
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
