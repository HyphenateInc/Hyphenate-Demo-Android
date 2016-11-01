package com.hyphenate.chatuidemo.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.apply.ApplyActivity;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseConversationListView;
import com.hyphenate.easeui.widget.EaseListItemClickListener;

/**
 * A fragment which shows conversation list
 */
public class ConversationListFragment extends Fragment {

    private Unbinder mUnbinder;

    @BindView(R.id.list_view) EaseConversationListView mConversationListView;

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
        // init ConversationListView
        mConversationListView.init();
        // set item click listener
        mConversationListView.setOnItemClickListener(new EaseListItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                EMConversation conversation = mConversationListView.getItem(position);
                if (conversation.getUserName().equals(EaseConstant.CONVERSATION_NAME_APPLY)) {
                    startActivity(new Intent(getActivity(), ApplyActivity.class));
                } else {
                    //enter to chat activity
                    startActivity(new Intent(getActivity(), ChatActivity.class).putExtra(
                            EaseConstant.EXTRA_USER_ID, conversation.getUserName()));
                }
            }

            @Override public void onLongItemClick(View view, int position) {

            }
        });
    }

    @Override public void onResume() {
        super.onResume();
        //refresh list
        mConversationListView.refresh();
    }

    public void refresh() {
        mConversationListView.refresh();
    }

    /**
     * filter conversation list with passed query string
     */
    public void filter(String str) {
        mConversationListView.filter(str);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
