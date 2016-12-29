package com.hyphenate.easeui.model;

import java.util.List;

/**
 * Emoticon type
 *
 */
public class EaseEmojiconGroupEntity {
    /**
     * emoticon data
     */
    private List<EaseEmojicon> emojiconList;
    /**
     * icon
     */
    private int icon;
    /**
     * name
     */
    private String name;
    /**
     * emoticon type
     */
    private EaseEmojicon.Type type;
    
    public EaseEmojiconGroupEntity(){}
    
    public EaseEmojiconGroupEntity(int icon, List<EaseEmojicon> emojiconList){
        this.icon = icon;
        this.emojiconList = emojiconList;
        type = EaseEmojicon.Type.NORMAL;
    }
    
    public EaseEmojiconGroupEntity(int icon, List<EaseEmojicon> emojiconList, EaseEmojicon.Type type){
        this.icon = icon;
        this.emojiconList = emojiconList;
        this.type = type;
    }
    
    public List<EaseEmojicon> getEmojiconList() {
        return emojiconList;
    }
    public void setEmojiconList(List<EaseEmojicon> emojiconList) {
        this.emojiconList = emojiconList;
    }
    public int getIcon() {
        return icon;
    }
    public void setIcon(int icon) {
        this.icon = icon;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public EaseEmojicon.Type getType() {
        return type;
    }

    public void setType(EaseEmojicon.Type type) {
        this.type = type;
    }
    
    
}
