package com.hyphenate.chatuidemo.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

/**
 * Created by wei on 2016/10/22.
 */

public class VoiceRecordDialog extends Dialog{
    VoiceRecordView recordView;

    public VoiceRecordDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        recordView = new VoiceRecordView(context);
        setContentView(recordView);
    }

    public void setRecordCallback(VoiceRecordView.VoiceRecordCallback recordCallback){
        recordView.setRecordCallback(recordCallback);
    }
}
