package com.github.jrohatsch.moqqa.session;

public class UsernamePassword extends Authentication {
    public final String username;
    public final String password;
    protected UsernamePassword(String username, String password) {
        super(AuthenticationType.USERNAME_PASSWORD);
        this.username = username;
        this.password = password;
    }
}
