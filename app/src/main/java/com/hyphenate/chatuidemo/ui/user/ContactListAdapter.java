package com.hyphenate.chatuidemo.ui.user;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
    private List<String> stringList;
    private OnItemClickListener listener;

    ContactListAdapter(Context context, List<String> list){

        this.context = context;
        stringList = list;
    }

    interface OnItemClickListener{
        void ItemClickListener();
    }

    void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.em_item_contact_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String name = stringList.get(position);
        holder.contactName.setText(name);
        holder.header.setVisibility(View.VISIBLE);

        if (listener != null){
           holder.contactItem.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   listener.ItemClickListener();
               }
           });
        }
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

       @BindView(R.id.tv_contact_name) TextView contactName;
        @BindView(R.id.layout_contact_item) LinearLayout contactItem;
        @BindView(R.id.tv_header) TextView header;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

}
