package com.robogames.RoboCupMS.Business.Security;


public class LoginObj {

    private String email;

    public LoginObj() {
        this.email = null;
    }

    public LoginObj(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
