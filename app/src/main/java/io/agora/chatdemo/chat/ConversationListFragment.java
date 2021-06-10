package io.agora.chatdemo.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.agora.ConnectionListener;
import io.agora.Error;
import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chatdemo.Constant;
import io.agora.chatdemo.R;
import io.agora.chatdemo.apply.ApplyActivity;
import io.agora.chatdemo.bus.BusEvent;
import io.agora.chatdemo.bus.LiveDataBus;
import io.agora.chatdemo.ui.BaseFragment;
import io.agora.chatdemo.ui.MainActivity;
import io.agora.easeui.EaseConstant;
import io.agora.easeui.widget.EaseConversationListView;
import io.agora.easeui.widget.EaseListItemClickListener;
import io.agora.util.NetUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static io.agora.easeui.EaseConstant.CHATTYPE_GROUP;

/**
 * A fragment which shows mConversation list
 */
public class ConversationListFragment extends BaseFragment {

    private Unbinder mUnbinder;
    private int mItemLongClickPos;

    @BindView(R.id.list_view) EaseConversationListView mConversationListView;
    @BindView(R.id.layout_disconnected_indicator) LinearLayout mIndicatorLayout;
    @BindView(R.id.tv_connect_errormsg) TextView mDisconnectErrorView;

    public ConversationListFragment() {
        // Required empty public constructor
    }

    public static ConversationListFragment newInstance() {
        ConversationListFragment fragment = new ConversationListFragment();
        return fragment;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.em_fragment_conversation_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LiveDataBus.get().with(BusEvent.REFRESH_GROUP, Boolean.class).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean != null) {
                    refresh();
                }
            }
        });
    }

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ChatClient.getInstance().addConnectionListener(connectionListener);

        // init ConversationListView
        mConversationListView.init();
        List<String> hiddenList = new ArrayList<>();
        hiddenList.add(Constant.CONVERSATION_NAME_APPLY);
        mConversationListView.setHiddenList(hiddenList);

        mConversationListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override public void onCreateContextMenu(ContextMenu menu, View v,
                    ContextMenu.ContextMenuInfo menuInfo) {
                getActivity().getMenuInflater().inflate(R.menu.em_delete_conversation, menu);
            }
        });
        // set item click listener
        mConversationListView.setOnItemClickListener(new EaseListItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                Conversation conversation = mConversationListView.getItem(position);
                if (conversation.conversationId().equals(Constant.CONVERSATION_NAME_APPLY)) {
                    startActivity(new Intent(getActivity(), ApplyActivity.class));
                } else {
                    //enter to chat activity
                    if(conversation.getType() == Conversation.ConversationType.GroupChat){
                        startActivity(new Intent(getActivity(), ChatActivity.class)
                                .putExtra(Constant.EXTRA_USER_ID, conversation.conversationId())
                                .putExtra(EaseConstant.EXTRA_CHAT_TYPE, CHATTYPE_GROUP));
                    }else if(conversation.getType() == Conversation.ConversationType.Chat){
                        startActivity(new Intent(getActivity(), ChatActivity.class).putExtra(
                                Constant.EXTRA_USER_ID, conversation.conversationId()));
                    }
                }
            }

            @Override public void onItemLongClick(View view, int position) {
                mItemLongClickPos = position;
                mConversationListView.showContextMenu();
            }
        });
    }

    @Override public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.delete_conversation){
            Conversation tobeDeleteCons = mConversationListView.getItem(mItemLongClickPos);
            if(tobeDeleteCons == null){
                return true;
            }
            ChatClient.getInstance().chatManager().deleteConversation(tobeDeleteCons.conversationId(), true);
            refresh();

            ((MainActivity)getActivity()).updateUnreadMsgLabel();
        }
        return true;
    }

    protected ConnectionListener connectionListener = new ConnectionListener() {

        @Override
        public void onDisconnected(final int error) {
            if (error == Error.USER_REMOVED || error == Error.USER_LOGIN_ANOTHER_DEVICE) {

            } else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mIndicatorLayout.setVisibility(View.VISIBLE);
                        if (NetUtils.hasNetwork(getActivity())){
                            mDisconnectErrorView.setText(R.string.can_not_connect_chat_server_connection);
                        } else {
                            mDisconnectErrorView.setText(R.string.current_network_unavailable);
                        }
                    }

                });
            }
        }

        @Override
        public void onConnected() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mIndicatorLayout.setVisibility(View.GONE);
                }

            });
        }
    };

    @Override public void onResume() {
        super.onResume();
        //refresh list
        mConversationListView.refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        filter(null);
        hideKeyboard();
    }

    public void refresh() {
        mConversationListView.refresh();
    }

    /**
     * filter Conversation list with passed query string
     */
    public void filter(String str) {
        mConversationListView.filter(str);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        ChatClient.getInstance().removeConnectionListener(connectionListener);
        mUnbinder.unbind();
    }
}
