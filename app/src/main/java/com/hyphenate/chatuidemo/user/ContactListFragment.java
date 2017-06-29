package com.hyphenate.chatuidemo.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.apply.ApplyActivity;
import com.hyphenate.chatuidemo.call.VideoCallActivity;
import com.hyphenate.chatuidemo.call.VoiceCallActivity;
import com.hyphenate.chatuidemo.chat.ChatActivity;
import com.hyphenate.chatuidemo.chatroom.ChatRoomListActivity;
import com.hyphenate.chatuidemo.group.GroupListActivity;
import com.hyphenate.chatuidemo.user.model.UserEntity;
import com.hyphenate.chatuidemo.user.model.UserProfileManager;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by benson on 2016/10/8.
 */

public class ContactListFragment extends Fragment {

    private static String TAG = ContactListFragment.class.getSimpleName();

    @BindView(R.id.text_unread_notifications_number) TextView unreadNumberView;
    @BindView(R.id.rv_contacts) RecyclerView recyclerView;
    @BindView(R.id.progressbar) ProgressBar progressBar;
    ShowDialogFragment dialogFragment;

    LinearLayoutManager layoutManager;
    ContactListAdapter adapter;

    private List<UserEntity> userList;

    private UserProfileManager mUserManager;

    public static ContactListFragment newInstance() {
        return new ContactListFragment();
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUserManager = DemoHelper.getInstance().getUserManager();
        setRecyclerView();
        getContactsFromServer();
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
                UserEntity user = userList.get(position);
                itemClick(user);
            }

            @Override public void onItemLongClick(View view, int position) {
                UserEntity user = userList.get(position);
                itemLongClick(user);
            }
        });
    }

    private void getContactsFromServer() {
        progressBar.setVisibility(View.VISIBLE);
        DemoHelper.getInstance().getUserManager().fetchContactsFromServer(new EMCallBack() {
            @Override public void onSuccess() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        progressBar.setVisibility(View.GONE);
                        refresh();
                    }
                });
            }

            @Override public void onError(int i, final String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        progressBar.setVisibility(View.GONE);
                        refresh();
                        Snackbar.make(recyclerView, "failure:" + s, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

            @Override public void onProgress(int i, String s) {

            }
        });
    }

    public void filter(String newText) {
        List<UserEntity> list = new ArrayList<>();
        if (userList == null) {
            userList = new ArrayList<>();
        }
        userList.clear();
        userList.addAll(mUserManager.getContactList().values());
        for (UserEntity userEntity : userList) {
            if (userEntity.getNickname().contains(newText)) {
                list.add(userEntity);
            }
        }
        userList.clear();
        userList.addAll(list);
        adapter.notifyDataSetChanged();
    }

    /**
     * Item click event
     *
     * @param user current user item
     */
    private void itemClick(final UserEntity user) {

        dialogFragment = new ShowDialogFragment();
        dialogFragment.show(getFragmentManager(), "dialog");

        dialogFragment.setOnShowDialogClickListener(new ShowDialogFragment.OnShowDialogClickListener() {
            @Override public String getUserId() {
                return user.getUsername();
            }

            @Override public void onVoiceCallClick() {
                startActivity(new Intent(getActivity(), VoiceCallActivity.class).putExtra(EaseConstant.EXTRA_USER_ID, user.getUsername())
                        .putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false));
                dialogFragment.dismiss();
            }

            @Override public void onSendMessageClick() {
                startActivity(new Intent(getActivity(), ChatActivity.class).putExtra(EaseConstant.EXTRA_USER_ID, user.getUsername()));
                dialogFragment.dismiss();
            }

            @Override public void onVideoCallClick() {
                startActivity(new Intent(getActivity(), VideoCallActivity.class).putExtra(EaseConstant.EXTRA_USER_ID, user.getUsername())
                        .putExtra(EaseConstant.EXTRA_IS_INCOMING_CALL, false));
                dialogFragment.dismiss();
            }
        });
    }

    /**
     * item long click event
     *
     * @param userEntity current click item
     */
    private void itemLongClick(final UserEntity userEntity) {

        String[] menus = { getString(R.string.em_delete_contact), getString(R.string.em_add_to_blacklist) };

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setItems(menus, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        deleteContacts(userEntity);
                        break;
                    case 1:
                        moveToBlacklist(userEntity);
                        break;
                }
            }
        });
        AlertDialog contactsMenuDialog = alertDialogBuilder.create();
        contactsMenuDialog.show();
    }

    /**
     * delete contacts
     */
    private void deleteContacts(final UserEntity userEntity) {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMClient.getInstance().contactManager().deleteContact(userEntity.getUsername());
                    mUserManager.deleteContact(userEntity);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            refresh();
                            Toast.makeText(getActivity(), getString(R.string.em_delete_contact), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Add user to Blacklist
     */
    private void moveToBlacklist(final UserEntity userEntity) {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    EMClient.getInstance().contactManager().addUserToBlackList(userEntity.getUsername(), true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(getActivity(), R.string.move_into_blacklist_success, Toast.LENGTH_SHORT).show();
                            refresh();
                        }
                    });
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), R.string.move_into_blacklist_failure, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void refresh() {

        loadContacts();

        if (unreadNumberView != null) {
            EMConversation conversation = EMClient.getInstance()
                    .chatManager()
                    .getConversation(Constant.CONVERSATION_NAME_APPLY, EMConversation.EMConversationType.Chat, true);
            int count = conversation.getUnreadMsgCount();
            if (count != 0) {
                unreadNumberView.setText(String.valueOf(count));
                unreadNumberView.setVisibility(View.VISIBLE);
            } else {
                unreadNumberView.setVisibility(View.GONE);
            }
        }

        if (adapter == null) {
            adapter = new ContactListAdapter(getActivity(), userList);
            if (recyclerView != null) {
                recyclerView.setAdapter(adapter);
            }
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Load contacts
     */
    private void loadContacts() {
        if (userList == null) {
            userList = new ArrayList<>();
        }
        synchronized (userList){
            userList.clear();
            Iterator<Map.Entry<String, UserEntity>> iterator = mUserManager.getContactList().entrySet().iterator();
            List<String> blackList = EMClient.getInstance().contactManager().getBlackListUsernames();
            while (iterator.hasNext()) {
                Map.Entry<String, UserEntity> entry = iterator.next();
                if(!blackList.contains(entry.getKey())){
                    //filter out users in blacklist
                    UserEntity user = entry.getValue();
                    userList.add(user);
                }
            }
        }
        // sort
        Collections.sort(userList, new Comparator<UserEntity>() {
            @Override public int compare(UserEntity o1, UserEntity o2) {
                return o1.getUsername().compareTo(o2.getUsername());
            }
        });
    }

    @Override public void onResume() {
        super.onResume();
        // refresh ui
        refresh();
    }

    @Override public void onStop() {
        super.onStop();
    }

    @OnClick({ R.id.layout_group_entry, R.id.layout_chatroom_entry, R.id.layout_apply_entry }) void onclick(View v) {
        switch (v.getId()) {
            case R.id.layout_group_entry:
                startActivity(new Intent(getActivity(), GroupListActivity.class));
                break;
            case R.id.layout_chatroom_entry:
                startActivity(new Intent(getActivity(), ChatRoomListActivity.class));
                break;
            case R.id.layout_apply_entry:
                startActivity(new Intent(getActivity(), ApplyActivity.class));
                break;
        }
    }
}
