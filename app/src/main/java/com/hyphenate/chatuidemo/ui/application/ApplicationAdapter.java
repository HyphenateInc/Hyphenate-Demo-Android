package com.hyphenate.chatuidemo.ui.application;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.widget.EaseImageView;
import java.util.Collections;
import java.util.List;

/**
 * Created by lzan13 on 2016/10/26.
 * Application and invite message
 */

public class ApplicationAdapter
        extends RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder> {

    private Context mContext;

    private LayoutInflater mInflater;

    // Application conversation
    private EMConversation mConversation;
    private List<EMMessage> mMessages;

    private ItemClickListener mItemClickListener;

    public ApplicationAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mConversation = EMClient.getInstance()
                .chatManager()
                .getConversation(EaseConstant.CONVERSATION_NAME_APPLICATION, null, true);
        mMessages = mConversation.getAllMessages();
        // The list collection is sorted in reverse orde
        Collections.reverse(mMessages);
    }

    @Override public ApplicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.em_item_application, parent, false);
        ApplicationViewHolder holder = new ApplicationViewHolder(view);
        return holder;
    }

    @Override public void onBindViewHolder(ApplicationViewHolder holder, final int position) {
        EMMessage message = mMessages.get(position);
        holder.imageViewAvatar.setImageResource(R.drawable.ease_default_avatar);

        String currUsername = EMClient.getInstance().getCurrentUser();
        String username = message.getStringAttribute("", "");
        // 设置申请的人
        if (currUsername.equals(username)) {
            holder.textViewUsername.setText(username);
        } else {
            holder.textViewUsername.setText(username);
        }
        // 设置申请理由

        holder.agreeBtn.setTag(position);
        holder.agreeBtn.setOnClickListener(viewListener);
    }

    @Override public int getItemCount() {
        return mMessages.size();
    }

    /**
     * 申请与通知列表内Button点击事件
     */
    private View.OnClickListener viewListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            int position = (int) v.getTag();
            switch (v.getId()) {
                case R.id.btn_agree:
                    mItemClickListener.onItemAction(position, 0);
                    break;
                case R.id.btn_reject:

                    break;
            }
        }
    };

    /**
     * item click on the callback interface
     */
    protected interface ItemClickListener {
        /**
         * Item action event
         *
         * @param position item position
         * @param action item action
         */
        public void onItemAction(int position, int action);
    }

    /**
     * Set item click listener
     *
     * @param listener item click callback listener
     */
    public void setItemClickListener(ItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * 自定义ViewHolder
     */
    protected static class ApplicationViewHolder extends RecyclerView.ViewHolder {
        EaseImageView imageViewAvatar;
        TextView textViewUsername;
        TextView textViewReason;
        TextView textViewStatus;
        Button agreeBtn;
        Button rejectBtn;

        /**
         *
         */
        public ApplicationViewHolder(View itemView) {
            super(itemView);
            imageViewAvatar = (EaseImageView) itemView.findViewById(R.id.img_avatar);
            textViewUsername = (TextView) itemView.findViewById(R.id.text_username);
            textViewReason = (TextView) itemView.findViewById(R.id.text_application_reason);
            textViewStatus = (TextView) itemView.findViewById(R.id.text_application_status);
            agreeBtn = (Button) itemView.findViewById(R.id.btn_agree);
            rejectBtn = (Button) itemView.findViewById(R.id.btn_reject);
        }
    }
}
