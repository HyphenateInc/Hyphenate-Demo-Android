package com.hyphenate.chatuidemo.group;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.user.ContactListAdapter;
import com.hyphenate.chatuidemo.user.model.UserEntity;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_invite_members);

        ButterKnife.bind(this);

        membersList = getIntent().getStringArrayListExtra("members");
        isOwner = getIntent().getBooleanExtra("isOwner", false);
        groupId = getIntent().getStringExtra("groupId");

        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        initToolbar();

        refresh();

        //需要再加一个判断,群成员是否有邀请好友的权限
        if (membersList != null && !TextUtils.isEmpty(groupId)) {
            adapter = new ContactListAdapter(this, entityList, true, membersList, isOwner, groupId);
        } else {
            adapter = new ContactListAdapter(this, entityList, true);
        }
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new EaseListItemClickListener() {
            @Override public void onItemClick(View view, int position) {
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
        return true;
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
            adapter = new ContactListAdapter(InviteMembersActivity.this, entityList, false, membersList, isOwner, groupId);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
}
