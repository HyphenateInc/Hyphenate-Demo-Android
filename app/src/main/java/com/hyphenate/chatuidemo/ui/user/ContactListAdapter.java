package com.hyphenate.chatuidemo.ui.user;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chatuidemo.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by benson on 2016/10/8.
 */

class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {

    private Context context;
    private List<UserEntity> userEntities;
    private OnItemClickListener listener;

    ContactListAdapter(Context context, List<UserEntity> list) {

        this.context = context;
        userEntities = list;
    }

    interface OnItemClickListener {
        void ItemClickListener();
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(context).inflate(R.layout.em_item_contact_list, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {

        UserEntity user = userEntities.get(position);
        holder.contactNameView.setText(user.getUsername());

        if (position == 0 || user.getInitialLetter() != null && !user.getInitialLetter()
                .equals(userEntities.get(position - 1).getInitialLetter())) {
            if (TextUtils.isEmpty(user.getInitialLetter())) {
                holder.headerView.setVisibility(View.INVISIBLE);
                holder.baseLineView.setVisibility(View.INVISIBLE);
            } else {
                holder.headerView.setVisibility(View.VISIBLE);
                holder.baseLineView.setVisibility(View.VISIBLE);
                holder.headerView.setText(user.getInitialLetter());
            }
        } else {
            holder.headerView.setVisibility(View.INVISIBLE);
            holder.baseLineView.setVisibility(View.INVISIBLE);
        }

        if (listener != null) {
            holder.contactItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.ItemClickListener();
                }
            });
        }
    }

    @Override public int getItemCount() {
        return userEntities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_contact_name) TextView contactNameView;
        @BindView(R.id.layout_contact_item) LinearLayout contactItemLayout;
        @BindView(R.id.txt_header) TextView headerView;
        @BindView(R.id.txt_base_line) TextView baseLineView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
