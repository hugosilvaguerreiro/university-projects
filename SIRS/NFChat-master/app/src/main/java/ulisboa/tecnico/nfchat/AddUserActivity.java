package ulisboa.tecnico.nfchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.UUID;

public class AddUserActivity extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback {
    NfcAdapter mNfcAdapter;

    private final String LOG_ALIAS = "AddUserActiv_LOGdebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_user_main);

        TextView uuidSpace = (TextView) findViewById(R.id.uuid_data);
        uuidSpace.setText(getUUID());

        TextView certificate_data = (TextView) findViewById(R.id.certificate_data);
        certificate_data.setHorizontallyScrolling(true);
        certificate_data.setMovementMethod(new ScrollingMovementMethod());

        try {
            certificate_data.setText(SecurityCommons.getSelfCertificateString());
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }

        try {
            SecurityCommons.CreateRSAIfNotExists();
        } catch (NoSuchProviderException | NoSuchAlgorithmException |
                InvalidAlgorithmParameterException | KeyStoreException |
                CertificateException | IOException e) {
            e.printStackTrace();
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            return;
        }
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);

        Log.d(LOG_ALIAS, "Device ID:" + getUUID());
    }


    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String id = getUUID();
        Log.d(LOG_ALIAS, "Device ID:" + id);
        String certificate = "";
        try {
            certificate = SecurityCommons.getSelfCertificateString();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { NdefRecord.createMime(
                        "application/vnd.ulisboa.tecnico.nfchat", (id + "|" + certificate).getBytes())
                        //,NdefRecord.createApplicationRecord("example.dorin.sharesexyprivates")
                });
        return msg;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        String recv = new String(msg.getRecords()[0].getPayload());
        Log.d(LOG_ALIAS, "recieved: " + recv);
        String[] args = recv.split("|");

        Intent switchIntent = new Intent(AddUserActivity.this, MainActivity.class);
        switchIntent.putExtra("uuid", args[0].toString());
        switchIntent.putExtra("cert", args[1].toString());
        startActivity(switchIntent);

    }

    private String getUUID() {
        SharedPreferences prefs = this.getSharedPreferences("UserSelfUUID", 0);
        String notSet = "NOTSET";
        String android_key;
        android_key = prefs.getString("id", notSet);

        if (android_key.equals(notSet)) {
            Log.d(LOG_ALIAS, "Creating keys for 1st time");
            android_key = UUID.randomUUID().toString();
            prefs.edit().putString("id", android_key).commit();
        }
        return android_key;
    }

    public void createTestUser(View view) {
        String id = getUUID();
        String cert = null;
        try {
            cert = SecurityCommons.getSelfCertificateString();
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }

        Log.d(LOG_ALIAS, "creating test user: \n" + id + "\n" + cert);

        Intent switchIntent = new Intent(AddUserActivity.this, MainActivity.class);
        switchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        switchIntent.putExtra("uuid", id);
        switchIntent.putExtra("cert", cert);
        startActivity(switchIntent);
    }
}