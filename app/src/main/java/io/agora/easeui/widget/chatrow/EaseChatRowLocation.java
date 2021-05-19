package io.agora.easeui.widget.chatrow;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;
import io.agora.chat.ChatClient;
import io.agora.chat.LocationMessageBody;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatMessage.ChatType;
import io.agora.chatdemo.R;
import io.agora.easeui.ui.EaseMapActivity;
import io.agora.exceptions.ChatException;

public class EaseChatRowLocation extends EaseChatRow {

    private TextView locationView;
    private LocationMessageBody locBody;

	public EaseChatRowLocation(Context context, ChatMessage message, int position, BaseAdapter adapter) {
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
                R.layout.ease_row_received_location : R.layout.ease_row_sent_location;
    }


    @Override
    protected void onFindViewById() {
    	locationView = (TextView) findViewById(R.id.tv_location);
    }


    @Override
    protected void onSetUpView() {
		locBody = (LocationMessageBody) message.getBody();
		locationView.setText(locBody.getAddress());

		// handle sending message
		if (message.direct() == ChatMessage.Direct.SEND) {
		    setMessageSendCallback();
            switch (message.status()) {
            case CREATE: 
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                break;
            case SUCCESS:
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.GONE);
                break;
            case FAIL:
                progressBar.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                progressBar.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
                break;
            default:
               break;
            }
        }else{
            if(!message.isAcked() && message.getChatType() == ChatType.Chat){
                try {
                    ChatClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
                } catch (ChatException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    protected void onUpdateView() {
        adapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onBubbleClick() {
        Intent intent = new Intent(context, EaseMapActivity.class);
        intent.putExtra("latitude", locBody.getLatitude());
        intent.putExtra("longitude", locBody.getLongitude());
        intent.putExtra("address", locBody.getAddress());
        context.startActivity(intent);
    }


}
