package com.github.jrohatsch.moqqa.session;

import java.io.Serializable;

public abstract class Authentication implements Serializable {
    public final AuthenticationType type;

    protected Authentication(AuthenticationType type) {
        this.type = type;
    }
}
