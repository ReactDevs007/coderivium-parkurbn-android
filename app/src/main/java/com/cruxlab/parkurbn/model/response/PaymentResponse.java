package com.cruxlab.parkurbn.model.response;

import com.google.gson.JsonObject;

public class PaymentResponse {

    private boolean success;
    private int expiration;

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public boolean isSuccessful() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public JsonObject errors;
}
