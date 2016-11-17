package com.hyphenate.easeui.model;

/**
 * Created by wei on 2016/10/10.
 */

public interface EaseUser{

    /**
     * get the user hyphenate id
     * @return
     */
    String getEaseUsername();

    /**
     * avatar url of the user, also can be drawable id that need convert to String
     * @return
     */
    String getEaseAvatar();

    /**
     * get the user nick
     * @return
     */
    String getEaseNickname();


    ///**
    // * initial letter for nickname
    // */
    //protected String initialLetter;
    ///**
    // * avatar url of the user
    // */
    //protected String avatar;
    //
    //public EaseUser(String username) {
    //    this.username = username;
    //}
    //
    //public String getInitialLetter() {
    //    if (initialLetter == null) {
    //        EaseCommonUtils.setUserInitialLetter(this);
    //    }
    //    return initialLetter;
    //}
    //
    //public void setInitialLetter(String initialLetter) {
    //    this.initialLetter = initialLetter;
    //}
    //
    //public String getAvatar() {
    //    return avatar;
    //}
    //
    //public void setAvatar(String avatar) {
    //    this.avatar = avatar;
    //}
    //
    //@Override public int hashCode() {
    //    return 17 * getUsername().hashCode();
    //}
    //
    //@Override public boolean equals(Object o) {
    //    if (o == null || !(o instanceof EaseUser)) {
    //        return false;
    //    }
    //    return getUsername().equals(((EaseUser) o).getUsername());
    //}
    //
    //@Override public String toString() {
    //    return nick == null ? username : nick;
    //}
}
