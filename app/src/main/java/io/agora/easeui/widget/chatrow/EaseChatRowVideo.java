package io.agora.easeui.widget.chatrow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import io.agora.chat.ChatClient;
import io.agora.chat.FileMessageBody;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatMessage.ChatType;
import io.agora.chat.VideoMessageBody;
import io.agora.chatdemo.R;
import io.agora.easeui.model.EaseImageCache;
import io.agora.easeui.ui.EaseShowVideoActivity;
import io.agora.easeui.utils.EaseCommonUtils;
import io.agora.util.DateUtils;
import io.agora.util.EMLog;
import io.agora.util.ImageUtils;
import io.agora.util.TextFormater;
import java.io.File;

public class EaseChatRowVideo extends EaseChatRowFile {

	private ImageView imageView;
    private TextView sizeView;
    private TextView timeLengthView;
    private ImageView playView;

    public EaseChatRowVideo(Context context, ChatMessage message, int position, BaseAdapter adapter) {
		super(context, message, position, adapter);
	}

    @Override protected boolean overrideBaseLayout() {
        return false;
    }


    @Override protected int onGetLayoutId() {
        return message.direct() == ChatMessage.Direct.RECEIVE ?
                R.layout.ease_row_received_video : R.layout.ease_row_sent_video;
    }

    @Override
	protected void onFindViewById() {
	    imageView = ((ImageView) findViewById(R.id.chatting_content_iv));
        sizeView = (TextView) findViewById(R.id.chatting_size_iv);
        timeLengthView = (TextView) findViewById(R.id.chatting_length_iv);
        playView = (ImageView) findViewById(R.id.chatting_status_btn);
        percentageView = (TextView) findViewById(R.id.percentage);
	}

	@Override
	protected void onSetUpView() {
	    VideoMessageBody videoBody = (VideoMessageBody) message.getBody();
        String localThumb = videoBody.getLocalThumb();

        if (localThumb != null) {

            showVideoThumbView(localThumb, imageView, videoBody.getThumbnailUrl(), message);
        }
        if (videoBody.getDuration() > 0) {
            String time = DateUtils.toTime(videoBody.getDuration());
            timeLengthView.setText(time);
        }

        if (message.direct() == ChatMessage.Direct.RECEIVE) {
            if (videoBody.getVideoFileLength() > 0) {
                String size = TextFormater.getDataSize(videoBody.getVideoFileLength());
                sizeView.setText(size);
            }
        } else {
            if (videoBody.getLocalUrl() != null && new File(videoBody.getLocalUrl()).exists()) {
                String size = TextFormater.getDataSize(new File(videoBody.getLocalUrl()).length());
                sizeView.setText(size);
            }
        }

        EMLog.d(TAG,  "video thumbnailStatus:" + videoBody.thumbnailDownloadStatus());
        if (message.direct() == ChatMessage.Direct.RECEIVE) {
            if (videoBody.thumbnailDownloadStatus() == FileMessageBody.EMDownloadStatus.DOWNLOADING ||
                    videoBody.thumbnailDownloadStatus() == FileMessageBody.EMDownloadStatus.PENDING) {
                imageView.setImageResource(R.drawable.ease_default_image);
                setMessageReceiveCallback();
            } else {
                // System.err.println("!!!! not back receive, show image directly");
                imageView.setImageResource(R.drawable.ease_default_image);
                if (localThumb != null) {
                    showVideoThumbView(localThumb, imageView, videoBody.getThumbnailUrl(), message);
                }

            }

            return;
        }
        //handle sending message
        handleSendMessage();
	}
	
	@Override
	protected void onBubbleClick() {
        EMLog.d(TAG, "video view is on click");
        Intent intent = new Intent(context, EaseShowVideoActivity.class);
        intent.putExtra("msg", message);
        if (message != null && message.direct() == ChatMessage.Direct.RECEIVE && !message.isAcked()
                && message.getChatType() == ChatType.Chat) {
            try {
                ChatClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        context.startActivity(intent);
	}
	
	/**
     * show video thumbnails
     * 
     * @param localThumb
     *            local path for thumbnail
     * @param iv
     * @param thumbnailUrl
     *            Url on server for thumbnails
     * @param message
     */
    private void showVideoThumbView(final String localThumb, final ImageView iv, String thumbnailUrl, final ChatMessage message) {
        // first check if the thumbnail image already loaded into cache
        Bitmap bitmap = EaseImageCache.getInstance().get(localThumb);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);

        } else {
            new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Void... params) {
                    if (new File(localThumb).exists()) {
                        return ImageUtils.decodeScaleImage(localThumb, 160, 160);
                    } else {
                        return null;
                    }
                }
                
                @Override
                protected void onPostExecute(Bitmap result) {
                    super.onPostExecute(result);
                    if (result != null) {
                        EaseImageCache.getInstance().put(localThumb, result);
                        iv.setImageBitmap(result);

                    } else {
                        if (message.status() == ChatMessage.Status.FAIL) {
                            if (EaseCommonUtils.isNetWorkConnected(context)) {
                                ChatClient.getInstance().chatManager().downloadThumbnail(message);
                            }
                        }

                    }
                }
            }.execute();
        }
        
    }

}
