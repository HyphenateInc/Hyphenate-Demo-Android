package com.hyphenate.easeui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chatuidemo.R;

import java.util.List;

/**
 * Created by wei on 2016/9/28.
 */
public class EaseConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context mContext;
    private List<EMConversation> mConversationList;

    public EaseConversationAdapter(Context context, List<EMConversation> conversationList){
        mContext = context;
        mConversationList = conversationList;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.ease_row_conversation_list, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mConversationList.size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mAvatarView;
        TextView mNameView;
        TextView mTimeView;
        TextView mMessageView;

        public ViewHolder(View itemView) {
            super(itemView);
            mAvatarView = (ImageView) itemView.findViewById(R.id.img_avatar);
            mNameView = (TextView) itemView.findViewById(R.id.txt_name);
            mTimeView = (TextView) itemView.findViewById(R.id.txt_time);
            mMessageView = (TextView) itemView.findViewById(R.id.txt_message);
        }
    }
}
