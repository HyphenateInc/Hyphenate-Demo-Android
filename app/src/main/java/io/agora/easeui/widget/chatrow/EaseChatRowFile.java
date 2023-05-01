package io.agora.easeui.widget.chatrow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;
import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatMessage.ChatType;
import io.agora.chat.NormalFileMessageBody;
import io.agora.chatdemo.R;
import io.agora.easeui.ui.EaseShowNormalFileActivity;
import io.agora.easeui.utils.EaseCompat;
import io.agora.easeui.utils.EaseFileUtils;
import io.agora.exceptions.ChatException;
import io.agora.util.FileUtils;
import io.agora.util.TextFormater;
import java.io.File;

public class EaseChatRowFile extends EaseChatRow {

    protected TextView fileNameView;
	protected TextView fileSizeView;
    protected TextView fileStateView;
    
    protected CallBack sendfileCallBack;
    
    protected boolean isNotifyProcessed;
    private NormalFileMessageBody fileMessageBody;

    public EaseChatRowFile(Context context, ChatMessage message, int position, BaseAdapter adapter) {
		super(context, message, position, adapter);
	}

    /**
     * The default child layout only needs to write ui in the bubble,
     * If all the layout you want to write their own, return true.
     */
    @Override protected boolean overrideBaseLayout() {
        return false;
    }

    /**
     * get the layout res id
     */
    @Override protected int onGetLayoutId() {
        return message.direct() == ChatMessage.Direct.RECEIVE ?
                R.layout.ease_row_received_file : R.layout.ease_row_sent_file;
    }


	@Override
	protected void onFindViewById() {
	    fileNameView = (TextView) findViewById(R.id.tv_file_name);
        fileSizeView = (TextView) findViewById(R.id.tv_file_size);
        fileStateView = (TextView) findViewById(R.id.tv_file_state);
        percentageView = (TextView) findViewById(R.id.percentage);
	}


	@Override
	protected void onSetUpView() {
	    fileMessageBody = (NormalFileMessageBody) message.getBody();
        String filePath = fileMessageBody.getLocalUrl();
        fileNameView.setText(fileMessageBody.getFileName());
        fileSizeView.setText(TextFormater.getDataSize(fileMessageBody.getFileSize()));
        if (message.direct() == ChatMessage.Direct.RECEIVE) {
            File file = new File(filePath);
            if (file != null && file.exists()) {
                fileStateView.setText(R.string.Have_downloaded);
            } else {
                fileStateView.setText(R.string.Did_not_download);
            }
            return;
        }

        // until here, to sending message
        handleSendMessage();
	}

	/**
	 * handle sending message
	 */
    protected void handleSendMessage() {
        setMessageSendCallback();
        switch (message.status()) {
        case SUCCESS:
            progressBar.setVisibility(View.INVISIBLE);
            if(percentageView != null)
                percentageView.setVisibility(View.INVISIBLE);
            statusView.setVisibility(View.INVISIBLE);
            break;
        case FAIL:
            progressBar.setVisibility(View.INVISIBLE);
            if(percentageView != null)
                percentageView.setVisibility(View.INVISIBLE);
            statusView.setVisibility(View.VISIBLE);
            break;
        case INPROGRESS:
            progressBar.setVisibility(View.VISIBLE);
            if(percentageView != null){
                percentageView.setVisibility(View.VISIBLE);
                percentageView.setText(message.progress() + "%");
            }
            statusView.setVisibility(View.INVISIBLE);
            break;
        default:
            progressBar.setVisibility(View.INVISIBLE);
            if(percentageView != null)
                percentageView.setVisibility(View.INVISIBLE);
            statusView.setVisibility(View.VISIBLE);
            break;
        }
    }
	

	@Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onBubbleClick() {
        Uri localUri = fileMessageBody.getLocalUri();
        if (EaseFileUtils.isFileExistByUri(context, localUri)) {
            // open files if it exist
            EaseCompat.openFile(context, localUri);
        } else {
            // download the file
            context.startActivity(new Intent(context, EaseShowNormalFileActivity.class).putExtra("msg", message));
        }
        if (message.direct() == ChatMessage.Direct.RECEIVE && !message.isAcked() && message.getChatType() == ChatType.Chat) {
            try {
                ChatClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (ChatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }

}
