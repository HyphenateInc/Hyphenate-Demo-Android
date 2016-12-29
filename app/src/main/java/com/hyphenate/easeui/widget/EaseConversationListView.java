package com.hyphenate.easeui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.easeui.adapter.EaseConversationListAdapter;
import com.hyphenate.easeui.model.EaseUser;
import com.hyphenate.easeui.utils.EaseUserUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wei on 2016/9/28.
 * Conversation list view, which extends RecyclerView
 */
public class EaseConversationListView extends RecyclerView {
    protected final int MSG_REFRESH_ADAPTER_DATA = 0;

    protected Context mContext;
    protected List<EMConversation> mConversationList;
    protected EaseConversationListAdapter mAdapter;

    protected List<String> mHiddenList;

    public EaseConversationListView(Context context) {
        this(context, null);
    }

    public EaseConversationListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EaseConversationListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
//        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EaseConversationList);
//        primaryColor = ta.getColor(R.styleable.EaseConversationList_cvsListPrimaryTextColor, getResources().getColor(R.color.list_itease_primary_color));
//        secondaryColor = ta.getColor(R.styleable.EaseConversationList_cvsListSecondaryTextColor, getResources().getColor(R.color.list_itease_secondary_color));
//        timeColor = ta.getColor(R.styleable.EaseConversationList_cvsListTimeTextColor, getResources().getColor(R.color.list_itease_secondary_color));
//        primarySize = ta.getDimensionPixelSize(R.styleable.EaseConversationList_cvsListPrimaryTextSize, 0);
//        secondarySize = ta.getDimensionPixelSize(R.styleable.EaseConversationList_cvsListSecondaryTextSize, 0);
//        timeSize = ta.getDimension(R.styleable.EaseConversationList_cvsListTimeTextSize, 0);

//        ta.recycle();

    }

    /**
     * Init this view, which use a default sorted mConversation list.
     * If you want to show list with your own sort, use {@link #init(Comparator)}
     */
    public void init() {
        init(null);
    }

    /**
     * Init list view with the passed Comparator
     *
     * @param comparator
     */
    public void init(Comparator<EMConversation> comparator) {
        mConversationList = loadConversationList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        setLayoutManager(layoutManager);

        if(comparator == null){
            comparator = new Comparator<EMConversation>() {
                @Override public int compare(EMConversation o1, EMConversation o2) {
                    return Long.valueOf(o2.getLastMessage().getMsgTime()).compareTo(o1.getLastMessage().getMsgTime());
                }
            };
        }

        mAdapter = new EaseConversationListAdapter(getContext(), comparator);
        setAdapter(mAdapter);

        mAdapter.edit().replaceAll(mConversationList).commit();
    }

    /**
     * filter mConversation list with passed string
     * @param cs
     */
    public void filter(String cs) {
        if(cs == null)
            cs = "";
        mAdapter.edit().replaceAll(getFilterList(cs)).commit();
    }

    Handler mHandler = new Handler(){
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == MSG_REFRESH_ADAPTER_DATA){
                mAdapter.edit().replaceAll(loadConversationList()).commit();
            }
        }
    };

    /**
     * Refresh conversations list view
     */
    public void refresh() {
        mHandler.sendEmptyMessage(MSG_REFRESH_ADAPTER_DATA);
    }

    /**
     * get list item entity
     * @param position
     */
    public EMConversation getItem(int position){
        return mAdapter.getItem(position);
    }

    /**
     * set list item onclick listener
     * @param onItemClickListener EaseListItemClickListener
     */
    public void setOnItemClickListener(EaseListItemClickListener onItemClickListener){
        mAdapter.setOnItemClickListener(onItemClickListener);
    }

    /**
     * load mConversation list
     * @return
     */
    protected synchronized List<EMConversation> loadConversationList() {
        // get all conversations
        Map<String, EMConversation> conversations = EMClient.getInstance().chatManager().getAllConversations();
        if (mConversationList == null) {
            mConversationList = new ArrayList<>(conversations.values());
        } else {
            mConversationList.clear();
            mConversationList.addAll(conversations.values());
        }
        Iterator iterator = mConversationList.iterator();
        while (iterator.hasNext()){
            EMConversation conversation = (EMConversation) iterator.next();
            if(conversation.getAllMessages().size() == 0 || (mHiddenList != null && mHiddenList.contains(conversation.conversationId()))){
                //remove the conversation which messages size == 0
                iterator.remove();
            }
        }
        return mConversationList;
    }

    protected synchronized  List<EMConversation> getFilterList(String query){
        List<EMConversation> list = new ArrayList<>();
        Iterator iterator = mConversationList.iterator();
        while (iterator.hasNext()){
            EMConversation conversation = (EMConversation) iterator.next();
            String username = conversation.conversationId();
            EMGroup group = EMClient.getInstance().groupManager().getGroup(username);
            //add group name or user nick
            if(group != null){
                username = group.getGroupName();
            }else{
                EaseUser user = EaseUserUtils.getUserInfo(username);
                if(user != null && user.getEaseNickname() != null)
                    username = user.getEaseNickname();
            }

            if(username.contains(query)){
                list.add(conversation);
            }
        }
        return list;
    }

    /**
     * set a list you want not to show in conversation list
     * @param hiddenList
     */
    public void setHiddenList(List<String> hiddenList){
        mHiddenList = hiddenList;
    }

}
