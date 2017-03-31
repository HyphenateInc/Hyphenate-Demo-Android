package com.hyphenate.chatuidemo.group;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseListItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by benson on 2016/10/26.
 */

public class MembersListAdapter extends RecyclerView.Adapter<MembersListAdapter.ViewHolder> {

    private List<String> membersList;
    private EaseListItemClickListener listener;
    private Context context;
    private int RECYCLER_ORIENTATION = 0;
    private boolean isOpenInvite;

    public MembersListAdapter(Context context, List<String> objects, int orientation, boolean isOpenInvite) {
        this.context = context;
        this.membersList = objects;
        this.RECYCLER_ORIENTATION = orientation;
        this.isOpenInvite = isOpenInvite;
    }

    public MembersListAdapter(Context context, List<String> objects, int orientation) {
        this.context = context;
        this.membersList = objects;
        this.RECYCLER_ORIENTATION = orientation;
    }

    public void setItemClickListener(EaseListItemClickListener listener) {
        this.listener = listener;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (RECYCLER_ORIENTATION == LinearLayoutManager.VERTICAL) {
            view = LayoutInflater.from(context).inflate(R.layout.em_item_group_member_list_vertical, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.em_item_group_member_list_horizontal, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(ViewHolder holder, final int position) {

        if (RECYCLER_ORIENTATION == LinearLayoutManager.HORIZONTAL) {
            if (isOpenInvite) {
                if (position == 0) {
                    holder.memberAvatarView.setImageResource(R.drawable.add_member_icon);
                } else {
                    EaseUserUtils.setUserNick(membersList.get(position - 1), holder.memberNameView);
                    EaseUserUtils.setUserAvatar(context, membersList.get(position - 1), holder.memberAvatarView);
                }
            } else {
                EaseUserUtils.setUserNick(membersList.get(position), holder.memberNameView);
                EaseUserUtils.setUserAvatar(context, membersList.get(position), holder.memberAvatarView);
            }
        } else {
            EaseUserUtils.setUserNick(membersList.get(position), holder.memberNameView);
            EaseUserUtils.setUserAvatar(context, membersList.get(position), holder.memberAvatarView);
        }

        if (listener != null) {
            if (RECYCLER_ORIENTATION == LinearLayoutManager.HORIZONTAL) {
                holder.memberListView.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        listener.onItemClick(v, position);
                    }
                });
            } else {
                holder.memberListView.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        listener.onItemClick(v, position);
                    }
                });

                holder.memberListView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override public boolean onLongClick(View v) {
                        listener.onItemLongClick(v, position);
                        return true;
                    }
                });
            }
        }
    }

    @Override public int getItemCount() {
        if (RECYCLER_ORIENTATION == LinearLayoutManager.HORIZONTAL && isOpenInvite) {
            return membersList.size() + 1;
        } else {
            return membersList.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.layout_member_list) LinearLayout memberListView;
        @BindView(R.id.img_member_avatar) ImageView memberAvatarView;
        @BindView(R.id.text_member_name) TextView memberNameView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
