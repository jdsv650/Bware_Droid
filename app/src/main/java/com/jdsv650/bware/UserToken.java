package com.jdsv650.bware;

/**
 * Created by james on 10/13/17.
 */

public class UserToken {

    private String userName;
    private String token;
    private String expires;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String theToken) {
        token = theToken;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expiresOn) {
        expires = expiresOn;
    }

}
