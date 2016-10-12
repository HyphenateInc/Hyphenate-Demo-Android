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
import butterknife.OnClick;
import butterknife.Unbinder;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.widget.EaseConversationListView;

/**
 * A fragment which shows conversation list
 */
public class ConversationListFragment extends Fragment {

    private Unbinder mUnbinder;

    @BindView(R.id.list_view) EaseConversationListView mCvsListView;


    public ConversationListFragment() {
        // Required empty public constructor
    }

    public static ConversationListFragment newInstance() {
        ConversationListFragment fragment = new ConversationListFragment();
        return fragment;
    }


    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.em_fragment_conversation_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCvsListView.init();
    }

    @OnClick(R.id.btn_test) void test(){
        startActivity(new Intent(getActivity(), ChatActivity.class));
    }


    @Override public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
