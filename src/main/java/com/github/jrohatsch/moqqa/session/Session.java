package com.github.jrohatsch.moqqa.session;

import java.io.Serializable;

public record Session(String name, String address, Authentication authentication) implements Serializable {
    @Override
    public String toString() {
        return name;
    }

    public static Session anonymous(String name, String address) {
        return new Session(name, address, new Anonymous());
    }

    public static Session usernamePassword(String name, String address, String username, String password) {
        return new Session(name, address, new UsernamePassword(username, password));
    }

    public static Session certPath(String name, String address, String certPath) {
        return new Session(name ,address, new ServerCertificate(certPath));
    }
}
