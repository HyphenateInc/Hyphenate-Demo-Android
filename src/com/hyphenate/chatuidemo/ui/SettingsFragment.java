/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.chatuidemo.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.DemoHelper;
import com.hyphenate.chatuidemo.DemoModel;
import com.hyphenate.chatuidemo.R;

/**
 * settings screen
 * 
 * 
 */
public class SettingsFragment extends Fragment implements OnClickListener, CompoundButton.OnCheckedChangeListener {

	/**
	 * new message notification
	 */
	private RelativeLayout rl_switch_notification;
	/**
	 * sound
	 */
	private RelativeLayout rl_switch_sound;
	/**
	 * vibration
	 */
	private RelativeLayout rl_switch_vibrate;
	/**
	 * speaker
	 */
	private RelativeLayout rl_switch_speaker;


	/**
	 * line between sound and vibration
	 */
	private TextView textview1, textview2;

	private LinearLayout blacklistContainer;
	
	private LinearLayout userProfileContainer;
	
	/**
	 * logout
	 */
	private Button logoutBtn;
    private RelativeLayout rl_switch_delete_msg_when_exit_group;
    private RelativeLayout rl_switch_auto_accept_group_invitation;
    private RelativeLayout rl_switch_adaptive_video_encode;
 
	/**
	 * Diagnose
	 */
	private LinearLayout llDiagnose;
	/**
	 * display name for APNs
	 */
	private LinearLayout pushNick;
	
