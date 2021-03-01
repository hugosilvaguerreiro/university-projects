package ulisboa.tecnico.nfchat;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.security.auth.x500.X500Principal;

public class SecurityCommons {

    private static final String LOG_ALIAS = "SecurityCom_LOGLOGdebug";

    private static final String SELF_KEY_ALIAS = "myKey";

    public static void CreateRSAIfNotExists() throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyStoreException, CertificateException, IOException {

        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);

        Certificate cert = ks.getCertificate(SELF_KEY_ALIAS);


        if(cert != null) {
            Log.d(LOG_ALIAS, "Certificate is: " + Base64.encodeToString(cert.getEncoded(), 0));
            return;
        }

        Calendar start = new GregorianCalendar();
        Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 1);

        KeyPairGenerator kpGenerator = KeyPairGenerator
                .getInstance("RSA", "AndroidKeyStore");

        AlgorithmParameterSpec spec;

        spec = new KeyGenParameterSpec.Builder(SELF_KEY_ALIAS, KeyProperties.PURPOSE_SIGN)
                .setCertificateSubject(new X500Principal("CN=" + SELF_KEY_ALIAS))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setCertificateSerialNumber(BigInteger.valueOf(1337))
                .setCertificateNotBefore(start.getTime())
                .setCertificateNotAfter(end.getTime())
                .build();


        kpGenerator.initialize(spec);

        KeyPair kp = kpGenerator.generateKeyPair();
        Log.d(LOG_ALIAS, "Certificate is: " + Base64.encodeToString(cert.getEncoded(), 0));
    }

    public static String getSelfCertificateString() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);

        Certificate cert = ks.getCertificate(SELF_KEY_ALIAS);

        if(cert != null) {
            Log.d(LOG_ALIAS, "Certificate is: " + Base64.encodeToString(cert.getEncoded(), 0));
            return Base64.encodeToString(cert.getEncoded(), 0);
        }
        return "";
    }


    public static void addOtherUserCertificate(String uuid, String certificate) {
    }
}



