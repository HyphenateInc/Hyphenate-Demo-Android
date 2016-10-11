package com.hyphenate.chatuidemo.ui.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.OnClick;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.DemoApplication;
import com.hyphenate.chatuidemo.R;

import com.hyphenate.exceptions.HyphenateException;
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

    @BindView(R.id.rv_contacts) RecyclerView recyclerView;

    LinearLayoutManager layoutManager;
    ContactListAdapter adapter;

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

        Collections.sort(DemoApplication.getInstance().getContactList(), new Comparator<UserEntity>() {
            @Override public int compare(UserEntity o1, UserEntity o2) {
                return o1.getUserId().compareTo(o2.getUserId());
            }
        });

       refresh();

        adapter.setOnItemClickListener(new ContactListAdapter.OnItemClickListener() {
            @Override public void ItemClickListener() {

            }
        });
    }

    public void refresh(){
        if (adapter == null){
            adapter = new ContactListAdapter(getActivity(), DemoApplication.getInstance().getContactList());
            recyclerView.setAdapter(adapter);
        }else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override public void onResume() {
        super.onResume();
        if (DemoApplication.getInstance().getContactList().size() == 0) {
            final ProgressDialog dialog =
                    ProgressDialog.show(getActivity(), "loading...", "waiting...", false);
            new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        List<String> contacts =
                                EMClient.getInstance().contactManager().getAllContactsFromServer();
                        for (String name : contacts) {
                            UserEntity user = new UserEntity();
                            user.setUserId(name);
                            user.setHeader(name.subSequence(0, 1).toString().toUpperCase());
                            DemoApplication.getInstance().setContactList(user);
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override public void run() {
                                dialog.dismiss();
                                refresh();
                            }
                        });
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override public void run() {

                                dialog.dismiss();
                            }
                        });
                    }
                }
            }).start();
        }
    }

    @OnClick(R.id.layout_group_entry) void onclick() {
        startActivity(new Intent(getActivity(), GroupListActivity.class));
    }
}
