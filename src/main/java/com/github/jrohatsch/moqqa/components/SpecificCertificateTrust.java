package com.github.jrohatsch.moqqa.components;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SpecificCertificateTrust {

    public static SSLSocketFactory createCustomSSLFactory(String certPath) throws Exception {
        // Load the specific certificate
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(
                new FileInputStream(certPath));

        // Create a KeyStore containing our certificate
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry("serverCert", certificate);

        // Create TrustManager that trusts our KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        // Create SSLContext with our TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        return sslContext.getSocketFactory();
    }
}