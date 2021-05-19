package io.agora.easeui.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.FileMessageBody;
import io.agora.chatdemo.R;
import io.agora.util.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EaseShowNormalFileActivity extends Activity {
    private ProgressBar progressBar;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ease_activity_show_file);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final FileMessageBody messageBody = getIntent().getParcelableExtra("msgbody");
        file = new File(messageBody.getLocalUrl());
        //set head map
        final Map<String, String> maps = new HashMap<String, String>();
        if (!TextUtils.isEmpty(messageBody.getSecret())) {
            maps.put("share-secret", messageBody.getSecret());
        }

        //download file
        ChatClient.getInstance().chatManager().downloadFile(messageBody.getRemoteUrl(), messageBody.getLocalUrl(), maps,
                new CallBack() {

                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                FileUtils.openFile(file, EaseShowNormalFileActivity.this);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onProgress(final int progress, String status) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressBar.setProgress(progress);
                            }
                        });
                    }

                    @Override
                    public void onError(int error, final String msg) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (file != null && file.exists() && file.isFile())
                                    file.delete();
                                String str4 = getResources().getString(R.string.Failed_to_download_file);
                                Toast.makeText(EaseShowNormalFileActivity.this, str4 + msg, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }
                });

    }
}
