package com.hey.model;

import java.io.Serializable;
import java.util.Date;

public class UserStatus implements Serializable {
    private String userId;
    private String status;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }
    // status la cai gi thi chua co lay duoc do :
    // dung roi do : con cai gi nua do :
    // dung vay :
    
    public void setStatus(String status) {
        this.status = status;
    }
}
