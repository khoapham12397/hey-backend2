package com.hey.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class FriendList implements Serializable {

    private UserHash currentUserHashes;
    private UserHash friendUserHashes;
    // ban chat thiet ke ngaovl: // dung cay :
    // tu cai UserHash nay thuc ra la get currentUserHashes => tu do get duoc:
    // cai gi 
    // user hash convert to json don gian dung ay :
    // sau do the nao ?? cai nay no dung cuc ky thu cong dung la vay luon do
    
    public UserHash getCurrentUserHashes() {
        return currentUserHashes;
    }

    public void setCurrentUserHashes(UserHash currentUserHashes) {
        this.currentUserHashes = currentUserHashes;
    }

    public UserHash getFriendUserHashes() {
        return friendUserHashes;
    }

    public void setFriendUserHashes(UserHash friendUserHashes) {
        this.friendUserHashes = friendUserHashes;
    }
}
