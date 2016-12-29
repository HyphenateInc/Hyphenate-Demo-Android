package com.hyphenate.chatuidemo.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.apply.ApplyActivity;
import com.hyphenate.chatuidemo.ui.MainActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseConversationListView;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.util.NetUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.hyphenate.easeui.EaseConstant.CHATTYPE_GROUP;

/**
 * A fragment which shows mConversation list
 */
public class ConversationListFragment extends Fragment {

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

    @Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EMClient.getInstance().addConnectionListener(connectionListener);

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
                EMConversation conversation = mConversationListView.getItem(position);
                if (conversation.conversationId().equals(Constant.CONVERSATION_NAME_APPLY)) {
                    startActivity(new Intent(getActivity(), ApplyActivity.class));
                } else {
                    //enter to chat activity
                    if(conversation.getType() == EMConversation.EMConversationType.GroupChat){
                        startActivity(new Intent(getActivity(), ChatActivity.class)
                                .putExtra(Constant.EXTRA_USER_ID, conversation.conversationId())
                                .putExtra(EaseConstant.EXTRA_CHAT_TYPE, CHATTYPE_GROUP));
                    }else if(conversation.getType() == EMConversation.EMConversationType.Chat){
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
            EMConversation tobeDeleteCons = mConversationListView.getItem(mItemLongClickPos);
            if(tobeDeleteCons == null){
                return true;
            }
            EMClient.getInstance().chatManager().deleteConversation(tobeDeleteCons.conversationId(), true);
            refresh();

            ((MainActivity)getActivity()).updateUnreadMsgLabel();
        }
        return true;
    }

    protected EMConnectionListener connectionListener = new EMConnectionListener() {

        @Override
        public void onDisconnected(final int error) {
            if (error == EMError.USER_REMOVED || error == EMError.USER_LOGIN_ANOTHER_DEVICE) {

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
        EMClient.getInstance().removeConnectionListener(connectionListener);
        mUnbinder.unbind();
    }
}