    private Switch notifiSwitch;
    private Switch soundSwitch;
    private Switch vibrateSwitch;
    private Switch speakerSwitch;
    private Switch ownerLeaveSwitch;
    private Switch switch_delete_msg_when_exit_group;
    private Switch switch_auto_accept_group_invitation;
    private Switch switch_adaptive_video_encode;
    private DemoModel settingsModel;
    private EMOptions chatOptions;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.em_fragment_conversation_settings, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState != null && savedInstanceState.getBoolean("isConflict", false))
            return;
		rl_switch_notification = (RelativeLayout) getView().findViewById(R.id.rl_switch_notification);
		rl_switch_sound = (RelativeLayout) getView().findViewById(R.id.rl_switch_sound);
		rl_switch_vibrate = (RelativeLayout) getView().findViewById(R.id.rl_switch_vibrate);
		rl_switch_speaker = (RelativeLayout) getView().findViewById(R.id.rl_switch_speaker);
		rl_switch_delete_msg_when_exit_group = (RelativeLayout) getView().findViewById(R.id.rl_switch_delete_msg_when_exit_group);
		rl_switch_auto_accept_group_invitation = (RelativeLayout) getView().findViewById(R.id.rl_switch_auto_accept_group_invitation);
		rl_switch_adaptive_video_encode = (RelativeLayout) getView().findViewById(R.id.rl_switch_adaptive_video_encode);
		
		notifiSwitch = (Switch) getView().findViewById(R.id.switch_notification);
		soundSwitch = (Switch) getView().findViewById(R.id.switch_sound);
		vibrateSwitch = (Switch) getView().findViewById(R.id.switch_vibrate);
		speakerSwitch = (Switch) getView().findViewById(R.id.switch_speaker);
		switch_delete_msg_when_exit_group = (Switch) getView().findViewById(R.id.switch_delete_msg_when_exit_group);
		switch_auto_accept_group_invitation = (Switch) getView().findViewById(R.id.switch_auto_accept_group_invitation);
		switch_adaptive_video_encode = (Switch) getView().findViewById(R.id.switch_adaptive_video_encode);

		notifiSwitch.setOnCheckedChangeListener(this);
		soundSwitch.setOnCheckedChangeListener(this);
		vibrateSwitch.setOnCheckedChangeListener(this);
		speakerSwitch.setOnCheckedChangeListener(this);
		switch_delete_msg_when_exit_group.setOnCheckedChangeListener(this);
		switch_auto_accept_group_invitation.setOnCheckedChangeListener(this);
		switch_adaptive_video_encode.setOnCheckedChangeListener(this);

		logoutBtn = (Button) getView().findViewById(R.id.btn_logout);
		if(!TextUtils.isEmpty(EMClient.getInstance().getCurrentUser())){
			logoutBtn.setText(getString(R.string.button_logout) + "(" + EMClient.getInstance().getCurrentUser() + ")");
		}

		textview1 = (TextView) getView().findViewById(R.id.textview1);
		textview2 = (TextView) getView().findViewById(R.id.textview2);
		
		blacklistContainer = (LinearLayout) getView().findViewById(R.id.ll_black_list);
		userProfileContainer = (LinearLayout) getView().findViewById(R.id.ll_user_profile);
		llDiagnose=(LinearLayout) getView().findViewById(R.id.ll_diagnose);
		pushNick=(LinearLayout) getView().findViewById(R.id.ll_set_push_nick);
		
		settingsModel = DemoHelper.getInstance().getModel();
		chatOptions = EMClient.getInstance().getOptions();
		
		blacklistContainer.setOnClickListener(this);
		userProfileContainer.setOnClickListener(this);
		logoutBtn.setOnClickListener(this);
		llDiagnose.setOnClickListener(this);
		pushNick.setOnClickListener(this);

		// 震动和声音总开关，来消息时，是否允许此开关打开
		// the vibrate and sound notification are allowed or not?
		if (settingsModel.getSettingMsgNotification()) {
			notifiSwitch.setChecked(true);
		} else {
		    notifiSwitch.setChecked(false);
		}
		
		// 是否打开声音
		// sound notification is switched on or not?
		if (settingsModel.getSettingMsgSound()) {
		    soundSwitch.setChecked(true);
		} else {
		    soundSwitch.setChecked(false);
		}
		
		// 是否打开震动
		// vibrate notification is switched on or not?
		if (settingsModel.getSettingMsgVibrate()) {
		    vibrateSwitch.setChecked(true);
		} else {
		    vibrateSwitch.setChecked(false);
		}

		// the speaker is switched on or not?
		if (settingsModel.getSettingMsgSpeaker()) {
		    speakerSwitch.setChecked(true);
		} else {
		    speakerSwitch.setChecked(false);
		}

		// delete messages when exit group?
		if(settingsModel.isDeleteMessagesAsExitGroup()){
		    switch_delete_msg_when_exit_group.setChecked(true);
		} else {
		    switch_delete_msg_when_exit_group.setChecked(false);
		}
		
		if (settingsModel.isAutoAcceptGroupInvitation()) {
		    switch_auto_accept_group_invitation.setChecked(true);
		} else {
		    switch_auto_accept_group_invitation.setChecked(false);
		}
		
		if (settingsModel.isAdaptiveVideoEncode()) {
            switch_adaptive_video_encode.setChecked(true);
        } else {
            switch_adaptive_video_encode.setChecked(false);
        }
	}

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_logout:
			logout();
			break;
		case R.id.ll_black_list:
			startActivity(new Intent(getActivity(), BlacklistActivity.class));
			break;
		case R.id.ll_diagnose:
			startActivity(new Intent(getActivity(), DiagnoseActivity.class));
			break;
		case R.id.ll_set_push_nick:
			startActivity(new Intent(getActivity(), OfflinePushNickActivity.class));
			break;
		case R.id.ll_user_profile:
			startActivity(new Intent(getActivity(), UserProfileActivity.class).putExtra("setting", true)
			        .putExtra("username", EMClient.getInstance().getCurrentUser()));
			break;
		}
		
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()){
			case R.id.switch_notification:
				if (isChecked) {
					rl_switch_sound.setVisibility(View.VISIBLE);
					rl_switch_vibrate.setVisibility(View.VISIBLE);
					textview1.setVisibility(View.VISIBLE);
					textview2.setVisibility(View.VISIBLE);
					settingsModel.setSettingMsgNotification(true);

				} else {
					rl_switch_sound.setVisibility(View.GONE);
					rl_switch_vibrate.setVisibility(View.GONE);
					textview1.setVisibility(View.GONE);
					textview2.setVisibility(View.GONE);

					settingsModel.setSettingMsgNotification(false);
				}
				break;
			case R.id.switch_sound :
				if (isChecked) {
					settingsModel.setSettingMsgSound(true);
				} else {
					settingsModel.setSettingMsgSound(false);
				}
				break;
			case R.id.switch_vibrate :
				if (isChecked) {
					settingsModel.setSettingMsgVibrate(true);
				} else {
					settingsModel.setSettingMsgVibrate(false);
				}
				break;
			case R.id.switch_speaker :
				if (isChecked) {
					settingsModel.setSettingMsgVibrate(true);
				} else {
					settingsModel.setSettingMsgSpeaker(false);
				}
				break;
			case R.id.switch_delete_msg_when_exit_group :
				if(isChecked){
					settingsModel.setDeleteMessagesAsExitGroup(true);
					chatOptions.setDeleteMessagesAsExitGroup(true);
				}else{
					settingsModel.setDeleteMessagesAsExitGroup(false);
					chatOptions.setDeleteMessagesAsExitGroup(false);
				}
				break;
			case R.id.switch_auto_accept_group_invitation :
				if(isChecked){
					settingsModel.setAutoAcceptGroupInvitation(true);
					chatOptions.setAutoAcceptGroupInvitation(true);
				}else{
					settingsModel.setAutoAcceptGroupInvitation(false);
					chatOptions.setAutoAcceptGroupInvitation(false);
				}
				break;
			case R.id.switch_adaptive_video_encode :
				if (isChecked){
					settingsModel.setAdaptiveVideoEncode(true);
					EMClient.getInstance().callManager().getVideoCallHelper().setAdaptiveVideoFlag(true);
				}else{
					settingsModel.setAdaptiveVideoEncode(false);
					EMClient.getInstance().callManager().getVideoCallHelper().setAdaptiveVideoFlag(false);
				}
				break;
			default:
				break;
		}
	}




	void logout() {
		final ProgressDialog pd = new ProgressDialog(getActivity());
		String st = getResources().getString(R.string.Are_logged_out);
		pd.setMessage(st);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		DemoHelper.getInstance().logout(false,new EMCallBack() {
			
			@Override
			public void onSuccess() {
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						pd.dismiss();
						// show login screen
						((MainActivity) getActivity()).finish();
						startActivity(new Intent(getActivity(), LoginActivity.class));
						
					}
				});
			}
			
			@Override
			public void onProgress(int progress, String status) {
				
			}
			
			@Override
			public void onError(int code, String message) {
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						pd.dismiss();
						Toast.makeText(getActivity(), "unbind devicetokens failed", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
        if(((MainActivity)getActivity()).isConflict){
        	outState.putBoolean("isConflict", true);
        }else if(((MainActivity)getActivity()).getCurrentAccountRemoved()){
        	outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
        }
    }

}
