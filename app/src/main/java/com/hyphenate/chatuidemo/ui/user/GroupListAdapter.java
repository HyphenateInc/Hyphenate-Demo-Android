package com.hyphenate.chatuidemo.ui.user;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatuidemo.R;
import java.util.List;

/**
 * Created by benson on 2016/10/21.
 */

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    private Context context;
    private List<EMGroup> groupList;

    GroupListAdapter(Context context, List<EMGroup> groups){
        this.context = context;
        this.groupList = groups;
    }

    @Override public int getItemCount() {
        return groupList.size();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.em_item_group_list,parent,false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        EMGroup group = groupList.get(position);
        holder.nameView.setText(group.getGroupName());
        holder.memberSizeView.setText(group.getMembers().size()+"");

    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.img_group_avatar) ImageView avatarView;
        @BindView(R.id.text_group_name) TextView nameView;
        @BindView(R.id.text_group_member_size) TextView memberSizeView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
