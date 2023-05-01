package io.agora.chatdemo.ui;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    public Activity mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = (Activity) context;
    }

    /**
     * hide keyboard
     */
    public void hideKeyboard() {
        if(mContext != null && mContext instanceof BaseActivity) {
            ((BaseActivity) mContext).hideKeyboard();
        }
    }
}

