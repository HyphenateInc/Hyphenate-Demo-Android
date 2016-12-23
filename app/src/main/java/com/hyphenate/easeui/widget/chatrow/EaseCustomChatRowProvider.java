package com.hyphenate.easeui.widget.chatrow;

import android.widget.BaseAdapter;
import com.hyphenate.chat.EMMessage;

/**
 * custom chat row provider
 *
 */
public interface EaseCustomChatRowProvider {
    /**
     * Get the number of types of custom chatrows<br/>
     * Each type of chatrow generally has two types: send type and receive type
     * @return
     */
    int getCustomChatRowTypeCount(); 
    
    /**
     * get chat row type，must be greater than 0
     * @return
     */
    int getCustomChatRowType(EMMessage message);
    
    /**
     * return the chat row base on the given message
     * @return
     */
    EaseChatRow getCustomChatRow(EMMessage message, int position, BaseAdapter adapter);
    
}
