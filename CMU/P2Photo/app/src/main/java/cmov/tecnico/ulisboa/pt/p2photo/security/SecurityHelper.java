package cmov.tecnico.ulisboa.pt.p2photo.security;

import android.annotation.SuppressLint;
import android.content.Context;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;


import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import javax.crypto.KeyGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.security.auth.x500.X500Principal;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxProvider;

public class SecurityHelper {

    public static final String TAG = "SecurityHelper";

    //================================================================================
    // KEY GENERATION METHODS
    //================================================================================

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressWarnings("deprecation")
    @SuppressLint("ObsoleteSdkInt")
    public static void generateRSAKeys(Context context, String keyAlias) throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {

        Calendar start = new GregorianCalendar();
        Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 10);

        KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA,
                "AndroidKeyStore");

        AlgorithmParameterSpec spec;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Below Android M, use the KeyPairGeneratorSpec.Builder.
            spec = new KeyPairGeneratorSpec.Builder(context)
                    // You'll use the alias later to retrieve the key.  It's a key for the key!
                    .setAlias(keyAlias)
                    // The subject used for the self-signed certificate of the generated pair
                    .setSubject(new X500Principal("CN=" + keyAlias))
                    // The serial number used for the self-signed certificate of the
                    // generated pair.
                    .setSerialNumber(BigInteger.valueOf(1337))
                    .build();


        } else {
            // On Android M or above, use the KeyGenParameterSpec.Builder and specify permitted
            // properties  and restrictions of the key.
            spec = new KeyGenParameterSpec.Builder(keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setKeySize(2048)
                    .build();
        }

        kpGenerator.initialize(spec);

        KeyPair kp = kpGenerator.generateKeyPair();

        Log.d(TAG, "Public Key is: " + kp.getPublic().toString());
    }

    public static String generateAlbumKey(String albumName) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = null;

        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES);

        keyGenerator.init(128);

        SecretKey secretKey = null;
        try{
            secretKey = keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert secretKey != null;
        String encodedSecret = Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP);

        Log.d(TAG, "encodedSecret -> " + encodedSecret);

        return encodedSecret;
    }

    //================================================================================
    // ENCRYPTION METHODS
    //================================================================================

    public static byte[] encryptRSA(String data, String keyAlias) throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException,
            NoSuchPaddingException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // In AndroidKeyStore no password is required x)))
        RSAPublicKey publicKey = (RSAPublicKey) keyStore.getCertificate(keyAlias).getPublicKey();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return encryptedData;
    }


    public static byte[] encryptRSAShare(String data, String key) throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException,
            NoSuchPaddingException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {


        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // In AndroidKeyStore no password is required x)))

        byte[] decoded = Base64.decode(key, Base64.DEFAULT);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey genPublic = null;
        try {
            genPublic = kf.generatePublic(new X509EncodedKeySpec(decoded));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }


        cipher.init(Cipher.ENCRYPT_MODE, genPublic);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return encryptedData;
    }

    public static String encryptGCM(String data, SecretKey key) throws KeyStoreException,
            NoSuchPaddingException, NoSuchAlgorithmException, IOException, CertificateException,
            UnrecoverableEntryException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException {


        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        // iv length = 12bytes
        byte[] iv = cipher.getIV();

        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        byte[] result = concatenateByteArrays(iv, encryptedData);

        Log.d(TAG, "gcmENCRYPT -> " + Base64.encodeToString(result, Base64.DEFAULT));

        return Base64.encodeToString(result, Base64.DEFAULT);
    }

    //================================================================================
    // DECRYPTION METHODS
    //================================================================================

    public static String decryptRSA(String data, String keyAlias) throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException,
            NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException,
            UnrecoverableKeyException, InvalidKeyException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        // In AndroidKeyStore no password is required x)))
        Log.d(TAG, keyAlias);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, null);
        Log.d(TAG, privateKey.toString());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encryptedData = Base64.decode(data, Base64.DEFAULT);
        byte[] decryptedData = cipher.doFinal(encryptedData);

        String dataString = new String(decryptedData, StandardCharsets.UTF_8);

        Log.d(TAG, "RSA Base64 decoded decrypted data -> " + dataString);
        return dataString;
    }

    public static String decryptGCM(String data, SecretKey key) throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException, InvalidKeyException,
            UnrecoverableEntryException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            BadPaddingException, IllegalBlockSizeException {


        byte[] encryptedData = Base64.decode(data, Base64.DEFAULT);

        // Create spec with IV
        final GCMParameterSpec spec = new GCMParameterSpec(128, encryptedData, 0, 12);
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] decryptedData = cipher.doFinal(encryptedData, 12, encryptedData.length - 12);

        Log.d(TAG, "decrypted GCM data string -> " + new String(decryptedData, StandardCharsets.UTF_8));

        return Base64.encodeToString(decryptedData, Base64.DEFAULT);
    }

    //================================================================================
    // SUPPORT METHODS
    //================================================================================

    public static ArrayList<String> getAllAliasesInTheKeystore() throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return Collections.list(keyStore.aliases());
    }

    public static byte[] concatenateByteArrays(byte[] a, byte[] b) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(a);
        outputStream.write(b);
        return outputStream.toByteArray();
    }

    public static String getBase64RSAPublicKey(String keyAlias) throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        // In AndroidKeyStore no password is required x)))
        RSAPublicKey publicKey = (RSAPublicKey) keyStore.getCertificate(keyAlias).getPublicKey();
        return Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);
    }

}


