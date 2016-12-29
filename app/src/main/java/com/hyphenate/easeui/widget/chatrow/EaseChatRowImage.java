package com.hyphenate.easeui.widget.chatrow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.model.EaseImageCache;
import com.hyphenate.easeui.ui.EaseShowImageActivity;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseImageUtils;
import com.hyphenate.util.DensityUtil;
import java.io.File;

public class EaseChatRowImage extends EaseChatRowFile{

    protected ImageView imageView;
    protected EMImageMessageBody imgBody;


    protected static final int THUMBNAIL_SIZE = 180;

    protected int toScaleMaxSize = 110;
    protected int toScaleMinSize = 50;

    public EaseChatRowImage(Context context, EMMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
        toScaleMaxSize = DensityUtil.dip2px(context, toScaleMaxSize);
        toScaleMinSize = DensityUtil.dip2px(context, toScaleMinSize);
    }

    @Override protected boolean overrideBaseLayout() {
        return false;
    }

    @Override protected int onGetLayoutId() {
        return message.direct() == EMMessage.Direct.RECEIVE ? R.layout.ease_row_received_image : R.layout.ease_row_sent_image;
    }

    @Override
    protected void onFindViewById() {
        percentageView = (TextView) findViewById(R.id.percentage);
        imageView = (ImageView) findViewById(R.id.image);
    }

    
    @Override
    protected void onSetUpView() {
        imgBody = (EMImageMessageBody) message.getBody();
        // received messages
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            if (imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
                    imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING) {
                imageView.setImageResource(R.drawable.ease_default_image);
                setMessageReceiveCallback();
            } else {
                progressBar.setVisibility(View.GONE);
                percentageView.setVisibility(View.GONE);
                showImageView();
            }
        } else {
            showImageView();
            handleSendMessage();
        }

    }
    
    @Override
    protected void onUpdateView() {
        super.onUpdateView();
    }
    
    @Override
    protected void onBubbleClick() {
        Intent intent = new Intent(context, EaseShowImageActivity.class);
        File file = new File(imgBody.getLocalUrl());
        if (file.exists()) {
            Uri uri = Uri.fromFile(file);
            intent.putExtra("uri", uri);
        } else {
            // The local full size pic does not exist yet.
            // ShowBigImage needs to download it from the server
            // first
            intent.putExtra("secret", imgBody.getSecret());
            intent.putExtra("remotepath", imgBody.getRemoteUrl());
            intent.putExtra("localUrl", imgBody.getLocalUrl());
        }
        if (message != null && message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked()
                && message.getChatType() == ChatType.Chat) {
            try {
                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        context.startActivity(intent);
    }
    
    /**
     * load image into image view
     * 
     */
    private void showImageView() {
        imageView.setImageResource(R.drawable.ease_default_image);

        String filePath = imgBody.getLocalUrl();
        String thumbnailPath = EaseImageUtils.getThumbnailImagePath(filePath);
        if(message.direct() == EMMessage.Direct.RECEIVE){
            filePath = imgBody.thumbnailLocalPath();
            thumbnailPath = filePath;
        }

        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = EaseImageCache.getInstance().get(thumbnailPath);
        if (bitmap != null) {
            scaleImageView(bitmap, imageView);
            // thumbnail image is already loaded, reuse the drawable
            imageView.setImageBitmap(bitmap);
        } else {
            final String finalFilePath = filePath;
            final String finalThumbnailPath = thumbnailPath;
            new AsyncTask<Object, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Object... args) {
                    File file = new File(finalThumbnailPath);
                    if (file.exists()) {
                        return EaseImageUtils.decodeScaleImage(finalThumbnailPath, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
                    } else if (new File(finalFilePath).exists()) {
                        return EaseImageUtils.decodeScaleImage(finalFilePath, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
                    }
                    return null;

                }

                protected void onPostExecute(Bitmap bmp) {
                    if (bmp != null) {
                        scaleImageView(bmp, imageView);
                        //set bitmap to scaled image view
                        imageView.setImageBitmap(bmp);
                        EaseImageCache.getInstance().put(finalThumbnailPath, bmp);
                    } else {
                        if (message.status() == EMMessage.Status.FAIL) {
                            if (EaseCommonUtils.isNetWorkConnected(context)) {
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        EMClient.getInstance().chatManager().downloadThumbnail(message);
                                    }
                                }).start();
                            }
                        }

                    }
                }
            }.execute();

        }
    }

    /**
     * used before setImageBitmap()
     * @param bmp
     * @param imageView
     */
    private void scaleImageView(Bitmap bmp, ImageView imageView){
        int originWidth = imgBody.getWidth();
        int originHeight = imgBody.getHeight();

        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();
        if (originWidth > toScaleMaxSize || originHeight > toScaleMaxSize) {

            float scale = originWidth < originHeight ? (float) toScaleMaxSize
                    /bmpWidth : (float) toScaleMaxSize /bmpHeight;
            int scaledWidth = (int) (bmpWidth * scale);
            int scaledHeight = (int) (bmpHeight * scale);
            setLayoutParams(imageView, lp, scaledWidth, scaledHeight);
        } else {
            if (originWidth < toScaleMinSize || originHeight < toScaleMinSize) {
                float scale = originWidth < originHeight ? (float) toScaleMinSize
                        /bmpWidth : (float) toScaleMinSize /bmpHeight;
                int scaledWidth = (int) (bmpWidth * scale);
                int scaledHeight = (int) (bmpHeight * scale);
                setLayoutParams(imageView, lp, scaledWidth, scaledHeight);
            }else{
                setLayoutParams(imageView, lp, bmpWidth, bmpHeight);
            }
        }
    }

    private void setLayoutParams(ImageView imageView, ViewGroup.LayoutParams lp, int scaledWidth,
            int scaledHeight) {
        if(lp.width != scaledWidth || lp.height != scaledHeight) {
            lp.width = scaledWidth;
            lp.height = scaledHeight;
            //imageView.setLayoutParams(lp);
            onUpdateView();
        }
    }
}
