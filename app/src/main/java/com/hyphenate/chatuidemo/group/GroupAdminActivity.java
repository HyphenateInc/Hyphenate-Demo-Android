package com.hyphenate.chatuidemo.group;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.chatuidemo.user.model.UserEntity;
import com.hyphenate.chatuidemo.user.model.UserProfileManager;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseExpandGridView;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.hyphenate.chatuidemo.group.GroupListActivity.toolbar;

/**
 * Created by linan on 17/3/30.
 */

public class GroupAdminActivity extends BaseActivity {
    String groupId;
    EMGroup group;

    List<String> gridAdapterData = new ArrayList<>();
    GridViewAdapter gridViewAdapter;

    CheckBoxAdapter checkBoxAdapter;
    List<UserEntity> entityList = new ArrayList<>();

    @BindView(R.id.grid_view)    EaseExpandGridView grid_view;
    @BindView(R.id.recycler_members)    RecyclerView recycler_members;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_group_admin_list);
        ButterKnife.bind(this);

        groupId = getIntent().getStringExtra("groupId");
        group = EMClient.getInstance().groupManager().getGroup(groupId);

        // grid view
        gridAdapterData.addAll(group.getAdminList());
        gridViewAdapter = new GridViewAdapter(this, 0, gridAdapterData);
        grid_view.setAdapter(gridViewAdapter);

        // check list
        List<String> list = new ArrayList<>();
        list.addAll(group.getAdminList());
        list.addAll(group.getMembers());
        entityList.addAll(UserProfileManager.convertContactList(list));
        EMLog.d(GroupAdminActivity.class.getSimpleName(), "entityList.size:" + entityList.size());

        checkBoxAdapter = new CheckBoxAdapter(this, entityList);
        recycler_members.setLayoutManager(new LinearLayoutManager(GroupAdminActivity.this, LinearLayoutManager.VERTICAL, false));
        recycler_members.setAdapter(checkBoxAdapter);

        toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.em_ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;
        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        updateMemberList();
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_item_group_admin_save:
                    saveAdmins();
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    private void saveAdmins() {
        // save admin list
        final List<String> toAdd = new ArrayList<>();
        final List<String> toRemove = new ArrayList<>();

        Set<String> adminSet = new HashSet<>();
        adminSet.addAll((ArrayList)group.getAdminList());
        for (String user : gridAdapterData) {
            if (!adminSet.contains(user)) {
                toAdd.add(user);
            }
        }

        Set<String> gridDataSet = new HashSet<>();
        gridDataSet.addAll(gridAdapterData);
        for (String user : adminSet) {
            if (!gridAdapterData.contains(user)) {
                toRemove.add(user);
            }
        }

        DemoHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                boolean result = true;
                HyphenateException exceptionResult = null;
                try {
                    for (String user : toAdd) {
                        EMClient.getInstance().groupManager().addGroupAdmin(groupId, user);
                    }
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    exceptionResult = e;
                    result = false;
                }
                try {
                    if (result) {
                        for (String user : toRemove) {
                            EMClient.getInstance().groupManager().removeGroupAdmin(groupId, user);
                        }
                    }
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    exceptionResult = e;
                    result = false;
                }

                final boolean fResult = result;
                final HyphenateException fExceptionResult = exceptionResult;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!fResult) {
                            Toast.makeText(GroupAdminActivity.this,
                                    String.format("Failed to save admin list, errorCode: %s, desc: %s",
                                            fExceptionResult.getErrorCode(),
                                            fExceptionResult.getDescription()),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.em_group_admin_menu, menu);
        return true;
    }

    private void updateMemberList() {
        DemoHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EMCursorResult<String> result = EMClient.getInstance().groupManager().fetchGroupMembers(groupId, "", 200);

                    final List<String> list = new ArrayList<>();
                    list.addAll(group.getAdminList());
                    list.addAll(result.getData());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            entityList.clear();
                            entityList.addAll(UserProfileManager.convertContactList(list));
                            EMLog.d(GroupAdminActivity.class.getSimpleName(), "entityList.size:" + entityList.size());
                            checkBoxAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    static class GridViewHolder {
        ImageView avatar;
        TextView nameView;
    }

    class GridViewAdapter extends ArrayAdapter<String> {
        private LayoutInflater layoutInflater;

        public GridViewAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            layoutInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GridViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.em_item_group_member_list_horizontal, parent, false);
                holder = new GridViewHolder();
                holder.avatar = (ImageView)convertView.findViewById(R.id.img_member_avatar);
                holder.nameView = (TextView) convertView.findViewById(R.id.text_member_name);
                convertView.setTag(holder);
            } else {
                holder = (GridViewHolder)convertView.getTag();
            }

            String username = getItem(position);

            EaseUserUtils.setUserNick(username, holder.nameView);
            EaseUserUtils.setUserAvatar(getContext(), username, holder.avatar);

            return convertView;
        }
    }


    void addGridAdmin(final String username) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!gridAdapterData.contains(username)) {
                    gridAdapterData.add(username);
                }
                gridViewAdapter.notifyDataSetChanged();
            }
        });
    }

    void removeGridAdmin(final String username) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gridAdapterData.remove(username);
                gridViewAdapter.notifyDataSetChanged();
            }
        });
    }

    //  ============================ checkbox adapter ============================
    public class CheckBoxAdapter extends RecyclerView.Adapter<CheckBoxAdapter.ViewHolder> {

        private Context context;
        private List<UserEntity> userEntities;

        public CheckBoxAdapter(Context context, List<UserEntity> list) {

            this.context = context;
            userEntities = list;
        }

        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.em_item_contact_list, parent, false);
            return new ViewHolder(view);
        }

        @Override public void onBindViewHolder(final ViewHolder holder, final int position) {

            final UserEntity user = userEntities.get(position);
            EaseUserUtils.setUserAvatar(context, user.getUsername(), holder.avatarView);
            EaseUserUtils.setUserNick(user.getUsername(), holder.contactNameView);

            //set checkbox listener
            holder.checkBoxView.setVisibility(View.VISIBLE);

            holder.checkBoxView.setChecked(gridAdapterData.contains(user.getUsername()));
            holder.checkBoxView.setEnabled(true);
            holder.checkBoxView.setClickable(false);

            if (position == 0 || user.getInitialLetter() != null && !user.getInitialLetter().equals(userEntities.get(position - 1).getInitialLetter())) {
                if (TextUtils.isEmpty(user.getInitialLetter())) {
                    holder.headerView.setVisibility(View.INVISIBLE);
                    holder.baseLineView.setVisibility(View.INVISIBLE);
                } else {
                    holder.headerView.setVisibility(View.VISIBLE);
                    holder.baseLineView.setVisibility(View.VISIBLE);
                    holder.headerView.setText(user.getInitialLetter());
                }
            } else {
                holder.headerView.setVisibility(View.INVISIBLE);
                holder.baseLineView.setVisibility(View.INVISIBLE);
            }

            holder.contactItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    holder.checkBoxView.setChecked(!holder.checkBoxView.isChecked());
                    boolean checked = holder.checkBoxView.isChecked();
                    String username = entityList.get(position).getUsername();
                    if (checked) {
                        addGridAdmin(username);
                    } else {
                        removeGridAdmin(username);
                    }
                }
            });
        }

        @Override public int getItemCount() {
            return userEntities.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.txt_contact_name) TextView contactNameView;
            @BindView(R.id.layout_contact_item) RelativeLayout contactItemLayout;
            @BindView(R.id.txt_header) TextView headerView;
            @BindView(R.id.txt_base_line) TextView baseLineView;
            @BindView(R.id.checkbox) CheckBox checkBoxView;
            @BindView(R.id.img_contact_avatar) EaseImageView avatarView;

            ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

    }
}
