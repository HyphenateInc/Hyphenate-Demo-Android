package com.hyphenate.chatuidemo.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hyphenate.chatuidemo.R;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by linan on 17/3/29.
 */

public class MucMembersVerticalAdapter extends RecyclerView.Adapter<MucMembersVerticalAdapter.ViewHolder> {

    private Context context;
    private List<String> membersList;
    private GroupUtils.MucRoleJudge roleJudge;

    public MucMembersVerticalAdapter(Context context, List<String> objects, GroupUtils.MucRoleJudge judge) {
        this.context = context;
        this.membersList = objects;
        this.roleJudge = judge;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.em_muc_list_item_vertical, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String username = membersList.get(position);
        EaseUserUtils.setUserNick(membersList.get(position), holder.memberNameView);
        boolean isOwner = roleJudge.isOwner(username);
        boolean isAdmin = roleJudge.isAdmin(username);
        if (isOwner) {
            holder.img_role.setVisibility(View.VISIBLE);
            holder.img_role.setImageResource(R.drawable.em_avatar_owner);
        } else if (isAdmin) {
            holder.img_role.setVisibility(View.VISIBLE);
            holder.img_role.setImageResource(R.drawable.em_avatar_admin);
        } else {
            holder.img_role.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.layout_member_list)  LinearLayout memberListView;
        @BindView(R.id.img_role)            ImageView img_role;
        @BindView(R.id.text_member_name)    TextView memberNameView;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
