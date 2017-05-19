package com.hyphenate.chatuidemo.group;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.user.ContactListAdapter;
import com.hyphenate.chatuidemo.user.model.UserEntity;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by benson on 2016/10/24.
 */

public class InviteMembersActivity extends BaseActivity {

    @BindView(R.id.recycler_contact) RecyclerView recyclerView;

    LinearLayoutManager manager;
    ContactListAdapter adapter;
    List<UserEntity> entityList = new ArrayList<>();//all of contacts
    List<String> membersList;//all of the group members
    boolean isOwner = false;
    String groupId;
    static InviteMembersActivity instance;
    private Snackbar snackbar;
    private boolean isCreate;
    public List<String> selectedMembers;
    private String[] newMembers;
    private ProgressDialog progressDialog;
    String content, action;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_invite_members);

        ButterKnife.bind(this);

        instance = this;

        membersList = getIntent().getStringArrayListExtra("members");
        isOwner = getIntent().getBooleanExtra("isOwner", false);
        groupId = getIntent().getStringExtra("groupId");

        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        initToolbar();

        refresh();

        if (membersList != null && !TextUtils.isEmpty(groupId)) {
            adapter = new ContactListAdapter(this, entityList, true, membersList);

            action = getString(R.string.em_contact_invite);
        } else {
            adapter = new ContactListAdapter(this, entityList, true);

            action = getString(R.string.em_contact_next);

            isCreate = true;
        }
        content = "0 " + getString(R.string.em_contact_selected);
        snackbar = Snackbar.make(recyclerView, content, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(action, listener);
        snackbar.show();

        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new EaseListItemClickListener() {
            @Override public void onItemClick(View view, final int position) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isEnabled()) {
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false);
                    } else {
                        checkBox.setChecked(true);
                    }
                }
            }

            @Override public void onItemLongClick(View view, int position) {

            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = getActionBarToolbar();
        toolbar.inflateMenu(R.menu.em_contacts_menu);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.em_contacts_menu, menu);
        menu.findItem(R.id.menu_add_contacts).setVisible(false);
        MenuItem item = menu.findItem(R.id.menu_search);

        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override public boolean onQueryTextChange(String newText) {
                List<UserEntity> list = new ArrayList<>();
                if (entityList == null) {
                    entityList = new ArrayList<>();
                }
                entityList.clear();
                entityList.addAll(DemoHelper.getInstance().getUserManager().getContactList().values());
                for (UserEntity userEntity : entityList) {
                    if (userEntity.getNickname().contains(newText)) {
                        list.add(userEntity);
                    }
                }
                entityList.clear();
                entityList.addAll(list);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        return true;
    }

    @Override protected void onResume() {
        super.onResume();
        if (isCreate) {
            snackbar.show();
        }
    }

    public void refresh() {

        entityList.clear();
        for (UserEntity userEntity : DemoHelper.getInstance().getUserManager().getContactList().values()) {
            entityList.add(userEntity);
        }

        Collections.sort(entityList, new Comparator<UserEntity>() {
            @Override public int compare(UserEntity o1, UserEntity o2) {
                return o1.getUsername().compareTo(o2.getUsername());
            }
        });

        if (adapter == null) {
            adapter = new ContactListAdapter(InviteMembersActivity.this, entityList, false, membersList);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    // add checkbox listener
    public void checkBoxListener(CheckBox checkBox, final UserEntity userEntity) {
        if (selectedMembers == null) {
            selectedMembers = new ArrayList<>();
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (membersList != null && membersList.contains(userEntity.getUsername())) {
                        return;
                    }
                    if (!selectedMembers.contains(userEntity.getUsername())){
                        selectedMembers.add(userEntity.getUsername());
                    }
                } else {
                    if (selectedMembers.contains(userEntity.getUsername())) {
                        selectedMembers.remove(userEntity.getUsername());
                    }
                }

                newMembers = selectedMembers.toArray(new String[0]);

                if (newMembers.length > 1 || newMembers.length == 0) {
                    content = newMembers.length + " " + getString(R.string.em_contact_selected);
                } else if (newMembers.length == 1) {
                    content = newMembers[0];
                }
                snackbar.setText(content);

                action = isCreate ? getString(R.string.em_contact_next) : getString(R.string.em_contact_invite);

                snackbar.setAction(action, listener);
                snackbar.show();
            }
        });
    }

    //set Snackbar action listener
    View.OnClickListener listener = new View.OnClickListener() {
        @Override public void onClick(View v) {

            if (isCreate) {

                //create a new group
                startActivity(new Intent(InviteMembersActivity.this, NewGroupActivity.class).putStringArrayListExtra("newMembers",
                        (ArrayList<String>) selectedMembers));
            } else {

                if (newMembers == null || newMembers.length == 0){
                    content = "member is null";
                    snackbar = Snackbar.make(recyclerView, content, Snackbar.LENGTH_INDEFINITE);
                    snackbar.show();
                    return;
                }
                content = "0" + getString(R.string.em_contact_selected);
                snackbar = Snackbar.make(recyclerView, content, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(action, listener);
                snackbar.show();
                //add contacts to existing group
                progressDialog =
                        ProgressDialog.show(InviteMembersActivity.this, getString(R.string.em_invite_members), getString(R.string.em_waiting), false);
                new Thread(new Runnable() {
                    @Override public void run() {
                        try {
                            if (isOwner) {
                                EMClient.getInstance().groupManager().addUsersToGroup(groupId, newMembers);
                            } else {
                                EMClient.getInstance().groupManager().inviteUser(groupId, newMembers, null);
                            }

                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    progressDialog.dismiss();
                                    if (EMClient.getInstance().getOptions().isAutoAcceptGroupInvitation()) {
                                        Intent intent = new Intent();
                                        intent.putExtra("selectedMembers", (ArrayList<String>) selectedMembers);
                                        setResult(InviteMembersActivity.RESULT_OK, intent);
                                    } else {
                                        setResult(InviteMembersActivity.RESULT_OK);
                                    }
                                    finish();
                                }
                            });
                        } catch (final HyphenateException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    progressDialog.dismiss();
                                    Snackbar.make(recyclerView, "invite failure,please again" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        }
    };
}
