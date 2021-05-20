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
package io.agora.easeui.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ImageMessageBody;
import io.agora.chatdemo.R;
import io.agora.easeui.utils.EaseFileUtils;
import io.agora.easeui.widget.photoview.EasePhotoView;
import io.agora.easeui.widget.photoview.PhotoViewAttacher;
import io.agora.util.EMLog;

/**
 * download and show original photoView
 * 
 */
public class EaseShowImageActivity extends Activity {
	private static final String TAG = "ShowBigImage";
	private ProgressDialog pd;
	private EasePhotoView photoView;
	private int default_res;
	private boolean isDownloaded;
	private ProgressBar loadLocalPb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.ease_activity_show_big_image);
		super.onCreate(savedInstanceState);

		photoView = (EasePhotoView) findViewById(R.id.image);
		loadLocalPb = (ProgressBar) findViewById(R.id.pb_load_local);
		default_res = getIntent().getIntExtra("default_image", R.drawable.ease_default_image);
		Uri uri = getIntent().getParcelableExtra("uri");
		String msgId = getIntent().getExtras().getString("messageId");
		EMLog.d(TAG, "show big photoView uri:" + uri + " messageId:" + msgId);

		//show the photoView if it exist in local path
		if (EaseFileUtils.isFileExistByUri(this, uri)) {
			EMLog.d(TAG, "showbigimage file exists. directly show it");
			Glide.with(this).load(uri).into(photoView);
		} else if (msgId != null) { //download photoView from server
			EMLog.d(TAG, "download remote photoView");
			downloadImage(msgId);
		} else {
			photoView.setImageResource(default_res);
		}

		photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
			@Override public void onPhotoTap(View view, float x, float y) {
				finish();
			}
		});
	}
	
	/**
	 * download photoView
	 * 
	 * @param msgId
	 */
	@SuppressLint("NewApi")
	private void downloadImage(final String msgId) {
		String str1 = getResources().getString(R.string.Download_the_pictures);
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage(str1);
		pd.show();
		final ChatMessage msg = ChatClient.getInstance().chatManager().getMessage(msgId);
		if(msg == null) {
		    EMLog.e(TAG, "msgId: "+msgId +" not find local message!");
		    return;
		}
		final CallBack callback = new CallBack() {
			public void onSuccess() {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!isFinishing() && !isDestroyed()) {
							if (pd != null) {
								pd.dismiss();
							}
							isDownloaded = true;
							Uri localUrlUri = ((ImageMessageBody) msg.getBody()).getLocalUri();
							Glide.with(EaseShowImageActivity.this)
									.load(localUrlUri)
									.apply(new RequestOptions().error(default_res))
									.into(photoView);
						}
					}
				});
			}

			public void onError(int error, String msg) {
				EMLog.e(TAG, "offline file transfer error:" + msg);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (EaseShowImageActivity.this.isFinishing() || EaseShowImageActivity.this.isDestroyed()) {
						    return;
						}
                        photoView.setImageResource(default_res);
						if (pd != null) {
							pd.dismiss();
						}
					}
				});
			}

			public void onProgress(final int progress, String status) {
				EMLog.d(TAG, "Progress: " + progress);
				final String str2 = getResources().getString(R.string.Download_the_pictures_new);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
                        if (EaseShowImageActivity.this.isFinishing() || EaseShowImageActivity.this.isDestroyed()) {
                            return;
                        }
						pd.setMessage(str2 + progress + "%");
					}
				});
			}
		};

		msg.setMessageStatusCallback(callback);

		ChatClient.getInstance().chatManager().downloadAttachment(msg);
	}

	@Override
	public void onBackPressed() {
		if (isDownloaded)
			setResult(RESULT_OK);
		finish();
	}
}
