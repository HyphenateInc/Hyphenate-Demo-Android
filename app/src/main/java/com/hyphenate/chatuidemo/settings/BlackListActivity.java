package com.hyphenate.chatuidemo.settings;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.BaseActivity;
import com.hyphenate.exceptions.HyphenateException;

/**
 * Created by lzan13 on 2016/10/27.
 * Blacklist activity
 */

public class BlackListActivity extends BaseActivity {

    private BaseActivity mActivity;

    @BindView(R.id.recycler_view_blacklist) RecyclerView mRecyclerView;

    private BlackListAdapter mAdapter;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_blacklist);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * Setup layout
     */
    private void initView() {
        mActivity = this;

        getSupportActionBar().setTitle(R.string.em_blacklist);
        getActionBarToolbar().setNavigationIcon(R.drawable.em_ic_back);
        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        mAdapter = new BlackListAdapter(mActivity);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

        mRecyclerView.setAdapter(mAdapter);

        setItemClickListener();
    }

    /**
     * Refresh ui
     */
    private void refresh() {
        if (mAdapter != null) {
            mAdapter.refreshBlackList();
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Set item click listener
     */
    private void setItemClickListener() {
        mAdapter.setItemClickListener(new BlackListAdapter.ItemClickListener() {
            /**
             * Item action event
             *
             * @param username unblock username
             * @param action item action
             */
            @Override public void onItemAction(String username, int action) {
                switch (action) {
                    case 0:
                        unblockUser(username);
                        break;
                }
            }
        });
    }

    /**
     * unblock user
     */
    private void unblockUser(final String username) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.be_removing));
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        new Thread(new Runnable() {
            @Override public void run() {
                boolean success = true;
                try {
                    EMClient.getInstance().contactManager().removeUserFromBlackList(username);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    success = false;
                }
                final boolean finalSuccess = success;
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        pd.dismiss();
                        if(finalSuccess) {
                            refresh();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.removed_from_blacklist_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }
}
