package com.github.jrohatsch.moqqa.session;

public class Anonymous extends Authentication{
    protected Anonymous() {
        super(AuthenticationType.ANONYMOUS);
    }
}
