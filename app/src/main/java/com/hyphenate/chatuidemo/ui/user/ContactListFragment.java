package com.hyphenate.chatuidemo.ui.user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.chatuidemo.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by benson on 2016/10/8.
 */

public class ContactListFragment extends Fragment {

    @BindView(R.id.rv_contacts)
    RecyclerView recyclerView;

    LinearLayoutManager layoutManager;
    ContactListAdapter adapter;

    public static ContactListFragment newInstance() {
        return new ContactListFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRecyclerView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.em_fragment_contact_list, container, false);
        ButterKnife.bind(this,view);
        return view;
    }

    private void setRecyclerView(){
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        list.add("e");

        adapter = new ContactListAdapter(getActivity(),list);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ContactListAdapter.OnItemClickListener() {
            @Override
            public void ItemClickListener() {

            }
        });
    }

}
