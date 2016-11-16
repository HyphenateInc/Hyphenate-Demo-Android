package com.hyphenate.chatuidemo;

import com.hyphenate.easeui.EaseConstant;

/**
 * Created by wei on 2016/11/9.
 */

public class Constant extends EaseConstant{
    public static final String CONVERSATION_NAME_APPLY = "em_conversation_apply";
    //0:chat,1:groupChat
    public static final String MESSAGE_ATTR_TYPE = "em_type";
    public static final String MESSAGE_ATTR_USERNAME = "em_username";
    public static final String MESSAGE_ATTR_GROUP_ID = "em_group_id";
    public static final String MESSAGE_ATTR_REASON = "em_reason";
    public static final String MESSAGE_ATTR_STATUS = "em_status";
    //0 : private group,1:public group
    public static final String MESSAGE_ATTR_GROUP_TYPE = "em_group_type";


    public static final String ACCOUNT_CONFLICT = "conflict";

    // Broadcast action
    public static final String BROADCAST_ACTION_CALL = "com.hyphenate.action.call";
    public static final String BROADCAST_ACTION_CONTACTS = "com.hyphenate.action.contacts";
    public static final String BROADCAST_ACTION_GROUP = "com.hyphenate.action.group";
    public static final String BROADCAST_ACTION_APPLY = "com.hyphenate.action.apply";
}
