package cmov.tecnico.ulisboa.pt.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.DropboxGalleryActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.WifiDirectGalleryActivity;
import cmov.tecnico.ulisboa.pt.p2photo.p2lib.cloud.UserActivity;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.activities.WiFiDirectActivity;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity
        implements Response.Listener<String>, Response.ErrorListener {
    // UI references.
    final static private int DEFAULT_ID = -1;
    final static private String DEFAULT_COOKIE = "cookie";
    final static private String DEFAULT_USERNAME = "asd";



    private AutoCompleteTextView mUsernameView;
    private Button mUsernameSignInButton;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Class nextClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedPref = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
        final String cookie = sharedPref.getString(getString(R.string.saved_user_cookie), DEFAULT_COOKIE);
        final int id = sharedPref.getInt(getString(R.string.saved_user_id), DEFAULT_ID);
        final String username = sharedPref.getString(getString(R.string.saved_username), DEFAULT_COOKIE);

        String mode = null;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                mode= null;
            } else {
                mode= extras.getString("mode");
            }
        } else {
            mode = (String) savedInstanceState.getSerializable("mode");
        }

        if( mode == null) {
            Log.e("LoginActivity", "this error should not happen");
        } else if (mode.equals(getString(R.string.mode_cloud))) {
            this.nextClass = DropboxGalleryActivity.class;
        } else if (mode.equals(getString(R.string.mode_wifi))) {
            this.nextClass = WiFiDirectActivity.class;
        }


        if(id != DEFAULT_ID && !cookie.equals(DEFAULT_COOKIE) && !username.equals(DEFAULT_USERNAME)) {
            setContentView(R.layout.activity_loged_in);
            mUsernameSignInButton = findViewById(R.id.username_continue_button);
            mUsernameSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    UserSessionDetails.user_name = username;
                    UserSessionDetails.user_id = id;
                    UserSessionDetails.cookie = cookie;
                    signIn();
                }
            });

            Button mForgetButton = findViewById(R.id.username_forget_button);
            mForgetButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.saved_user_cookie), DEFAULT_COOKIE);
                    editor.putInt(getString(R.string.saved_user_id), DEFAULT_ID);
                    editor.putString(getString(R.string.saved_username), DEFAULT_USERNAME);
                    editor.apply();
                    Intent sameIntent = getIntent();
                    finish();
                    startActivity(sameIntent);
                }
            });

            TextView tvUserID = findViewById(R.id.user_id);
            tvUserID.setText(String.format(Locale.UK, "%d", id));

            TextView tvCookie = findViewById(R.id.user_cookie);
            tvCookie.setText(cookie);

            TextView tvUsername = findViewById(R.id.user_username);
            tvUsername.setText(username);
        } else {
            setContentView(R.layout.activity_login);
            // Set up the login form.
            mUsernameView = findViewById(R.id.username);

            mPasswordView = findViewById(R.id.password);
            mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                    if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                    }
                    return false;
                }
            });

            mUsernameSignInButton = findViewById(R.id.username_sign_in_button);
            mUsernameSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });

            Button mRegisterButton = findViewById(R.id.username_register_button);
            mRegisterButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    register();

                }
            });

            mLoginFormView = findViewById(R.id.login_form);
            mProgressView = findViewById(R.id.login_progress);

        }

    }


    @Override
    public void onResponse(String response) {
        mUsernameSignInButton.setEnabled(true);
        Util.showProgress(mLoginFormView, mProgressView, false);
        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();

        try {
            JSONObject responseData = Util.parseJson(response);
            SharedPreferences sharedPref = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            assert responseData != null;
            editor.putString(getString(R.string.saved_user_cookie), responseData.getString("cookie"));
            editor.putInt(getString(R.string.saved_user_id), responseData.getInt("user_id"));
            Log.d("COOKIE -> ", responseData.getString("cookie"));


            UserSessionDetails.cookie = responseData.getString("cookie");
            UserSessionDetails.user_id = responseData.getInt("user_id");
            UserSessionDetails.user_name = responseData.getString("username");

            Log.d("USER_ID -> " , Integer.toString(responseData.getInt("user_id")));
            editor.apply();

            signIn();
        } catch (JSONException e) {
            Log.e("P2ServerE", "Could not parse malformed JSON: " + e.getMessage());
        }
    }


    @Override
    public void onErrorResponse(VolleyError ve) {
        mUsernameSignInButton.setEnabled(true);
        mPasswordView.setError(getString(R.string.error_incorrect_password));
        Util.showProgress(mLoginFormView, mProgressView, false);
        UserSessionDetails.user_name = null;
        if (ve.networkResponse != null) {
            VolleySingleton.LogNetworkError(ve);
            String error = new String(ve.networkResponse.data);
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
            mPasswordView.requestFocus();
        }
    }


    private void register() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }


    private void signIn() {
        // Para trabalhar com a Cloud Version trocar o intent... >_<
        Intent intent = new Intent(this, this.nextClass);
        finish();
        startActivity(intent);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        mUsernameSignInButton.setEnabled(false);

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && Util.isPasswordInvalid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
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

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            mUsernameSignInButton.setEnabled(true);
        } else {
            SharedPreferences sharedPref = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_username), username);
            editor.apply();

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Util.showProgress(mLoginFormView, mProgressView, true);
            UserSessionDetails.user_name = username;
            doVolleyRequest(username, password);
        }
    }

    private void doVolleyRequest(final String username, final String password) {
        VolleySingleton volley = VolleySingleton.getInstance(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, getString(R.string.url_login),
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
                return headers;
            }
        };
        volley.addToRequestQueue(request);
    }
}