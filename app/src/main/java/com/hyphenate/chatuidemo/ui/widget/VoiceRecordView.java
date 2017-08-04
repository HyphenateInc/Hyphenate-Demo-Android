package com.hyphenate.chatuidemo.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.EMError;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.chat.VoiceRecorder;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowVoicePlayClickListener;

/**
 * Created by wei on 2016/10/21.
 */

public class VoiceRecordView extends FrameLayout {
    @BindView(R.id.view_speaking_shadow) View mShadowView;
    @BindView(R.id.iv_voice_btn) ImageView mVoiceButton;
    @BindView(R.id.txt_recording_hint) TextView mRecordingHint;
    @BindView(R.id.chronometer) Chronometer mChronometer;

    private VoiceRecorder mVoiceRecorder;

    protected PowerManager.WakeLock mWakeLock;
    private Context mContext;

    private final float maxScale = 1.38f;
    private final float minScale = 1.10f;

    protected Handler micImageHandler = new Handler() {
        @Override public void handleMessage(android.os.Message msg) {

            // change image
            float scale = (msg.what / 12f) * 0.35f + 1;
            /*if (scale < minScale) {
                scale = minScale;
            } else */if (scale > maxScale) {
                scale = maxScale;
            }

            ViewCompat.animate(mShadowView)
                    .scaleX(scale).scaleY(scale).setDuration(50).start();
        }
    };

    public VoiceRecordView(Context context) {
        this(context, null);
    }

    public VoiceRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.em_widget_voice_record, this);
        ButterKnife.bind(this);
        this.mContext = context;

        mWakeLock = ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK, "voice_record_lock");

        mVoiceRecorder = new VoiceRecorder(micImageHandler);

        mVoiceButton.setOnTouchListener(new OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        try {
                            if (EaseChatRowVoicePlayClickListener.isPlaying) {
                                EaseChatRowVoicePlayClickListener.currentPlayListener.stopPlayVoice();
                            }
                            v.setPressed(true);
                            startRecording();
                        } catch (Exception e) {
                            v.setPressed(false);
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getY() < 0) {
                            showReleaseToCancelHint();
                        } else {
                            showMoveUpToCancelHint();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        v.setPressed(false);
                        if (event.getY() < 0) {
                            // discard the recorded audio.
                            discardRecording();
                        } else {
                            // stop recording and send voice file
                            try {
                                int length = stopRecoding();
                                if (length > 0) {
                                    if (mRecordCallback != null) {
                                        mRecordCallback.onVoiceRecordComplete(getVoiceFilePath(), length);
                                    }
                                } else if (length == EMError.FILE_INVALID) {
                                    Toast.makeText(getContext(),
                                            R.string.record_no_permission,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(),
                                            R.string.record_recording_time_is_too_short,
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), mContext.getString(R.string.record_failed) + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        return true;
                    default:
                        discardRecording();
                        return false;
                }
            }
        });
    }

    public String getVoiceFilePath() {
        if(mVoiceRecorder != null)
            return mVoiceRecorder.getVoiceFilePath();
        return null;
    }

    private void startRecording() {
        if (!EaseCommonUtils.isSdcardExist()) {
            Toast.makeText(mContext, R.string.record_need_sdcard_support, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        try {
            mWakeLock.acquire();
            //this.setVisibility(View.VISIBLE);
            mRecordingHint.setText(mContext.getString(R.string.record_move_up_to_cancel));
            mChronometer.setVisibility(View.VISIBLE);
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mVoiceRecorder.startRecording(mContext);
        } catch (Exception e) {
            e.printStackTrace();
            discardRecording();
            //this.setVisibility(View.INVISIBLE);
            Toast.makeText(mContext, R.string.record_recoding_fail, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private int stopRecoding() {
        //this.setVisibility(View.INVISIBLE);
        mRecordingHint.setTextColor(getResources().getColor(R.color.voice_recording_hint));
        mRecordingHint.setText(R.string.record_tap_to_record);
        stopChronometer();
        if (mWakeLock.isHeld())
            mWakeLock.release();
        return mVoiceRecorder.stopRecoding();
    }

    private void discardRecording() {
        if (mWakeLock.isHeld()) mWakeLock.release();
        // stop recording
        if (mVoiceRecorder.isRecording()) {
            mVoiceRecorder.discardRecording();
        }
        mRecordingHint.setTextColor(getResources().getColor(R.color.voice_recording_hint));
        mRecordingHint.setText(R.string.record_tap_to_record);

        stopChronometer();
    }

    private void showMoveUpToCancelHint() {
        mRecordingHint.setTextColor(getResources().getColor(R.color.voice_recording_hint));
        mRecordingHint.setText(mContext.getString(R.string.record_move_up_to_cancel));
    }

    private void showReleaseToCancelHint() {
        mRecordingHint.setTextColor(getResources().getColor(R.color.voice_cancel_hint));
        mRecordingHint.setText(mContext.getString(R.string.record_release_to_cancel));
    }

    private void stopChronometer(){
        mChronometer.stop();
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.setVisibility(View.INVISIBLE);
    }

    public interface VoiceRecordCallback {
        /**
         * on voice record complete
         *
         * @param voiceFilePath
         * @param voiceTimeLength
         */
        void onVoiceRecordComplete(String voiceFilePath, int voiceTimeLength);
    }

    VoiceRecordCallback mRecordCallback;

    public void setRecordCallback(VoiceRecordCallback recordCallback){
        mRecordCallback = recordCallback;
    }

}
