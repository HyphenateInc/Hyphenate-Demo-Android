package com.hyphenate.chatuidemo.user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.utils.EaseUserUtils;

/**
 * Created by benson on 2016/10/20.
 */

public class ShowDialogFragment extends DialogFragment {

    @BindView(R.id.img_contact_dialog_avatar) ImageView avatarView;
    @BindView(R.id.text_contact_dialog_name) TextView nameView;
    @BindView(R.id.img_contact_dialog_voice_call) ImageView voiceCallView;
    @BindView(R.id.img_contact_dialog_send_message) ImageView sendMessageView;
    @BindView(R.id.img_contact_dialog_video_call) ImageView videoCallView;

    OnShowDialogClickListener listener;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.em_show_dialog, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (listener != null) {
            EaseUserUtils.setUserAvatar(getActivity(), listener.getUserId(), avatarView);
            EaseUserUtils.setUserNick(listener.getUserId(), nameView);
        }
    }

    @OnClick({
            R.id.img_contact_dialog_voice_call, R.id.img_contact_dialog_send_message, R.id.img_contact_dialog_video_call
    }) public void submit(View view) {
        switch (view.getId()) {
            case R.id.img_contact_dialog_voice_call:
                if (listener != null) {
                    listener.onVoiceCallClick();
                }
                break;

            case R.id.img_contact_dialog_send_message:
                if (listener != null) {
                    listener.onSendMessageClick();
                }
                break;

            case R.id.img_contact_dialog_video_call:
                if (listener != null) {
                    listener.onVideoCallClick();
                }
                break;
        }
    }

    public interface OnShowDialogClickListener {

        String getUserId();

        void onVoiceCallClick();

        void onSendMessageClick();

        void onVideoCallClick();
    }

    public void setOnShowDialogClickListener(OnShowDialogClickListener listener) {
        this.listener = listener;
    }
}
