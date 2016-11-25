package com.hyphenate.chatuidemo.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupInfo;
import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import java.util.List;

/**
 * Created by benson on 2016/10/21.
 */

public class GroupListAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<EMGroup> groupList; // the private groups info set
    private List<EMGroupInfo> publicGroupList; // the public groups info set
    private EaseListItemClickListener listener; //rewrite click and long click listener
    private boolean isPublic; // true is public group otherwise false

    private static final int VIEW_ITEM = 0;
    private static final int VIEW_PROGRESSBAR = 1;

    GroupListAdapter(Context context, List<EMGroup> groups) {
        this.context = context;
        this.groupList = groups;
    }

    GroupListAdapter(Context context, List<EMGroupInfo> groups, boolean isPublic) {
        this.context = context;
        this.publicGroupList = groups;
        this.isPublic = isPublic;
    }

    public void setItemClickListener(EaseListItemClickListener listener) {
        this.listener = listener;
    }

    @Override public int getItemCount() {
        if (isPublic) {
            return publicGroupList == null ? 0 : publicGroupList.size();
        } else {
            return groupList == null ? 0 : groupList.size();
        }
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_ITEM) {
            view = LayoutInflater.from(context).inflate(R.layout.em_item_group_list, parent, false);
        } else {
            view = new ProgressBar(context);
        }
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof ViewHolder) {
            if (isPublic) {
                ((ViewHolder) holder).groupJoinView.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).guideArrowView.setVisibility(View.GONE);
                EMGroupInfo group = publicGroupList.get(position);
                ((ViewHolder) holder).nameView.setText(group.getGroupName());
            } else {
                ((ViewHolder) holder).groupItem.setBackgroundResource(0);
                ((ViewHolder) holder).guideArrowView.setImageResource(R.drawable.cell_chevron_right);

                EMGroup group = groupList.get(position);
                ((ViewHolder) holder).nameView.setText(group.getGroupName());
            }

            if (listener != null) {
                if (isPublic) {
                    ((ViewHolder) holder).groupJoinView.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            listener.onItemClick(v, position);
                        }
                    });
                } else {
                    ((ViewHolder) holder).groupItem.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {

                            listener.onItemClick(v, position);
                        }
                    });
                }
            }
        } else if (holder instanceof ProgressBarViewHolder) {
            ((ProgressBarViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override public int getItemViewType(int position) {
        if (isPublic) {
            return publicGroupList.get(position) != null ? VIEW_ITEM : VIEW_PROGRESSBAR;
        } else {
            return groupList.get(position) != null ? VIEW_ITEM : VIEW_PROGRESSBAR;
        }
    }

    private static class ProgressBarViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public ProgressBarViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.layout_group_list) RelativeLayout groupItem;
        @BindView(R.id.img_group_avatar) ImageView avatarView;
        @BindView(R.id.text_group_name) TextView nameView;
        @BindView(R.id.btn_group_join) Button groupJoinView;
        @BindView(R.id.img_guide_arrow) ImageView guideArrowView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
