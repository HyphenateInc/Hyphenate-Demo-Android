package io.agora.chatdemo.group;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.CursorResult;
import io.agora.chat.Group;
import io.agora.chatdemo.R;
import io.agora.chatdemo.ui.BaseActivity;
import io.agora.chatdemo.user.model.UserEntity;
import io.agora.chatdemo.user.model.UserProfileManager;
import io.agora.easeui.utils.EaseUserUtils;
import io.agora.easeui.widget.EaseImageView;
import io.agora.exceptions.ChatException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.Window.FEATURE_NO_TITLE;
import static io.agora.chatdemo.group.GroupListActivity.toolbar;

/**
 * Created by linan on 17/3/31.
 */

public class GroupTransferOwnerActivity extends BaseActivity {

    @BindView(R.id.recycler_members)    RecyclerView recyclerView;

    String groupId;
    Group group;

    LinearLayoutManager manager;
    List<String> membersList = new ArrayList<>();
    List<UserEntity> userEntityList = new ArrayList<>();

    MyAdapter adapter;
    GroupUtils.LoadMoreData<UserEntity> loadMoreData;
    Handler handler = new Handler();


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_group_transfer_owner);
        ButterKnife.bind(this);

        groupId = getIntent().getExtras().getString("groupId");
        group = ChatClient.getInstance().groupManager().getGroup(groupId);

        // recyclerView
        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        adapter = new MyAdapter(this, userEntityList);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {

                if(scrollState == RecyclerView.SCROLL_STATE_IDLE){
                    if(adapter.getItemCount() != 0){
                        int lastVisibleItem = manager.findLastVisibleItemPosition();
                        int totalItemCount = manager.getItemCount();
                        if(loadMoreData.hasMoreData() && !loadMoreData.isLoading() && lastVisibleItem == totalItemCount-1){
                            loadMoreData.load();
                        }
                    }
                }
            }

        });

        // toolbar
        toolbar = getActionBarToolbar();
        toolbar.setNavigationIcon(R.drawable.em_ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        toolbar.setOnMenuItemClickListener(onMenuItemClick);

        // loadMoreData
        loadMoreData = new GroupUtils.LoadMoreData<>(this, userEntityList, adapter, new GroupUtils.LoadMoreData.onLoadListener() {
            @Override
            public void onInitialAction() {
                try {
                    group = ChatClient.getInstance().groupManager().getGroupFromServer(groupId);

                    List<String> list = new ArrayList<>();
                    list.addAll(group.getAdminList());
                    CursorResult<UserEntity> result = new CursorResult<>();
                    result.setData(UserProfileManager.convertContactList(list));
                    loadMoreData.setFetchResult(result);
                } catch (ChatException e) { e.printStackTrace(); }
            }

            @Override
            public void onLoadAction() {
                try {
                    CursorResult<String> result = ChatClient.getInstance().groupManager().fetchGroupMembers(
                            groupId, loadMoreData.getCursor(), GroupUtils.LoadMoreData.PAGE_SIZE);
                    CursorResult<UserEntity> fetchResult = new CursorResult<>();
//                    fetchResult.setCursor(result.getCursor());
                    fetchResult.setData(UserProfileManager.convertContactList(result.getData()));
                    loadMoreData.setFetchResult(fetchResult);
                } catch (ChatException e) { e.printStackTrace(); }
            }

            @Override
            public void onNoMoreDataAction() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(recyclerView, "No more data", Snackbar.LENGTH_LONG).show();
                    }
                }, 1000);
            }
        });
        loadMoreData.load();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.em_group_transfer_owner, menu);
        return true;
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_item_group_transfer_owner_save:
                    if (adapter.getSelected() != null && !adapter.getSelected().isEmpty()) {
                        transferOwner(adapter.getSelected());
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    public void transferOwner(String to) {

        Dialog dialog = new AlertDialog.Builder(this).setMessage("Transfer owner to " +  to)
                .setPositiveButton(getString(R.string.common_ok), new DialogInterface.OnClickListener() {

                    @Override public void onClick(DialogInterface dialog, int which) {
                        ChatClient.getInstance().groupManager().asyncChangeOwner(groupId, adapter.getSelected(), new ValueCallBack<Group>() {
                            @Override
                            public void onSuccess(Group value) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                });
                            }

                            @Override
                            public void onError(final int error, final String errorMsg) {
                                runOnUiThread(new Runnable() {
                                                  @Override
                                                  public void run() {
                                                      Toast.makeText(GroupTransferOwnerActivity.this,
                                                              String.format("failed to transfer owner, error code:%s, desc:%s",
                                                                      "" + error, errorMsg),
                                                              Toast.LENGTH_LONG).show();
                                                      Snackbar.make(recyclerView,
                                                              String.format("failed to transfer owner, error code:%s, desc:%s",
                                                                      "" + error, errorMsg),
                                                              Snackbar.LENGTH_LONG).show();
                                                  }
                                              }
                                );
                            }
                        });
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.common_cancel), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.requestWindowFeature(FEATURE_NO_TITLE);
        dialog.show();
    }

    // ================================= Adapter =================================

    private class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

        private Context context;
        private List<UserEntity> userEntities;
        private String selected;

        MyAdapter(Context context, List<UserEntity> list) {

            this.context = context;
            userEntities = list;
        }

        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.em_item_contact_list, parent, false);
            return new ViewHolder(view);
        }

        @Override public void onBindViewHolder(final ViewHolder holder, int position) {

            final UserEntity user = userEntities.get(position);
            EaseUserUtils.setUserAvatar(context, user.getUsername(), holder.avatarView);
            EaseUserUtils.setUserNick(user.getUsername(), holder.contactNameView);

            holder.img_right.setVisibility(userEntities.get(position).getUsername().equals(selected) ? View.VISIBLE : View.GONE);

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

            holder.contactItemLayout.setOnClickListener(new View.OnClickListener() {

                @Override public void onClick(View v) {
                    String username = userEntities.get(holder.getAdapterPosition()).getUsername();
                    if (username.equals(selected)) {
                        selected = null;
                    } else {
                        selected = username;
                        Snackbar.make(recyclerView, String.format("Transfer to:%s", selected), Snackbar.LENGTH_LONG)
                                .show();
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }

        @Override public int getItemCount() {
            return userEntities.size();
        }

        String getSelected() {
            return selected;
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_contact_name)        TextView contactNameView;
        @BindView(R.id.layout_contact_item)     RelativeLayout contactItemLayout;
        @BindView(R.id.txt_header)              TextView headerView;
        @BindView(R.id.txt_base_line)           TextView baseLineView;
        @BindView(R.id.img_contact_avatar)      EaseImageView avatarView;
        @BindView(R.id.img_right)               ImageView  img_right;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
