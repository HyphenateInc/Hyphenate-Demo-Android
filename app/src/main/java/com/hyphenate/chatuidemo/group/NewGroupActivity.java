package com.hyphenate.chatuidemo.group;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.chat.ChatActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.exceptions.HyphenateException;
import java.util.List;

/**
 * Created by benson on 2016/10/26.
 */

public class NewGroupActivity extends BaseActivity {

    @BindView(R.id.edit_new_group_name) EditText groupNameView;
    @BindView(R.id.text_new_group_members_size) TextView membersSizeView;
    @BindView(R.id.recycler_new_group_members) RecyclerView recyclerView;
    @BindView(R.id.layout_allow_members_to_invite) RelativeLayout inviteView;
    @BindView(R.id.switch_allow_members_to_invite) Switch inviteSwitch;
    @BindView(R.id.layout_appear_in_group_search) RelativeLayout groupTypeView;
    @BindView(R.id.switch_appear_in_group_search) Switch groupTypeSwitch;

    LinearLayoutManager manager;
    private List<String> newMembers;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        ButterKnife.bind(this);

        newMembers = getIntent().getStringArrayListExtra("newMembers");
        membersSizeView.setText(newMembers.size() + "/2000");
        initToolbar();
        initRecyclerView();
    }

    private void initToolbar() {
        Toolbar toolbar = getActionBarToolbar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView() {

        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(manager);

        MembersListAdapter adapter = new MembersListAdapter(this, newMembers, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setAdapter(adapter);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.em_contacts_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        menu.findItem(R.id.menu_add_contacts).setVisible(false);
        item.setIcon(null);
        item.setTitle("CREATE");

        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override public boolean onMenuItemClick(MenuItem item) {
                if (!TextUtils.isEmpty(groupNameView.getText().toString())) {
                    final ProgressDialog progressDialog = ProgressDialog.show(NewGroupActivity.this, "creating group", "waiting...", false);

                    new Thread(new Runnable() {
                        @Override public void run() {

                            EMGroupManager.EMGroupOptions options = new EMGroupManager.EMGroupOptions();
                            options.maxUsers = 200;
                            if (groupTypeSwitch.isChecked()) {
                                options.style = EMGroupManager.EMGroupStyle.EMGroupStylePublicJoinNeedApproval;
                            } else {
                                options.style = inviteSwitch.isChecked() ? EMGroupManager.EMGroupStyle.EMGroupStylePrivateMemberCanInvite
                                        : EMGroupManager.EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;
                            }

                            try {
                                final EMGroup group = EMClient.getInstance()
                                        .groupManager()
                                        .createGroup(groupNameView.getText().toString(), "new group", newMembers.toArray(new String[0]), "", options);
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();
                                        finish();
                                        startActivity(new Intent(NewGroupActivity.this, ChatActivity.class).putExtra(EaseConstant.EXTRA_CHAT_TYPE,
                                                EaseConstant.CHATTYPE_GROUP).putExtra(EaseConstant.EXTRA_USER_ID, group.getGroupId()));
                                    }
                                });
                            } catch (final HyphenateException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        progressDialog.dismiss();
                                        Snackbar.make(recyclerView, "create failure" + e.getLocalizedMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).start();
                } else {
                    Snackbar.make(recyclerView, "please input group name", Snackbar.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        return true;
    }

    @OnClick({ R.id.layout_allow_members_to_invite, R.id.layout_appear_in_group_search }) void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_allow_members_to_invite:
                if (inviteSwitch.isChecked()) {
                    inviteSwitch.setChecked(false);
                } else {
                    inviteSwitch.setChecked(true);
                }
                break;

            case R.id.layout_appear_in_group_search:
                if (groupTypeSwitch.isChecked()) {
                    groupTypeSwitch.setChecked(false);
                } else {
                    groupTypeSwitch.setChecked(true);
                }
                break;
        }
    }
}
