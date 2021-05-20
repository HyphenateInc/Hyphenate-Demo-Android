package io.agora.easeui.widget.chatrow;

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
import io.agora.chat.ChatClient;
import io.agora.chat.FileMessageBody;
import io.agora.chat.ImageMessageBody;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatMessage.ChatType;
import io.agora.chatdemo.R;
import io.agora.easeui.model.EaseImageCache;
import io.agora.easeui.ui.EaseShowImageActivity;
import io.agora.easeui.utils.EaseCommonUtils;
import io.agora.easeui.utils.EaseFileUtils;
import io.agora.easeui.utils.EaseImageUtils;
import io.agora.util.DensityUtil;
import java.io.File;

public class EaseChatRowImage extends EaseChatRowFile{

    protected ImageView imageView;
    protected ImageMessageBody imgBody;

    protected static final int THUMBNAIL_SIZE = 180;

    protected int toScaleMaxSize = 110;
    protected int toScaleMinSize = 50;

    public EaseChatRowImage(Context context, ChatMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
        toScaleMaxSize = DensityUtil.dip2px(context, toScaleMaxSize);
        toScaleMinSize = DensityUtil.dip2px(context, toScaleMinSize);
    }

    @Override protected boolean overrideBaseLayout() {
        return false;
    }

    @Override protected int onGetLayoutId() {
        return message.direct() == ChatMessage.Direct.RECEIVE ? R.layout.ease_row_received_image : R.layout.ease_row_sent_image;
    }

    @Override
    protected void onFindViewById() {
        percentageView = (TextView) findViewById(R.id.percentage);
        imageView = (ImageView) findViewById(R.id.image);
    }

    @Override
    protected void onSetUpView() {
        imgBody = (ImageMessageBody) message.getBody();
        // received messages
        if (message.direct() == ChatMessage.Direct.RECEIVE) {
            if (imgBody.thumbnailDownloadStatus() == FileMessageBody.EMDownloadStatus.DOWNLOADING ||
                    imgBody.thumbnailDownloadStatus() == FileMessageBody.EMDownloadStatus.PENDING) {
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

        Uri localUri = imgBody.getLocalUri();
        // if the file already exist on local device, then load it
        if (EaseFileUtils.isFileExistByUri(context, localUri)) {
            intent.putExtra("uri", localUri);
        }
        // otherwise download it from the server
        else {
            intent.putExtra("messageId", message.getMsgId());
        }

        // send message read ack
        if (message != null && message.direct() == ChatMessage.Direct.RECEIVE &&
                !message.isAcked() && message.getChatType() == ChatType.Chat) {
            try {
                ChatClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
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
        EaseImageUtils.showImage(context, imageView, message);
    }

    /**
     * used before setImageBitmap()
     * @param bmp        bitmap
     * @param imageView  image view
     */
    private void scaleImageView(Bitmap bmp, ImageView imageView) {
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
            } else {
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
