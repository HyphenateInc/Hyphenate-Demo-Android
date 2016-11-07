package com.hyphenate.chatuidemo.ui.user;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;
import butterknife.OnClick;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;

import com.hyphenate.chatuidemo.ui.call.VideoCallActivity;
import com.hyphenate.chatuidemo.ui.call.VoiceCallActivity;
import com.hyphenate.chatuidemo.ui.chat.ChatActivity;
import com.hyphenate.chatuidemo.ui.apply.ApplyActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by benson on 2016/10/8.
 */

public class ContactListFragment extends Fragment {

    private static String TAG = ContactListFragment.class.getSimpleName();
    private LocalBroadcastManager localBroadcastManager;
    private ContactsBroadcastReceiver broadcastReceiver;

    @BindView(R.id.rv_contacts) RecyclerView recyclerView;
    ShowDialogFragment dialogFragment;

    LinearLayoutManager layoutManager;
    ContactListAdapter adapter;

    private List<UserEntity> entityList;

    public static ContactListFragment newInstance() {
        return new ContactListFragment();
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRecyclerView();
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.em_fragment_contact_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private void setRecyclerView() {
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        refresh();

        adapter.setOnItemClickListener(new EaseListItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                UserEntity user = entityList.get(position);
                showDialog(user);
            }

            @Override public void onItemLongClick(View view, int position) {
                UserEntity user = entityList.get(position);
                deleteContacter(user);
            }
        });
    }

    private void showDialog(final UserEntity user) {

        dialogFragment = new ShowDialogFragment();
        dialogFragment.show(getFragmentManager(), "dialog");

        dialogFragment.setOnShowDialogClickListener(
                new ShowDialogFragment.OnShowDialogClickListener() {
                    @Override public String showNameView() {
                        if (!TextUtils.isEmpty(user.getNickname())) {
                            return user.getNickname();
                        } else {
                            return user.getUsername();
                        }
                    }

                    @Override public String showAvatarView() {
                        return user.getAvatar();
                    }

                    @Override public void onVoiceCallClick() {
                        startActivity(new Intent(getActivity(), VoiceCallActivity.class).putExtra(
                                EaseConstant.EXTRA_USER_ID, user.getUsername())
                                .putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false));
                        dialogFragment.dismiss();
                    }

                    @Override public void onSendMessageClick() {
                        startActivity(new Intent(getActivity(), ChatActivity.class).putExtra(
                                EaseConstant.EXTRA_USER_ID, user.getUsername()));
                        dialogFragment.dismiss();
                    }

                    @Override public void onVideoCallClick() {
                        startActivity(new Intent(getActivity(), VideoCallActivity.class).putExtra(
                                EaseConstant.EXTRA_USER_ID, user.getUsername())
                                .putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false));
                        dialogFragment.dismiss();
                    }
                });
    }

    /**
     * delete contacter
     */
    private void deleteContacter(final UserEntity userEntity) {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMClient.getInstance().contactManager().deleteContact(userEntity.getUsername());
                    DemoHelper.getInstance().deleteContacts(userEntity);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            refresh();
                            Toast.makeText(getActivity(), "contacter is deleted", Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void refresh() {

        loadContacts();

        if (adapter == null) {
            adapter = new ContactListAdapter(getActivity(), entityList);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Load contacts
     */
    private void loadContacts() {
        if (entityList == null) {
            entityList = new ArrayList<UserEntity>();
        }
        entityList.clear();
        entityList.addAll(DemoHelper.getInstance().getContactList().values());
        // sort
        Collections.sort(entityList, new Comparator<UserEntity>() {
            @Override public int compare(UserEntity o1, UserEntity o2) {
                return o1.getUsername().compareTo(o2.getUsername());
            }
        });
    }

    /**
     * Contacts broadcast receiver
     */
    private class ContactsBroadcastReceiver extends BroadcastReceiver {

        @Override public void onReceive(Context context, Intent intent) {
            EMLog.d(TAG, "contact action");
            refresh();
        }
    }




    @Override public void onResume() {
        super.onResume();

        // register broadcast register
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        ContactsBroadcastReceiver broadcastReceiver = new ContactsBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(EaseConstant.BROADCAST_ACTION_CONTACTS);
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);
        // refresh ui
        refresh();
    }

    @Override public void onStop() {
        super.onStop();
        // unregister broadcast receiver
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }

    @OnClick({ R.id.layout_group_entry, R.id.layout_apply_entry }) void onclick(View v) {
        switch (v.getId()) {
            case R.id.layout_group_entry:
                startActivity(new Intent(getActivity(), GroupListActivity.class));
                break;
            case R.id.layout_apply_entry:
                startActivity(new Intent(getActivity(), ApplyActivity.class));
                break;
        }
    }
}
