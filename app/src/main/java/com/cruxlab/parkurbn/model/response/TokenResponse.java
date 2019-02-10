package com.cruxlab.parkurbn.model.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenResponse {

    @SerializedName("token")
    @Expose
    private String clientToken;

    public String getClientToken() {
        return clientToken;
    }

}
