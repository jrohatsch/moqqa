package com.github.jrohatsch.moqqa.session;

import java.io.Serializable;
import java.time.Instant;

public record Session(String name, String address, Authentication authentication, Instant connect) implements Serializable {
    @Override
    public String toString() {
        return name;
    }

    public static Session anonymous(String name, String address, Instant connect) {
        return new Session(name, address, new Anonymous(), connect);
    }

    public static Session usernamePassword(String name, String address, String username, String password) {
        return new Session(name, address, new UsernamePassword(username, password), Instant.EPOCH);
    }

    public static Session certPath(String name, String address, String certPath) {
        return new Session(name ,address, new ServerCertificate(certPath), Instant.EPOCH);
    }
}
