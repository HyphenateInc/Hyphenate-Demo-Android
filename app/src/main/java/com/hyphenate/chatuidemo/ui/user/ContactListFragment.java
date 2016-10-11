package com.hyphenate.chatuidemo.ui.user;

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
import com.hyphenate.chatuidemo.R;

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

        List<UserEntity> list = new ArrayList<>();
        UserEntity userEntity = new UserEntity();
        userEntity.setHeader("B");
        userEntity.setUserId("ben");
        list.add(userEntity);

        UserEntity userEntity1 = new UserEntity();
        userEntity1.setHeader("D");
        userEntity1.setUserId("d1");
        list.add(userEntity1);

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setHeader("F");
        userEntity2.setUserId("fa");
        list.add(userEntity2);

        UserEntity userEntity3 = new UserEntity();
        userEntity3.setHeader("D");
        userEntity3.setUserId("d2");
        list.add(userEntity3);

        UserEntity userEntity4 = new UserEntity();
        userEntity4.setHeader("D");
        userEntity4.setUserId("说法叫老师");
        list.add(userEntity4);

        Collections.sort(list, new Comparator<UserEntity>() {
            @Override public int compare(UserEntity o1, UserEntity o2) {
                return o1.getUserId().compareTo(o2.getUserId());
            }
        });

        adapter = new ContactListAdapter(getActivity(), list);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ContactListAdapter.OnItemClickListener() {
            @Override public void ItemClickListener() {

            }
        });
    }

    @OnClick(R.id.layout_group_entry) void onclick() {
        startActivity(new Intent(getActivity(), GroupListActivity.class));
    }
}
