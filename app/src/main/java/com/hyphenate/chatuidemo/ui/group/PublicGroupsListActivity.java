package com.hyphenate.chatuidemo.ui.group;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupInfo;
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

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar.setTitle("Public Groups");
    }

    @Override public void loadGroupList() {
        loadAndShowData();
    }

    private void loadAndShowData() {
        final ProgressDialog progressDialog =
                ProgressDialog.show(this, "load public group...", "waiting...", false);
        new Thread(new Runnable() {

            public void run() {
                try {
                    isLoading = true;
                    final EMCursorResult<EMGroupInfo> result = EMClient.getInstance()
                            .groupManager()
                            .getPublicGroupsFromServer(pageSize, cursor);
                    final List<EMGroupInfo> returnGroups = result.getData();
                    runOnUiThread(new Runnable() {

                        public void run() {
                            groups.addAll(returnGroups);
                            if (returnGroups.size() != 0) {
                                cursor = result.getCursor();
                            }
                            progressDialog.dismiss();
                            if (isFirstLoading) {
                                isFirstLoading = false;
                                adapter =
                                        new GroupListAdapter(PublicGroupsListActivity.this, groups,
                                                true);
                                recyclerView.setAdapter(adapter);
                                adapter.setItemClickListener(new EaseListItemClickListener() {
                                    @Override
                                    public void onItemClick(final View view, final int position) {
                                        progressDialog.show();
                                        progressDialog.setTitle("join the group");
                                        new Thread(new Runnable() {
                                            @Override public void run() {
                                                try {
                                                    EMGroup group = EMClient.getInstance()
                                                            .groupManager()
                                                            .getGroupFromServer(groups.get(position)
                                                                    .getGroupId());
                                                    if (group != null) {
                                                        if (group.isMemberOnly()) {
                                                            EMClient.getInstance()
                                                                    .groupManager()
                                                                    .applyJoinToGroup(
                                                                            group.getGroupId(),
                                                                            "apply to join");
                                                        } else {
                                                            EMClient.getInstance()
                                                                    .groupManager()
                                                                    .joinGroup(group.getGroupId());
                                                        }
                                                    }
                                                    runOnUiThread(new Runnable() {
                                                        @Override public void run() {
                                                            progressDialog.dismiss();
                                                            view.setBackgroundResource(0);
                                                            ((Button) view).setText("REQUESTED");
                                                            view.setClickable(false);
                                                            view.setEnabled(false);
                                                            ((Button) view).setTextColor(
                                                                    Color.parseColor("#8798a4"));
                                                            Snackbar.make(recyclerView,
                                                                    "successful application,please waiting...",
                                                                    Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } catch (HyphenateException e) {
                                                    e.printStackTrace();
                                                    runOnUiThread(new Runnable() {
                                                        @Override public void run() {
                                                            progressDialog.dismiss();
                                                            Snackbar.make(recyclerView,
                                                                    "join failure,please again",
                                                                    Snackbar.LENGTH_SHORT).show();
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
                            Snackbar.make(recyclerView,
                                    "load failed, please check your network or try it later",
                                    Snackbar.LENGTH_SHORT).show();
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
