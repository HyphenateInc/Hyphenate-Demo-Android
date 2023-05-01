/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agora.easeui.widget.chatrow;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatMessage.ChatType;
import io.agora.chat.VoiceMessageBody;
import io.agora.chatdemo.R;
import io.agora.util.EMLog;

import java.io.File;

/**
 * Row voice playing event
 *
 */
public class EaseChatRowVoicePlayClickListener implements View.OnClickListener {
    private static final String TAG = "VoicePlayClickListener";
    ChatMessage message;
    VoiceMessageBody voiceBody;
    ImageView voiceIconView;

    //private AnimationDrawable voiceAnimation = null;
    MediaPlayer mediaPlayer = null;
    ImageView iv_read_status;
    Context context;
    private ChatType chatType;
    private BaseAdapter adapter;

    public static boolean isPlaying = false;
    public static EaseChatRowVoicePlayClickListener currentPlayListener = null;
    public static String playMsgId;

    public EaseChatRowVoicePlayClickListener(ChatMessage message, ImageView v, ImageView iv_read_status, BaseAdapter adapter, Context context) {
        this.message = message;
        voiceBody = (VoiceMessageBody) message.getBody();
        this.iv_read_status = iv_read_status;
        this.adapter = adapter;
        voiceIconView = v;
        this.context = context;
        this.chatType = message.getChatType();
    }

    public void stopPlayVoice() {
        //voiceAnimation.stop();
        if (message.direct() == ChatMessage.Direct.RECEIVE) {
            voiceIconView.setImageResource(R.drawable.ease_ic_voice_received_stopped);
        } else {
            voiceIconView.setImageResource(R.drawable.ease_ic_voice_sent_stopped);
        }
        // stop play voice
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        isPlaying = false;
        playMsgId = null;
        adapter.notifyDataSetChanged();
    }

    public void playVoice(String filePath) {
        if (!(new File(filePath).exists())) {
            return;
        }
        playMsgId = message.getMsgId();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        mediaPlayer = new MediaPlayer();
        //if (EaseUI.getInstance().getSettingsProvider().isSpeakerOpened()) {
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        //} else {
        //	audioManager.setSpeakerphoneOn(false);  // turn off speaker
        //	// Change the audio source to earpiece
        //	audioManager.setMode(AudioManager.MODE_IN_CALL);
        //	mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        //}
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mediaPlayer.release();
                    mediaPlayer = null;
                    stopPlayVoice(); // stop animation
                }

            });
            isPlaying = true;
            currentPlayListener = this;
            mediaPlayer.start();
            showAnimation();

            // If the message is receivable
            if (message.direct() == ChatMessage.Direct.RECEIVE) {
                if (!message.isAcked() && chatType == ChatType.Chat) {
                    // Make message read
                    ChatClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
                }
                if (!message.isListened() && iv_read_status != null && iv_read_status.getVisibility() == View.VISIBLE) {
                    // Hide not played voice message indicator
                    iv_read_status.setVisibility(View.INVISIBLE);
                    message.setListened(true);
                    ChatClient.getInstance().chatManager().setVoiceMessageListened(message);
                }
            }

        } catch (Exception e) {
            System.out.println();
        }
    }

    // show the voice playing animation
    private void showAnimation() {
        // play voice, and start animation
        if (message.direct() == ChatMessage.Direct.RECEIVE) {
            voiceIconView.setImageResource(R.drawable.ease_ic_voice_received_playing);
        } else {
            voiceIconView.setImageResource(R.drawable.ease_ic_voice_sent_playing);
        }
        //voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
        //voiceAnimation.start();
    }

    @Override
    public void onClick(View v) {
        String st = context.getResources().getString(R.string.Is_download_voice_click_later);
        if (isPlaying) {
            if (playMsgId != null && playMsgId.equals(message.getMsgId())) {
                currentPlayListener.stopPlayVoice();
                return;
            }
            currentPlayListener.stopPlayVoice();
        }

        if (message.direct() == ChatMessage.Direct.SEND) {
            // for sent msg, we will try to play the voice file directly
            playVoice(voiceBody.getLocalUrl());
        } else {
            if (message.status() == ChatMessage.Status.SUCCESS) {
                File file = new File(voiceBody.getLocalUrl());
                if (file.exists() && file.isFile())
                    playVoice(voiceBody.getLocalUrl());
                else
                    EMLog.e(TAG, "file not exist");

            } else if (message.status() == ChatMessage.Status.INPROGRESS) {
                Toast.makeText(context, st, Toast.LENGTH_SHORT).show();
            } else if (message.status() == ChatMessage.Status.FAIL) {
                Toast.makeText(context, st, Toast.LENGTH_SHORT).show();
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        ChatClient.getInstance().chatManager().downloadAttachment(message);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                        adapter.notifyDataSetChanged();
                    }

                }.execute();

            }

        }
    }
}
