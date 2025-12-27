package com.github.jrohatsch.moqqa.session;

public class ServerCertificate extends Authentication {
    public final String certPath;
    protected ServerCertificate(String certPath) {
        super(AuthenticationType.SERVER_CERT);
        this.certPath = certPath;
    }
}
