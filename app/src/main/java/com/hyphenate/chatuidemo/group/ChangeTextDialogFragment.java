package com.hyphenate.chatuidemo.group;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.hyphenate.chatuidemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by linan on 17/4/7.
 */

public class ChangeTextDialogFragment extends DialogFragment {
    @BindView(R.id.edit_content)    EditText edit_content;
    @BindView(R.id.text_input_layout)    TextInputLayout text_input_layout;

    DialogListener listener;

    @Nullable
    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.em_group_change_text, container, false);
        ButterKnife.bind(this, view);
        text_input_layout.setHint(listener.getTitle());
        edit_content.setText(listener.getContent());
        return view;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnClick({R.id.ok_action,R.id.cancel_action
    }) public void submit(View view) {
        switch (view.getId()) {
            case R.id.ok_action:
                listener.onChangeTo(edit_content.getText().toString());
                break;
            case R.id.cancel_action:
                listener.onCancel();
                break;
            default:
                break;
        }
    }

    public interface DialogListener {

        String getTitle();

        String getContent();

        void onChangeTo(String content);

        void onCancel();
    }

    public void setDialogListener(DialogListener listener) {
        this.listener = listener;
    }

}
