package io.agora.easeui.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.agora.CallBack;
import io.agora.Error;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.FileMessageBody;
import io.agora.chatdemo.R;
import io.agora.easeui.utils.EaseCompat;

public class EaseShowNormalFileActivity extends Activity {
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ease_activity_show_file);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final ChatMessage message = getIntent().getParcelableExtra("msg");
        if (!(message.getBody() instanceof FileMessageBody)) {
            Toast.makeText(EaseShowNormalFileActivity.this, "Unsupported message body", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //download file
        message.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        EaseCompat.openFile(EaseShowNormalFileActivity.this,
                                ((FileMessageBody) message.getBody()).getLocalUri());
                        finish();
                    }
                });
            }

            @Override
            public void onError(int code, String error) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        EaseCompat.deleteFile(EaseShowNormalFileActivity.this, ((FileMessageBody) message.getBody()).getLocalUri());
                        String str4 = getResources().getString(R.string.Failed_to_download_file);
                        if (code == Error.FILE_NOT_FOUND) {
                            str4 = getResources().getString(R.string.File_expired);
                        }
                        Toast.makeText(getApplicationContext(), str4+message, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }

            @Override
            public void onProgress(int progress, String status) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressBar.setProgress(progress);
                    }
                });
            }
        });

        ChatClient.getInstance().chatManager().downloadAttachment(message);
    }
}
