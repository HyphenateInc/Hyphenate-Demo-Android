package com.hyphenate.chatuidemo.group;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupInfo;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.exceptions.HyphenateException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by benson on 2016/10/27.
 */

public class PublicGroupsListActivity extends GroupListActivity {

    private List<EMGroupInfo> groups = new ArrayList<>();
    private boolean isLoading;
    private boolean isFirstLoading = true;
    private boolean hasMoreData = true;
    private String cursor;
    private final int pageSize = 20;
    List<EMGroupInfo> temp = new ArrayList<EMGroupInfo>();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar.setTitle(R.string.em_public_group);
        loadAndShowData();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.em_contacts_menu, menu);
        menu.findItem(R.id.menu_add_contacts).setVisible(false);
        item = menu.findItem(R.id.menu_search);

        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override public boolean onQueryTextChange(String newText) {
                List<EMGroupInfo> list = new ArrayList<EMGroupInfo>();

                groups.clear();
                groups.addAll(temp);
                for (EMGroupInfo group : groups) {
                    if (group.getGroupName().contains(newText)) {
                        list.add(group);
                    }
                }
                groups.clear();
                groups.addAll(list);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        return true;
    }

    private void loadAndShowData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.em_load_public_group));
        progressDialog.setMessage(getString(R.string.em_waiting));
        progressDialog.setCanceledOnTouchOutside(false);
        if (isFirstLoading) {
            progressDialog.show();
        }
        new Thread(new Runnable() {

            public void run() {
                try {
                    isLoading = true;
                    final EMCursorResult<EMGroupInfo> result = EMClient.getInstance().groupManager().getPublicGroupsFromServer(pageSize, cursor);
                    final List<EMGroupInfo> returnGroups = result.getData();
                    runOnUiThread(new Runnable() {

                        public void run() {
                            groups.addAll(returnGroups);
                            if (returnGroups.size() != 0) {
                                cursor = result.getCursor();
                                if (returnGroups.size() == pageSize){
                                    if (adapter != null){
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }

                            progressDialog.dismiss();
                            if (isFirstLoading) {
                                isFirstLoading = false;
                                temp.addAll(groups);
                                adapter = new GroupListAdapter(PublicGroupsListActivity.this, groups, true);
                                recyclerView.setAdapter(adapter);
                                adapter.setItemClickListener(new EaseListItemClickListener() {
                                    @Override public void onItemClick(final View view, final int position) {
                                        progressDialog.show();
                                        progressDialog.setTitle("join the group");
                                        new Thread(new Runnable() {
                                            @Override public void run() {
                                                try {
                                                    EMGroup group = EMClient.getInstance()
                                                            .groupManager()
                                                            .getGroupFromServer(groups.get(position).getGroupId());

                                                    if (group != null && EMClient.getInstance().groupManager().getAllGroups().contains(group)){
                                                        runOnUiThread(new Runnable() {
                                                            @Override public void run() {
                                                                progressDialog.dismiss();
                                                                Snackbar.make(recyclerView,"you have already joined this group",Snackbar.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                        return;
                                                    }
                                                    if (group != null) {
                                                        if (group.isMemberOnly()) {
                                                            EMClient.getInstance()
                                                                    .groupManager()
                                                                    .applyJoinToGroup(group.getGroupId(), "apply to join");
                                                        } else {
                                                            EMClient.getInstance().groupManager().joinGroup(group.getGroupId());
                                                        }
                                                    }
                                                    runOnUiThread(new Runnable() {
                                                        @Override public void run() {
                                                            progressDialog.dismiss();
                                                            view.setBackgroundResource(0);
                                                            ((Button) view).setText("REQUESTED");
                                                            view.setClickable(false);
                                                            view.setEnabled(false);
                                                            ((Button) view).setTextColor(Color.parseColor("#8798a4"));
                                                            Snackbar.make(recyclerView, "successful application,please wait",
                                                                    Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } catch (final HyphenateException e) {
                                                    e.printStackTrace();
                                                    runOnUiThread(new Runnable() {
                                                        @Override public void run() {
                                                            progressDialog.dismiss();
                                                            Snackbar.make(recyclerView, "join failure,please again" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                        }).start();
                                    }

                                    @Override public void onItemLongClick(View view, int position) {

                                    }
                                });
                            } else {
                                if (returnGroups.size() < pageSize) {
                                    hasMoreData = false;
                                }
                                adapter.notifyDataSetChanged();
                            }
                            isLoading = false;
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            isLoading = false;
                            progressDialog.dismiss();
                            Snackbar.make(recyclerView, "load failed, please check your network or try it later", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (manager.getItemCount() != 0) {
                        int lasPos = manager.findLastVisibleItemPosition();
                        if (hasMoreData && !isLoading && lasPos == manager.getItemCount() - 1) {
                            loadAndShowData();
                        }
                    }
                }
            }
        });
    }
}
