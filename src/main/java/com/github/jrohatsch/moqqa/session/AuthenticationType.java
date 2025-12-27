package com.github.jrohatsch.moqqa.session;

public enum AuthenticationType {
    ANONYMOUS("Anonymous"), USERNAME_PASSWORD("Username/Password"), SERVER_CERT("Server Certificate");
    public String text;

    AuthenticationType(String text) {
        this.text = text;
    }
}
