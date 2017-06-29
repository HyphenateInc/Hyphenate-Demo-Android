package com.hyphenate.chatuidemo.group;

import android.app.Activity;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v7.widget.RecyclerView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.DemoHelper;

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

        /**
         *
         */
        public interface onLoadListener {
            /**
             * callback when initial load
             */
            @WorkerThread
            void onInitialAction();

            /**
             * callback when each time to load data
             */
            @WorkerThread
            void onLoadAction();

            /**
             * callback when no more data to load
             */
            @UiThread
            void onNoMoreDataAction();
        }

        public static final int PAGE_SIZE = 20;

        private boolean isLoading;
        private boolean isFirstLoading = true;
        private boolean hasMoreData = true;
        private int pageNumber = 0;
        private String cursor;
        private Object loadMutex = new Object();

        private List<T> adapterDataList;
        private RecyclerView.Adapter adapter;
        private List<T> fetchData = new ArrayList<>();

        private onLoadListener onLoadListener;

        private Activity activity;


        public LoadMoreData(Activity activity, List<T> dataList, RecyclerView.Adapter adapter, onLoadListener listener) {
            this.activity = activity;
            this.adapterDataList = dataList;
            this.adapter = adapter;
            this.onLoadListener = listener;
            this.pageNumber = 0;
        }

        public void load() {

            DemoHelper.getInstance().execute(new Runnable() {

                public void run() {
                    synchronized (loadMutex) {
                        if (hasMoreData == false) {
                            return;
                        }
                        isLoading = true;

                        if (isFirstLoading) {
                            onLoadListener.onInitialAction();

                            isFirstLoading = false;
                            adapterDataList.addAll(fetchData);
                            fetchData.clear();;
                        }

                        onLoadListener.onLoadAction();
                        pageNumber++;

                        activity.runOnUiThread(new Runnable() {

                            public void run() {
                                adapterDataList.addAll(fetchData);
                                if (fetchData.size() < PAGE_SIZE) {
                                    hasMoreData = false;

                                    onLoadListener.onNoMoreDataAction();
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
                        isLoading = false;
                    }
                }
            });
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

        /**
         * Cursor type API
         * @return
         */
        public String getCursor() {
            return cursor;
        }

        /**
         * Cursor type API
         * @return
         */
        public void setFetchResult(EMCursorResult<T> data) {
            fetchData.clear();
            fetchData.addAll(data.getData());
            cursor = data.getCursor();
        }

        /**
         * Page type API
         * @return
         */
        public int getPageNumber() {
            return pageNumber;
        }

        /**
         * set Page number
         */
        public void setPageNumber(int number) {
            pageNumber = number;
        }

        /**
         * Page type API
         * @param data
         */
        public void setFetchResult(List<T> data) {
            fetchData.clear();
            fetchData.addAll(data);
        }

    }

    /**
     * Muc role judge
     */
    interface MucRoleJudge {

        void update(EMGroup group);

        boolean isOwner(String name);

        boolean isAdmin(String name);

        boolean isMuted(String name);
    }

    /**
     * Muc role judge's default implementation
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
    }


    static boolean isAdmin(EMGroup group, String id) {
        List<String> adminList = group.getAdminList();
        for (String admin : adminList) {
            if (id.equals(admin)) {
                return true;
            }
        }
        return false;
    }

    static boolean isCurrentOwner(EMGroup group) {
        return EMClient.getInstance().getCurrentUser().equals(group.getOwner());
    }

    /**
     * judge whether current user can invite member join group
     */
    public static boolean isCanAddMember(EMGroup group) {
        if (group.isMemberAllowToInvite() ||
                isAdmin(group, EMClient.getInstance().getCurrentUser()) ||
                isCurrentOwner(group)) {
            return true;
        }
        return false;
    }
}
