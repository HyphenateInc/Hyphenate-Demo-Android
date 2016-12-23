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
package com.hyphenate.easeui.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.model.EaseImageCache;
import com.hyphenate.easeui.utils.EaseLoadLocalBigImgTask;
import com.hyphenate.easeui.widget.photoview.EasePhotoView;
import com.hyphenate.easeui.widget.photoview.PhotoViewAttacher;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.ImageUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * download and show original photoView
 * 
 */
public class EaseShowImageActivity extends Activity {
	private static final String TAG = "ShowBigImage";
	private ProgressDialog pd;
	private EasePhotoView photoView;
	private int default_res;
	private String localFilePath;
	private Bitmap bitmap;
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
		String remotepath = getIntent().getExtras().getString("remotepath");
		localFilePath = getIntent().getExtras().getString("localUrl");
		String secret = getIntent().getExtras().getString("secret");
		EMLog.d(TAG, "show big photoView uri:" + uri + " remotepath:" + remotepath);

		//show the photoView if it exist in local path
		if (uri != null && new File(uri.getPath()).exists()) {
			EMLog.d(TAG, "showbigimage file exists. directly show it");
			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			// int screenWidth = metrics.widthPixels;
			// int screenHeight =metrics.heightPixels;
			bitmap = EaseImageCache.getInstance().get(uri.getPath());
			if (bitmap == null) {
				EaseLoadLocalBigImgTask task = new EaseLoadLocalBigImgTask(this, uri.getPath(),
						photoView, loadLocalPb, ImageUtils.SCALE_IMAGE_WIDTH,
						ImageUtils.SCALE_IMAGE_HEIGHT);
				if (android.os.Build.VERSION.SDK_INT > 10) {
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} else {
					task.execute();
				}
			} else {
				photoView.setImageBitmap(bitmap);
			}
		} else if (remotepath != null) { //download photoView from server
			EMLog.d(TAG, "download remote photoView");
			Map<String, String> maps = new HashMap<String, String>();
			if (!TextUtils.isEmpty(secret)) {
				maps.put("share-secret", secret);
			}
			downloadImage(remotepath, maps);
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
	 * @param remoteFilePath
	 */
	@SuppressLint("NewApi")
	private void downloadImage(final String remoteFilePath, final Map<String, String> headers) {
		String str1 = getResources().getString(R.string.Download_the_pictures);
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setCanceledOnTouchOutside(false);
		pd.setMessage(str1);
		pd.show();
		File temp = new File(localFilePath);
		final String tempPath = temp.getParent() + "/temp_" + temp.getName();
		final EMCallBack callback = new EMCallBack() {
			public void onSuccess() {

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
                        new File(tempPath).renameTo(new File(localFilePath));

                        DisplayMetrics metrics = new DisplayMetrics();
						getWindowManager().getDefaultDisplay().getMetrics(metrics);
						int screenWidth = metrics.widthPixels;
						int screenHeight = metrics.heightPixels;

						bitmap = ImageUtils.decodeScaleImage(localFilePath, screenWidth, screenHeight);
						if (bitmap == null) {
							photoView.setImageResource(default_res);
						} else {
							photoView.setImageBitmap(bitmap);
							EaseImageCache.getInstance().put(localFilePath, bitmap);
							isDownloaded = true;
						}
						if (EaseShowImageActivity.this.isFinishing() || EaseShowImageActivity.this.isDestroyed()) {
						    return;
						}
						if (pd != null) {
							pd.dismiss();
						}
					}
				});
			}

			public void onError(int error, String msg) {
				EMLog.e(TAG, "offline file transfer error:" + msg);
				File file = new File(tempPath);
				if (file.exists()&&file.isFile()) {
					file.delete();
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (EaseShowImageActivity.this.isFinishing() || EaseShowImageActivity.this.isDestroyed()) {
						    return;
						}
                        photoView.setImageResource(default_res);
                        pd.dismiss();
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

	    EMClient.getInstance().chatManager().downloadFile(remoteFilePath, tempPath, headers, callback);

	}

	@Override
	public void onBackPressed() {
		if (isDownloaded)
			setResult(RESULT_OK);
		finish();
	}
}
