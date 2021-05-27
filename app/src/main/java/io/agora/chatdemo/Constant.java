package io.agora.chatdemo;

import io.agora.easeui.EaseConstant;

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
    public static final String BROADCAST_ACTION_CALL = "io.agora.action.call";
    public static final String BROADCAST_ACTION_CONTACTS = "io.agora.action.contacts";
    public static final String BROADCAST_ACTION_GROUP = "io.agora.action.group";
    public static final String BROADCAST_ACTION_APPLY = "io.agora.action.apply";

    // Settings
    public static final String SETTINGS_NOTIFICATION = "notification";
    public static final String SETTINGS_CHAT = "chat";
}
