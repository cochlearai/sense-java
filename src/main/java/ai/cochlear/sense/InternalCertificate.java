package ai.cochlear.sense;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class InternalCertificate {
    private SSLContext ssl;

    public InternalCertificate() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        SSLContext sslContext = null;
        String temp = Constants.SERVER_CA_CERTIFICATE;
        temp = temp.replace("\n","\r\n");
        InputStream caInputStream = new ByteArrayInputStream(temp.getBytes());

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(caInputStream);

        String alias = cert.getSubjectX500Principal().getName();

        // Load Client CA
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setCertificateEntry(alias, cert);

        // Create KeyManager using Client CA
        String kmfAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(kmfAlgorithm);
        kmf.init(keyStore, null);

        // Create TrustManager using Client CA
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create SSLContext using KeyManager and TrustManager
        ssl = SSLContext.getInstance("TLS");
        ssl.init(null, tmf.getTrustManagers(), null);
    }

    public SSLContext get() {
        return ssl;
    }
}