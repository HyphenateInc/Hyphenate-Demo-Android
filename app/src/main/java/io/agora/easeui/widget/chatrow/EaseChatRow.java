package io.agora.easeui.widget.chatrow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import io.agora.CallBack;
import io.agora.Error;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ChatMessage.Direct;
import io.agora.chatdemo.R;
import io.agora.easeui.model.styles.EaseMessageListItemStyle;
import io.agora.easeui.utils.EaseUserUtils;
import io.agora.easeui.widget.EaseMessageListView;
import io.agora.util.DateUtils;
import java.util.Date;

public abstract class EaseChatRow extends LinearLayout {
    protected static final String TAG = EaseChatRow.class.getSimpleName();

    protected LayoutInflater inflater;
    protected Context context;
    protected ChatMessage message;
    protected int position;

    protected TextView timeStampView;
    protected ImageView userAvatarView;
    protected ViewGroup bubbleLayout;
    protected TextView usernickView;

    protected TextView percentageView;
    protected ProgressBar progressBar;
    protected ImageView statusView;

    protected TextView ackedView;
    protected TextView deliveredView;

    protected CallBack messageSendCallback;
    protected CallBack messageReceiveCallback;

    protected EaseMessageListView.MessageListItemClicksListener itemClickListener;
    protected EaseMessageListItemStyle style;

    protected BaseAdapter adapter;
    protected EaseMessageListItemStyle itemStyle;

    protected Handler handler = new Handler(Looper.getMainLooper());

    public EaseChatRow(Context context, ChatMessage message, int position, BaseAdapter adapter) {
        super(context);
        this.context = context;
        this.message = message;
        this.position = position;
        this.adapter = adapter;
        inflater = LayoutInflater.from(context);

        initView();
    }

    private void initView() {
        if(overrideBaseLayout()){
            inflater.inflate(onGetLayoutId(), this);
        }else{
            inflater.inflate(message.direct() == ChatMessage.Direct.RECEIVE ?
                    R.layout.ease_row_chat_received : R.layout.ease_row_chat_sent, this);
            ViewGroup bubbleLayoutContainer = (ViewGroup) findViewById(R.id.bubble_container);
            inflater.inflate(onGetLayoutId(), bubbleLayoutContainer);
        }

        timeStampView = (TextView) findViewById(R.id.timestamp);
        userAvatarView = (ImageView) findViewById(R.id.iv_userhead);

        usernickView = (TextView) findViewById(R.id.tv_userid);
        bubbleLayout = (ViewGroup) findViewById(R.id.bubble);
        if(bubbleLayout != null){
            bubbleLayout.setBackgroundResource(
                    message.direct() == ChatMessage.Direct.RECEIVE ? R.drawable.ease_msg_bubble_other : R.drawable.ease_msg_bubble_me);
        }

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        statusView = (ImageView) findViewById(R.id.msg_status);
        ackedView = (TextView) findViewById(R.id.tv_ack);
        deliveredView = (TextView) findViewById(R.id.tv_delivered);

        onFindViewById();
    }

    /**
     * set view property
     * @param position
     * @param itemClickListener
     * @param itemStyle
     */
    public void setUpView(int position,
            EaseMessageListView.MessageListItemClicksListener itemClickListener,
            EaseMessageListItemStyle itemStyle) {
        this.position = position;
        this.message = (ChatMessage) adapter.getItem(position);
        this.itemClickListener = itemClickListener;
        this.itemStyle = itemStyle;


        setUpBaseView();
        onSetUpView();
        setClickListener();
    }

    private void setUpBaseView() {
    	// set nickname, avatar and background of bubble
        TextView timestamp = (TextView) findViewById(R.id.timestamp);
        if (timestamp != null) {
            if (position == 0) {
                timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                timestamp.setVisibility(View.VISIBLE);
            } else {
            	// show time stamp if interval with last message is > 30 seconds
                ChatMessage prevMessage = (ChatMessage) adapter.getItem(position - 1);
                if (prevMessage != null && DateUtils.isCloseEnough(message.getMsgTime(), prevMessage.getMsgTime())) {
                    timestamp.setVisibility(View.GONE);
                } else {
                    timestamp.setText(DateUtils.getTimestampString(new Date(message.getMsgTime())));
                    timestamp.setVisibility(View.VISIBLE);
                }
            }
        }
        //set nickname and avatar
        if(message.direct() == Direct.SEND){
            EaseUserUtils.setUserAvatar(context, ChatClient.getInstance().getCurrentUser(), userAvatarView);
        }else{
            EaseUserUtils.setUserAvatar(context, message.getFrom(), userAvatarView);
            EaseUserUtils.setUserNick(message.getFrom(), usernickView);
        }
        
        if(deliveredView != null){
            if (message.isDelivered()) {
                deliveredView.setVisibility(View.VISIBLE);
            } else {
                deliveredView.setVisibility(View.INVISIBLE);
            }
        }
        
        if(ackedView != null){
            if (message.isAcked()) {
                if (deliveredView != null) {
                    deliveredView.setVisibility(View.INVISIBLE);
                }
                ackedView.setVisibility(View.VISIBLE);
            } else {
                ackedView.setVisibility(View.INVISIBLE);
            }
        }

        if (usernickView != null) {
            if (itemStyle.isShowUserNick()){
                usernickView.setVisibility(View.VISIBLE);
            } else {
                usernickView.setVisibility(View.GONE);
            }
        }



        //if (adapter instanceof EaseMessageListAdapter) {
        //    if (((EaseMessageListAdapter) adapter).isShowAvatar())
        //        userAvatarView.setVisibility(View.VISIBLE);
        //    else
        //        userAvatarView.setVisibility(View.GONE);
        //    if (usernickView != null) {
        //        if (((EaseMessageListAdapter) adapter).isShowUserNick())
        //            usernickView.setVisibility(View.VISIBLE);
        //        else
        //            usernickView.setVisibility(View.GONE);
        //    }
        //    if (message.direct() == Direct.SEND) {
        //        if (((EaseMessageListAdapter) adapter).getMyBubbleBg() != null) {
        //            bubbleLayout.setBackgroundDrawable(((EaseMessageListAdapter) adapter).getMyBubbleBg());
        //        }
        //    } else if (message.direct() == Direct.RECEIVE) {
        //        if (((EaseMessageListAdapter) adapter).getOtherBuddleBg() != null) {
        //            bubbleLayout.setBackgroundDrawable(((EaseMessageListAdapter) adapter).getOtherBuddleBg());
        //        }
        //    }
        //}
    }

