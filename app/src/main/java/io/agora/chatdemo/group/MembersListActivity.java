package io.agora.chatdemo.group;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import io.agora.chat.ChatClient;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.easeui.EaseConstant;
import io.agora.easeui.widget.EaseListItemClickListener;
import io.agora.exceptions.ChatException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by benson on 2016/10/25.
 */

public class MembersListActivity extends BaseActivity {

    @BindView(R.id.recycler_members) RecyclerView recyclerView;
    LinearLayoutManager manager;
    List<String> membersList = new ArrayList<>();
    boolean isOwner = false;
    String groupId;
    ProgressDialog progressDialog;
    private boolean isChange;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_members);
        ButterKnife.bind(this);

        groupId = getIntent().getExtras().getString("groupId");
        isOwner = getIntent().getExtras().getBoolean("isOwner");
        membersList.addAll(getIntent().getStringArrayListExtra("members"));

        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        final MembersListAdapter adapter = new MembersListAdapter(this, membersList, LinearLayoutManager.VERTICAL);
        recyclerView.setAdapter(adapter);

        final Toolbar toolbar = getActionBarToolbar();
        toolbar.setTitle(getString(R.string.em_group_members) + "(" + membersList.size() + ")");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        adapter.setItemClickListener(new EaseListItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                if (!ChatClient.getInstance().getCurrentUser().equals(membersList.get(position))) {

                    startActivity(
                            new Intent(MembersListActivity.this, ChatActivity.class).putExtra(EaseConstant.EXTRA_USER_ID, membersList.get(position)));
                } else {
                    Snackbar.make(toolbar, "you can not chat with yourself", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override public void onItemLongClick(View view, final int position) {

                if (isOwner) {
                    new AlertDialog.Builder(MembersListActivity.this).setTitle(getString(R.string.em_group_member))
                            .setMessage(getString(R.string.em_group_delete_member))
                            .setPositiveButton(getString(R.string.em_ok), new DialogInterface.OnClickListener() {
                                @Override public void onClick(final DialogInterface dialog, int which) {
                                    progressDialog = ProgressDialog.show(MembersListActivity.this, getResources().getString(R.string.em_group_delete_member), getString(R.string.em_waiting), false);
                                    final String member = membersList.get(position);

                                    if (!ChatClient.getInstance().getCurrentUser().equals(member)) {
                                        new Thread(new Runnable() {
                                            @Override public void run() {
                                                try {
                                                    ChatClient.getInstance().groupManager().removeUserFromGroup(groupId, member);
                                                    runOnUiThread(new Runnable() {
                                                        @Override public void run() {
                                                            progressDialog.dismiss();
                                                            membersList.remove(member);
                                                            isChange = true;
                                                            adapter.notifyDataSetChanged();
                                                            toolbar.setTitle(getResources().getString(R.string.em_group_members) + "(" + membersList.size() + ")");
                                                        }
                                                    });
                                                } catch (final ChatException e) {
                                                    e.printStackTrace();
                                                    runOnUiThread(new Runnable() {
                                                        @Override public void run() {
                                                            progressDialog.dismiss();
                                                            Snackbar.make(toolbar, "delete failure" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                        }).start();
                                    } else {
                                        Snackbar.make(toolbar, "you can not delete yourself", Snackbar.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }

                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(getString(R.string.em_cancel), new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        });
    }

    @Override public void onBackPressed() {
        if (isChange){
            Intent intent = new Intent();
            intent.putExtra("selectedMembers", (ArrayList<String>) membersList);
            setResult(RESULT_OK,intent);
        }
        finish();
    }
}
