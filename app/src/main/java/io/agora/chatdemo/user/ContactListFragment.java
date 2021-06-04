package io.agora.chatdemo.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chatdemo.Constant;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.apply.ApplyActivity;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.chatroom.ChatRoomListActivity;
import io.agora.chatdemo.group.GroupListActivity;
import io.agora.chatdemo.ui.BaseFragment;
import io.agora.chatdemo.user.model.UserEntity;
import io.agora.chatdemo.user.model.UserProfileManager;
import io.agora.easeui.EaseConstant;
import io.agora.easeui.widget.EaseListItemClickListener;
import io.agora.exceptions.ChatException;

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

public class ContactListFragment extends BaseFragment {

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
        DemoHelper.getInstance().getUserManager().fetchContactsFromServer(new CallBack() {
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

        if(dialogFragment != null && dialogFragment.isVisible()) {
            dialogFragment.dismiss();
        }
        dialogFragment = new ShowDialogFragment();
        dialogFragment.show(getChildFragmentManager(), "dialog");

        dialogFragment.setOnShowDialogClickListener(new ShowDialogFragment.OnShowDialogClickListener() {
            @Override public String getUserId() {
                return user.getUsername();
            }

            @Override public void onVoiceCallClick() {
//                if (CallManager.getInstance().getCallState() == CallManager.CallState.DISCONNECTED) {
//                    Intent intent = new Intent(getActivity(), VoiceCallActivity.class);
//                    CallManager.getInstance().setChatId(user.getUsername());
//                    CallManager.getInstance().setInComingCall(false);
//                    CallManager.getInstance().setCallType(CallManager.CallType.VOICE);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                } else {
//                    Intent intent = new Intent();
//                    if (CallManager.getInstance().getCallType() == CallManager.CallType.VIDEO) {
//                        intent.setClass(getActivity(), VideoCallActivity.class);
//                    } else {
//                        intent.setClass(getActivity(), VoiceCallActivity.class);
//                    }
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
                dialogFragment.dismiss();
            }

            @Override public void onSendMessageClick() {
                startActivity(new Intent(getActivity(), ChatActivity.class).putExtra(EaseConstant.EXTRA_USER_ID, user.getUsername()));
                dialogFragment.dismiss();
            }

            @Override public void onVideoCallClick() {
//                if (CallManager.getInstance().getCallState() == CallManager.CallState.DISCONNECTED) {
//                    Intent intent = new Intent(getActivity(), VideoCallActivity.class);
//                    CallManager.getInstance().setChatId(user.getUsername());
//                    CallManager.getInstance().setInComingCall(false);
//                    CallManager.getInstance().setCallType(CallManager.CallType.VIDEO);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                } else {
//                    Intent intent = new Intent();
//                    if (CallManager.getInstance().getCallType() == CallManager.CallType.VIDEO) {
//                        intent.setClass(getActivity(), VideoCallActivity.class);
//                    } else {
//                        intent.setClass(getActivity(), VoiceCallActivity.class);
//                    }
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                }
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
                    ChatClient.getInstance().contactManager().deleteContact(userEntity.getUsername());
                    mUserManager.deleteContact(userEntity);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            refresh();
                            Toast.makeText(getActivity(), getString(R.string.em_delete_contact), Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (ChatException e) {
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
                    ChatClient.getInstance().contactManager().addUserToBlackList(userEntity.getUsername(), true);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(getActivity(), R.string.move_into_blacklist_success, Toast.LENGTH_SHORT).show();
                            refresh();
                        }
                    });
                } catch (ChatException e) {
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
            Conversation conversation = ChatClient.getInstance()
                    .chatManager()
                    .getConversation(Constant.CONVERSATION_NAME_APPLY, Conversation.ConversationType.Chat, true);
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
            List<String> blackList = ChatClient.getInstance().contactManager().getBlackListUsernames();
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

    @Override public void onPause() {
        super.onPause();
        hideKeyboard();
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
