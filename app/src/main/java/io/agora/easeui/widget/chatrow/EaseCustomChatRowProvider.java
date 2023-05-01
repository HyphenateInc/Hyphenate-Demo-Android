package io.agora.easeui.widget.chatrow;

import android.widget.BaseAdapter;
import io.agora.chat.ChatMessage;

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
    int getCustomChatRowType(ChatMessage message);
    
    /**
     * return the chat row base on the given message
     * @return
     */
    EaseChatRow getCustomChatRow(ChatMessage message, int position, BaseAdapter adapter);
    
}
