package com.hyphenate.chatuidemo.group;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by linan on 17/3/30.
 */

public class GroupUtils {

    /**
     * load more data byte cursor or by page
     */
    public static class LoadMoreData<T> {
        public static final int PAGE_SIZE = 20;

        private boolean isLoading;
        private boolean isFirstLoading = true;
        private boolean hasMoreData = true;
        private String cursor;
        private Object loadMutex = new Object();

        private List<T> adapterDataList;
        private RecyclerView.Adapter adapter;
        private List<T> fetchData = new ArrayList<>();

        private Runnable initialAction;
        private Runnable loadAction;
        private Runnable onNoMoreAction;

        private Activity activity;

        public LoadMoreData(Activity activity, List<T> dataList, RecyclerView.Adapter adapter, Runnable initialAction, Runnable loadAction, Runnable onNoMoreDataAction) {
            this.activity = activity;
            this.adapterDataList = dataList;
            this.adapter = adapter;
            this.initialAction = initialAction;
            this.loadAction = loadAction;
            this.onNoMoreAction = onNoMoreDataAction;
        }

        public void load(){

            new Thread(new Runnable() {

                public void run() {
                    synchronized (loadMutex) {
                        if (hasMoreData == false) {
                            return;
                        }
                        isLoading = true;

                        if (isFirstLoading) {
                            initialAction.run();

                            isFirstLoading = false;
                            adapterDataList.addAll(fetchData);
                            fetchData.clear();;
                        }

                        loadAction.run();

                        activity.runOnUiThread(new Runnable() {

                            public void run() {
                                adapterDataList.addAll(fetchData);
                                if (fetchData.size() < PAGE_SIZE) {
                                    hasMoreData = false;

                                    onNoMoreAction.run();
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
                        isLoading = false;
                    }
                }
            }).start();
        }

        public String getCursor() {
            return cursor;
        }

        public boolean hasMoreData() {
            return hasMoreData;
        }

        public boolean isLoading() {
            return isLoading;
        }

        public void reset() {
            synchronized (loadMutex) {
                isLoading = false;
                isFirstLoading = true;
                hasMoreData = true;
                cursor = "";
            }
        }

        public void setFetchResult(EMCursorResult<T> data) {
            fetchData.clear();
            fetchData.addAll(data.getData());
            cursor = data.getCursor();
        }
    }

    /**
     * Muc Role judge
     */
    interface MucRoleJudge {

        void update(EMGroup group);

        boolean isOwner(String name);

        boolean isAdmin(String name);

        boolean isMuted(String name);
    }

    /**
     * Muc Role judge default implementation
     */
    public static class MucRoleJudgeImpl implements MucRoleJudge {

        private String owner;
        private Set<String> adminSet = new HashSet<>();
        private Set<String> muteSet = new HashSet<>();

        public void update(EMGroup group) {
            owner = group.getOwner();
            adminSet.clear();
            adminSet.addAll(group.getAdminList());
            muteSet.clear();
            muteSet.addAll(group.getMuteList());
        }

        @Override
        public boolean isOwner(String name) {
            return owner.equals(name);
        }

        @Override
        public boolean isAdmin(String name) {
            if (name == null || name.isEmpty()) {
                return false;
            }
            return adminSet.contains(name);
        }

        @Override
        public boolean isMuted(String name) {
            if (name == null || name.isEmpty()) {
                return false;
            }
            return muteSet.contains(name);
        }
    };
}
