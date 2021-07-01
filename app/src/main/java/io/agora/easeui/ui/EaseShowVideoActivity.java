package io.agora.easeui.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.VideoMessageBody;
import io.agora.chatdemo.R;
import io.agora.easeui.utils.EaseFileUtils;
import io.agora.util.EMLog;
import io.agora.Error;

/**
 * show the video
 * 
 */
public class EaseShowVideoActivity extends FragmentActivity{
	private static final String TAG = "ShowVideoActivity";
	
	private RelativeLayout loadingLayout;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.ease_showvideo_activity);
		loadingLayout = (RelativeLayout) findViewById(R.id.loading_layout);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		final ChatMessage message = getIntent().getParcelableExtra("msg");
		if(message == null) {
		    Toast.makeText(EaseShowVideoActivity.this, "Video message is not exist!", Toast.LENGTH_SHORT).show();
		    finish();
		    return;
		}
		if (!(message.getBody() instanceof VideoMessageBody)) {
			Toast.makeText(EaseShowVideoActivity.this, "Unsupported message body", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		VideoMessageBody messageBody = (VideoMessageBody)message.getBody();
		Uri localUri = messageBody.getLocalUri();
		EMLog.d(TAG, "show video view file:" + localUri);

		//check Uri read permission
		EaseFileUtils.takePersistableUriPermission(this, localUri);
		if(EaseFileUtils.isFileExistByUri(this, localUri)) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(localUri,
					"video/mp4");
			startActivity(intent);
			finish();
		}else {
			EMLog.d(TAG, "download remote video file");
			downloadVideo(message);
		}
	}
	
	/**
	 * show local video
	 * @param localPath -- local path of the video file
	 */
	private void showLocalVideo(Uri localPath){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(localPath,
				"video/mp4");
		startActivity(intent);
		finish();
	}
	
	
	

	/**
	 * download video file
	 */
	private void downloadVideo(final ChatMessage message) {

		loadingLayout.setVisibility(View.VISIBLE);
		
		CallBack callback = new CallBack() {

			@Override
			public void onSuccess() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						loadingLayout.setVisibility(View.GONE);
						progressBar.setProgress(0);
						showLocalVideo(((VideoMessageBody)message.getBody()).getLocalUri());
					}
				});
			}

			@Override
			public void onProgress(final int progress,String status) {
				Log.d("ease", "video progress:" + progress);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						progressBar.setProgress(progress);
					}
				});

			}

			@Override
			public void onError(int error, String msg) {
				Log.e("###", "offline file transfer error:" + msg);
				EaseFileUtils.deleteFile(EaseShowVideoActivity.this, ((VideoMessageBody) message.getBody()).getLocalUri());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (error == Error.FILE_NOT_FOUND) {
							Toast.makeText(getApplicationContext(), R.string.Video_expired, Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		};

		message.setMessageStatusCallback(callback);

		ChatClient.getInstance().chatManager().downloadAttachment(message);
	}

	@Override
	public void onBackPressed() {
		finish();
	}
 

}
