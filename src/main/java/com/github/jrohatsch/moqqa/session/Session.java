package com.github.jrohatsch.moqqa.session;

import java.io.Serializable;

public record Session(String name, String address) implements Serializable {
    @Override
    public String toString() {
        return name;
    }
}
