package com.hyphenate.chatuidemo.ui.user;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chatuidemo.R;

import com.hyphenate.chatuidemo.ui.group.InviteMembersActivity;
import com.hyphenate.chatuidemo.ui.group.NewGroupActivity;
import com.hyphenate.easeui.widget.EaseListItemClickListener;
import com.hyphenate.exceptions.HyphenateException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by benson on 2016/10/8.
 */

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder> {

    private Context context;
    private List<UserEntity> userEntities;
    private EaseListItemClickListener listener;
    private boolean isSelected;
    private List<String> membersList;
    private boolean isOwner;
    private String groupId;
    private String[] newMembers;
    ProgressDialog progressDialog;
    List<String> selectedMembers = new ArrayList<>();

    ContactListAdapter(Context context, List<UserEntity> list) {

        this.context = context;
        userEntities = list;
    }

    public ContactListAdapter(Context context, List<UserEntity> list, boolean isSelected) {

        this.context = context;
        userEntities = list;
        this.isSelected = isSelected;
    }

    public ContactListAdapter(Context context, List<UserEntity> list, boolean isSelected, List<String> membersList, boolean isOwner, String groupId) {

        this.context = context;
        userEntities = list;
        this.isSelected = isSelected;
        if (isSelected) {
            this.membersList = membersList;
        }
        this.isOwner = isOwner;
        this.groupId = groupId;
    }

    public void setOnItemClickListener(EaseListItemClickListener listener) {
        this.listener = listener;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.em_item_contact_list, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(final ViewHolder holder, final int position) {

        final UserEntity user = userEntities.get(position);
        holder.contactNameView.setText(user.getUsername());

        if (isSelected) {
            holder.checkBoxView.setVisibility(View.VISIBLE);
            if (membersList != null && membersList.contains(user.getUsername())) {
                holder.checkBoxView.setChecked(true);
                holder.checkBoxView.setEnabled(false);
                holder.checkBoxView.setClickable(false);
            } else {
                holder.checkBoxView.setChecked(false);
                holder.checkBoxView.setEnabled(true);
                holder.checkBoxView.setClickable(true);
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
                    if (isSelected) {
                        listener.onItemClick(holder.checkBoxView, position);
                    } else {
                        listener.onItemClick(v, position);
                    }
                }
            });

            holder.contactItemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    listener.onLongItemClick(v, position);
                    return true;
                }
            });
        }

        holder.checkBoxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    selectedMembers.add(user.getUsername());
                } else {
                    if (selectedMembers.contains(user.getUsername())) {
                        selectedMembers.remove(user.getUsername());
                    }
                }
                newMembers = selectedMembers.toArray(new String[0]);
                String content = "", action = "INVITE";
                if (newMembers.length > 1) {
                    content = newMembers.length + "Members selected";
                } else if (newMembers.length == 1) {
                    content = newMembers[0];
                }

                if (isSelected && !isOwner) {
                    action = "NEXT";
                }

                Snackbar snackbar =
                        Snackbar.make(holder.contactItemLayout, content, Snackbar.LENGTH_SHORT).setAction(action, new View.OnClickListener() {
                            @Override public void onClick(final View v) {
                                if (isSelected && !isOwner) {

                                    context.startActivity(new Intent(context, NewGroupActivity.class).putStringArrayListExtra("newMembers",
                                            (ArrayList<String>) selectedMembers));
                                } else {
                                    progressDialog = ProgressDialog.show(context, "invite members", "waiting...", false);
                                    new Thread(new Runnable() {
                                        @Override public void run() {
                                            try {
                                                if (isOwner) {
                                                    EMClient.getInstance().groupManager().addUsersToGroup(groupId, newMembers);
                                                } else {
                                                    EMClient.getInstance().groupManager().inviteUser(groupId, newMembers, null);
                                                }

                                                ((InviteMembersActivity) context).runOnUiThread(new Runnable() {
                                                    @Override public void run() {
                                                        progressDialog.dismiss();
                                                        if (EMClient.getInstance().getOptions().isAutoAcceptGroupInvitation()) {
                                                            Intent intent = new Intent();
                                                            intent.putExtra("selectedMembers", (ArrayList<String>)selectedMembers);
                                                            ((InviteMembersActivity) context).setResult(InviteMembersActivity.RESULT_OK, intent);
                                                        } else {
                                                            ((InviteMembersActivity) context).setResult(InviteMembersActivity.RESULT_OK);
                                                        }
                                                        ((InviteMembersActivity) context).finish();
                                                    }
                                                });
                                            } catch (final HyphenateException e) {
                                                e.printStackTrace();
                                                ((InviteMembersActivity) context).runOnUiThread(new Runnable() {
                                                    @Override public void run() {
                                                        progressDialog.dismiss();
                                                        Snackbar.make(holder.contactItemLayout, "invite failure,please again" + e.getMessage(),
                                                                Snackbar.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    }).start();
                                }
                            }
                        });
                snackbar.show();
                if (newMembers.length == 0) {
                    snackbar.dismiss();
                }
            }
        });
    }

    @Override public int getItemCount() {
        return userEntities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_contact_name) TextView contactNameView;
        @BindView(R.id.layout_contact_item) RelativeLayout contactItemLayout;
        @BindView(R.id.txt_header) TextView headerView;
        @BindView(R.id.txt_base_line) TextView baseLineView;
        @BindView(R.id.checkbox) CheckBox checkBoxView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
