package com.hyphenate.chatuidemo.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chatuidemo.R;
import com.hyphenate.chatuidemo.user.model.UserEntity;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.easeui.widget.EaseSwipeLayout;
import com.hyphenate.easeui.widget.RecyclerSwipeView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by benson on 2016/10/8.
 */

public class GroupMuteListAdapter extends RecyclerView.Adapter<GroupMuteListAdapter.ViewHolder> {

    private Context context;
    private List<UserEntity> userEntities;
    private EaseListItemClickListener listener;
    private boolean showCheckBox;
    private List<String> membersList;
    private EaseSwipeLayout.SwipeAction action;
    private EaseSwipeLayout.SwipeListener swipeListener;

    GroupMuteListAdapter(Context context, List<UserEntity> list) {

        this.context = context;
        userEntities = list;
    }

    public GroupMuteListAdapter(Context context, List<UserEntity> list, boolean showCheckBox, EaseSwipeLayout.SwipeListener swipeListener) {
        this.context = context;
        userEntities = list;
        this.showCheckBox = showCheckBox;
        this.swipeListener = swipeListener;
    }

    public GroupMuteListAdapter(Context context, List<UserEntity> list, boolean showCheckBox, List<String> membersList) {

        this.context = context;
        userEntities = list;
        this.showCheckBox = showCheckBox;
        if (showCheckBox) {
            this.membersList = membersList;
        }
    }

    public void setOnItemClickListener(EaseListItemClickListener listener) {
        this.listener = listener;
    }

    public void setSwipeActions(EaseSwipeLayout.SwipeAction action) {
        this.action = action;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.em_item_group_mute_list, parent, false);
        return new ViewHolder(view, swipeListener);
    }

    @Override public void onBindViewHolder(final ViewHolder holder, final int position) {

        final UserEntity user = userEntities.get(position);
        EaseUserUtils.setUserAvatar(context, user.getUsername(), holder.avatarView);
        EaseUserUtils.setUserNick(user.getUsername(), holder.contactNameView);

        if (showCheckBox) {
            //set checkbox listener
            ((InviteMembersActivity) context).checkBoxListener(holder.checkBoxView, user);
            holder.checkBoxView.setVisibility(View.VISIBLE);

            if (membersList != null && membersList.contains(user.getUsername())) {
                holder.checkBoxView.setChecked(true);
                holder.checkBoxView.setEnabled(false);
                holder.checkBoxView.setClickable(false);
            } else {
                if (((InviteMembersActivity) context).selectedMembers.contains(user.getUsername())) {
                    holder.checkBoxView.setChecked(true);
                } else {
                    holder.checkBoxView.setChecked(false);
                    holder.checkBoxView.setEnabled(true);
                    holder.checkBoxView.setClickable(true);
                }
            }
        }

        if (position == 0 || user.getInitialLetter() != null && !user.getInitialLetter().equals(userEntities.get(position - 1).getInitialLetter())) {
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
                    if (showCheckBox) {
                        listener.onItemClick(holder.checkBoxView, position);
                    } else {
                        listener.onItemClick(v, position);
                    }
                }
            });

            holder.contactItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    listener.onItemLongClick(v, position);
                    return true;
                }
            });
        }

        EaseSwipeLayout.SwipeAction[] actions = {action};
        holder.swipeLayout.setButtons(actions);
        holder.swipeLayout.updateListPosition(position);
    }

    @Override public int getItemCount() {
        return userEntities.size();
    }

    static class ViewHolder extends RecyclerSwipeView.SwipeViewHolder {

        @BindView(R.id.txt_contact_name) TextView contactNameView;
        @BindView(R.id.layout_contact_item) RelativeLayout contactItemLayout;
        @BindView(R.id.txt_header) TextView headerView;
        @BindView(R.id.txt_base_line) TextView baseLineView;
        @BindView(R.id.checkbox) CheckBox checkBoxView;
        @BindView(R.id.img_contact_avatar) EaseImageView avatarView;
        @BindView(R.id.swipe_layout) EaseSwipeLayout swipeLayout;

        ViewHolder(View itemView, EaseSwipeLayout.SwipeListener listener) {
            super(itemView, listener);
            ButterKnife.bind(this, itemView);
            swipeLayout.attachListItem(itemView);
        }
    }
}
