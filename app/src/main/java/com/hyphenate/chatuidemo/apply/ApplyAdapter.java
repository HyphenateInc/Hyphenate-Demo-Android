package com.hyphenate.chatuidemo.apply;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatuidemo.Constant;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.exceptions.HyphenateException;
import java.util.Collections;
import java.util.List;

/**
 * Created by lzan13 on 2016/10/26.
 * Apply and invite message
 */

class ApplyAdapter extends RecyclerView.Adapter<ApplyAdapter.ApplyViewHolder> {

    private LayoutInflater mInflater;

    private EMConversation mConversation;
    private List<EMMessage> mMessages;

    private ItemClickListener mItemClickListener;

    ApplyAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mConversation = EMClient.getInstance()
                .chatManager()
                .getConversation(Constant.CONVERSATION_NAME_APPLY, null, true);
        mMessages = mConversation.getAllMessages();
        // The list collection is sorted in reverse order
        Collections.reverse(mMessages);
    }

    @Override public ApplyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.em_item_apply, parent, false);
        return new ApplyViewHolder(view);
    }

    @Override public void onBindViewHolder(ApplyViewHolder holder, final int position) {
        EMMessage message = mMessages.get(position);
        holder.imageViewAvatar.setImageResource(R.drawable.ease_default_avatar);

        if (message.getIntAttribute(Constant.MESSAGE_ATTR_TYPE, 0) == 1) {
            try {
                holder.textViewUsername.setText(
                        message.getStringAttribute(Constant.MESSAGE_ATTR_GROUP_ID));
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        } else {
            // apply for username
            String username = message.getStringAttribute(Constant.MESSAGE_ATTR_USERNAME, "");
            holder.textViewUsername.setText(username);
        }

        // apply for reason
        String reason = message.getStringAttribute(Constant.MESSAGE_ATTR_REASON, "");
        holder.textViewReason.setText(reason);

        // apply for status
        String status = message.getStringAttribute(Constant.MESSAGE_ATTR_STATUS, "");
        if (!TextUtils.isEmpty(status)) {
            holder.textViewStatus.setText(status);
            holder.textViewStatus.setVisibility(View.VISIBLE);
            holder.agreeBtn.setVisibility(View.GONE);
            holder.rejectBtn.setVisibility(View.GONE);
        } else {
            holder.textViewStatus.setVisibility(View.GONE);
            holder.agreeBtn.setVisibility(View.VISIBLE);
            holder.rejectBtn.setVisibility(View.VISIBLE);
        }

        holder.agreeBtn.setTag(message.getMsgId());
        holder.agreeBtn.setOnClickListener(viewListener);
        holder.rejectBtn.setTag(message.getMsgId());
        holder.rejectBtn.setOnClickListener(viewListener);
    }

    @Override public int getItemCount() {
        return mMessages.size();
    }

    /**
     * Set apply for view listener
     */
    private View.OnClickListener viewListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            String msgId = (String) v.getTag();
            switch (v.getId()) {
                case R.id.btn_agree:
                    mItemClickListener.onItemAction(msgId, 0);
                    break;
                case R.id.btn_reject:
                    mItemClickListener.onItemAction(msgId, 1);
                    break;
            }
        }
    };

    /**
     * item click on the callback interface
     */
    interface ItemClickListener {
        /**
         * Item action event
         *
         * @param msgId item message id
         * @param action item action
         */
        void onItemAction(String msgId, int action);
    }

    /**
     * Set item click listener
     *
     * @param listener item click callback listener
     */
    public void setItemClickListener(ItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void refresh() {
        if (mMessages == null) {
            mMessages = mConversation.getAllMessages();
        } else {
            mMessages.clear();
            mMessages.addAll(mConversation.getAllMessages());
        }
        // The list collection is sorted in reverse order
        Collections.reverse(mMessages);
    }

    /**
     * Apply for ViewHolder
     */
    static class ApplyViewHolder extends RecyclerView.ViewHolder {
        EaseImageView imageViewAvatar;
        TextView textViewUsername;
        TextView textViewReason;
        TextView textViewStatus;
        Button agreeBtn;
        Button rejectBtn;

        ApplyViewHolder(View itemView) {
            super(itemView);
            imageViewAvatar = (EaseImageView) itemView.findViewById(R.id.img_avatar);
            textViewUsername = (TextView) itemView.findViewById(R.id.text_username);
            textViewReason = (TextView) itemView.findViewById(R.id.text_apply_reason);
            textViewStatus = (TextView) itemView.findViewById(R.id.text_apply_status);
            agreeBtn = (Button) itemView.findViewById(R.id.btn_agree);
            rejectBtn = (Button) itemView.findViewById(R.id.btn_reject);
        }
    }
}
