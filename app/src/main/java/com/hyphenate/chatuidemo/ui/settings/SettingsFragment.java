package com.hyphenate.chatuidemo.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.EMCallBack;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.ui.MainActivity;
import com.hyphenate.chatuidemo.ui.sign.SigninActivity;

/**
 * Created by lzan13 on 2016/10/11.
 */
public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.em_fragment_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    /**
     * Call sign out
     */
    @OnClick(R.id.btn_sign_out) void singOut() {
        DemoHelper.getInstance().signOut(new EMCallBack() {
            @Override public void onSuccess() {
                startActivity(new Intent(getActivity(), SigninActivity.class));
                ((MainActivity) getActivity()).finish();
            }

            @Override public void onError(int i, String s) {

            }

            @Override public void onProgress(int i, String s) {

            }
        });
    }
}