    /**
     * set callback for sending message
     */
    protected void setMessageSendCallback(){
        if(messageSendCallback == null){
            messageSendCallback = new CallBack() {
                
                @Override
                public void onSuccess() {
                    updateView(0);
                }
                
                @Override
                public void onProgress(final int progress, String status) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(percentageView != null)
                                percentageView.setText(progress + "%");

                        }
                    });
                }
                
                @Override
                public void onError(int code, String error) {
                    updateView(code);
                }
            };
        }
        message.setMessageStatusCallback(messageSendCallback);
    }
    
    /**
     * set callback for receiving message
     */
    protected void setMessageReceiveCallback(){
        if(messageReceiveCallback == null){
            messageReceiveCallback = new CallBack() {
                
                @Override
                public void onSuccess() {
                    updateView(0);
                }
                
                @Override
                public void onProgress(final int progress, String status) {
                    handler.post(new Runnable() {
                        public void run() {
                            if(percentageView != null){
                                percentageView.setText(progress + "%");
                            }
                        }
                    });
                }
                
                @Override
                public void onError(int code, String error) {
                    updateView(code);
                }
            };
        }
        message.setMessageStatusCallback(messageReceiveCallback);
    }
    
    
    private void setClickListener() {
        if(bubbleLayout != null){
            bubbleLayout.setOnClickListener(new OnClickListener() {
    
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null){
                        if(!itemClickListener.onBubbleClick(message)){
                        	// if listener return false, we call default handling
                            onBubbleClick();
                        }
                    }
                }
            });
    
            bubbleLayout.setOnLongClickListener(new OnLongClickListener() {
    
                @Override
                public boolean onLongClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onBubbleLongClick(message);
                    }
                    return true;
                }
            });
        }

        if (statusView != null) {
            statusView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onResendClick(message);
                    }
                }
            });
        }

        if(userAvatarView != null){
            userAvatarView.setOnClickListener(new OnClickListener() {
    
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        if (message.direct() == Direct.SEND) {
                            itemClickListener.onUserAvatarClick(ChatClient.getInstance().getCurrentUser());
                        } else {
                            itemClickListener.onUserAvatarClick(message.getFrom());
                        }
                    }
                }
            });
            userAvatarView.setOnLongClickListener(new OnLongClickListener() {
                
                @Override
                public boolean onLongClick(View v) {
                    if(itemClickListener != null){
                        if (message.direct() == Direct.SEND) {
                            itemClickListener.onUserAvatarLongClick(ChatClient.getInstance().getCurrentUser());
                        } else {
                            itemClickListener.onUserAvatarLongClick(message.getFrom());
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }


    protected void updateView(final int errorCode) {
        handler.post(new Runnable() {
            public void run() {
                if (message.status() == ChatMessage.Status.FAIL) {
                    if (errorCode == Error.MESSAGE_INCLUDE_ILLEGAL_CONTENT) {
                        Toast.makeText(context,context.getString(R.string.send_fail) + context.getString(R.string.error_send_invalid_content), Toast.LENGTH_SHORT).show();
                    } else if (errorCode == Error.GROUP_NOT_JOINED) {
                        Toast.makeText(context,context.getString(R.string.send_fail) + context.getString(R.string.error_send_not_in_the_group), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context,context.getString(R.string.send_fail) + context.getString(R.string.connect_failuer_toast), Toast.LENGTH_SHORT).show();
                    }
                }

                onUpdateView();
            }
        });

    }


    /**
     * The default child layout only needs to write ui in the bubble,
     * If all the layout you want to write their own, return true.
     * @return
     */
    protected abstract boolean overrideBaseLayout();

    /**
     * get the layout res id
     * @return
     */
    protected abstract int onGetLayoutId();

    /**
     * find view by id
     */
    protected abstract void onFindViewById();

    /**
     * refresh list view when message status change
     */
    protected abstract void onUpdateView();

    /**
     * setup view
     * 
     */
    protected abstract void onSetUpView();
    
    /**
     * on bubble clicked
     */
    protected abstract void onBubbleClick();


}
