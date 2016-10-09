package com.hyphenate.chatuidemo.ui.chat;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hyphenate.chatuidemo.R;

public class ContactListFragment extends Fragment {


    public ContactListFragment() {
        // Required empty public constructor
    }

    public static ContactListFragment newInstance() {
        return new ContactListFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.em_fragment_contact_list, container, false);
    }

}
