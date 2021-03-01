package cmov.tecnico.ulisboa.pt.p2photo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import cmov.tecnico.ulisboa.pt.p2photo.security.SecurityHelper;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity
        implements Response.Listener<String>, Response.ErrorListener{

    public static final String TAG = "RegisterActivity";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    private AutoCompleteTextView mUsernameView;
    private Button mRegisterButton;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;
    private View mProgressView;
    private View mRegisterFormView;

    @Override
    public void onResponse(String response) {
        mRegisterButton.setEnabled(true);
        Util.showProgress(mRegisterFormView, mProgressView,false);
        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();

        try {
            for(String alias : SecurityHelper.getAllAliasesInTheKeystore()){
                Log.d(TAG, "Key Alias -> " + alias);
            }
        } catch (KeyStoreException | CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        finish();
    }



    @Override
    public void onErrorResponse(VolleyError ve) {
        mUsernameView.setError(getString(R.string.error_invalid_username));
        mRegisterButton.setEnabled(true);
        Util.showProgress(mRegisterFormView, mProgressView,false);
        if (ve.networkResponse != null) {
            VolleySingleton.LogNetworkError(ve);
            String error = new String(ve.networkResponse.data);
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            mPasswordView.requestFocus();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.
        mUsernameView = findViewById(R.id.register_username);

        mPasswordView = findViewById(R.id.register_password);


        mPasswordConfirmView = findViewById(R.id.register_password_confirm);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mRegisterButton = findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {
        mRegisterButton.setEnabled(false);

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the register attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirm_password = mPasswordConfirmView.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && Util.isPasswordInvalid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password confirmation, if the user entered one.
        if (!TextUtils.isEmpty(confirm_password) && Util.isPasswordInvalid(confirm_password)) {
            mPasswordConfirmView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check if passwords match
        if (!confirm_password.equals(password)) {
            mPasswordConfirmView.setError(getString(R.string.error_no_match_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username .
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (Util.isUsernameInvalid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        // Generate key possible key pair for user
        String newUserPublicKey = null;
        try {
            // Need to change p2server to also send the new username as response
            // to have specific aliases for different users in same phone
            SecurityHelper.generateRSAKeys(getApplicationContext(), "rsa_" + username);
            newUserPublicKey = SecurityHelper.getBase64RSAPublicKey("rsa_" + username);
            Log.d(TAG, "Keys created");
            Log.d(TAG, "Base64 Public Key -> " + newUserPublicKey);
        } catch (NoSuchAlgorithmException | KeyStoreException |
                CertificateException | IOException |
                NoSuchProviderException | InvalidAlgorithmParameterException e) {
            cancel = true;
        }



        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            mRegisterButton.setEnabled(true);
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Util.showProgress(mRegisterFormView, mProgressView, true);
            doVolleyRequest(username, password, newUserPublicKey);
        }
    }

    private void doVolleyRequest(final String username, final String password, final String publicKey) {
        VolleySingleton volley = VolleySingleton.getInstance(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, getString(R.string.url_register),
                this, this)
        {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getParams() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("usr", username);
                headers.put("pwd", password);
                headers.put("pub", publicKey);
                return headers;
            }
        };
        volley.addToRequestQueue(request);
    }

}

