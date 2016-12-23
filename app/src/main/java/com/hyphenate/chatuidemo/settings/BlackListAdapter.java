package com.hyphenate.chatuidemo.settings;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.widget.EaseImageView;
import java.util.List;

/**
 * Created by lzan13 on 2016/10/26.
 * Blacklist
 */

public class BlackListAdapter extends RecyclerView.Adapter<BlackListAdapter.BlackListViewHolder> {

    private Context mContext;

    private LayoutInflater mInflater;

    private List<String> mBlackList;

    private ItemClickListener mItemClickListener;

    public BlackListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);

        mBlackList = EMClient.getInstance().contactManager().getBlackListUsernames();
    }

    @Override public BlackListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.em_item_blacklist, parent, false);
        BlackListViewHolder holder = new BlackListViewHolder(view);
        return holder;
    }

    @Override public void onBindViewHolder(BlackListViewHolder holder, final int position) {
        String username = mBlackList.get(position);
        holder.imageViewAvatar.setImageResource(R.drawable.ease_default_avatar);

        // apply for username
        holder.textViewUsername.setText(username);
        holder.unblockBtn.setTag(username);
        holder.unblockBtn.setOnClickListener(viewListener);
    }

    @Override public int getItemCount() {
        return mBlackList.size();
    }

    /**
     * Set blacklist view listener
     */
    private View.OnClickListener viewListener = new View.OnClickListener() {
        @Override public void onClick(View v) {
            String username = (String) v.getTag();
            switch (v.getId()) {
                case R.id.btn_unblock:
                    mItemClickListener.onItemAction(username, 0);
                    break;
            }
        }
    };

    /**
     * refresh blacklist
     */
    public void refreshBlackList() {
        if (mBlackList == null) {
            mBlackList = EMClient.getInstance().contactManager().getBlackListUsernames();
        } else {
            mBlackList.clear();
            mBlackList.addAll(EMClient.getInstance().contactManager().getBlackListUsernames());
        }
    }

    /**
     * item click on the callback interface
     */
    protected interface ItemClickListener {
        /**
         * Item action event
         *
         * @param username username
         * @param action item action
         */
        public void onItemAction(String username, int action);
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
     * BlackList ViewHolder
     */
    protected static class BlackListViewHolder extends RecyclerView.ViewHolder {
        EaseImageView imageViewAvatar;
        TextView textViewUsername;
        Button unblockBtn;

        public BlackListViewHolder(View itemView) {
            super(itemView);
            imageViewAvatar = (EaseImageView) itemView.findViewById(R.id.img_avatar);
            textViewUsername = (TextView) itemView.findViewById(R.id.text_username);
            unblockBtn = (Button) itemView.findViewById(R.id.btn_unblock);
        }
    }
}
