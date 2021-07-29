package com.hey.model;

public class UserHash {
    private String userId;
    private String fullName;
    // no chi la cai gi tri rat don gian dung la vay do ma just do it:
    // co ban la the nay do la cai gi ??
    
    public UserHash(String userId, String fullName) {
        this.userId = userId;
        this.fullName = fullName;
    }
    // hash 2 thang nay la ra gia tri hash cua user:
    // cai nay cung kha la ngo do ma:L just do it:
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
