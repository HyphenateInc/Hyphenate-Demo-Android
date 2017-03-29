package com.hyphenate.chatuidemo.group;

/**
 * Created by linan on 17/3/29.
 */

interface MucRoleJudge {
    boolean isOwner(String name);

    boolean isAdmin(String name);

    boolean isMuted(String name);
}
