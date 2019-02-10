package com.cruxlab.parkurbn.model.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ChangePasswordBody {

    @SerializedName("new_password")
    @Expose
    private String newPassword;
    @SerializedName("old_password")
    @Expose
    private String oldPassword;

    public ChangePasswordBody(String newPassword, String oldPassword) {
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
    }
}
