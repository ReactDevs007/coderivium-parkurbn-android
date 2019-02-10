package com.cruxlab.parkurbn.model.request;

public class LoginBody {

    private String email;
    private String password;

    public LoginBody(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginBody(String email) {
        this.email = email;
    }
}
