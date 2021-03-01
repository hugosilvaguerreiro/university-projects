package cmov.tecnico.ulisboa.pt.p2photo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxClientFactory;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.RevokeDropboxTokenTask;

public class LogoutActivity extends AppCompatActivity implements
        Response.Listener<String>, Response.ErrorListener {

    final static private int DEFAULT_ID = -1;
    final static private String DEFAULT_COOKIE = "cookie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Logout", "onCreate");
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_logout);
        logout();
    }

    private void logout(){
        SharedPreferences sharedPref = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
        VolleySingleton volley = VolleySingleton.getInstance(getApplicationContext());
        final String id = Integer.toString(sharedPref.getInt("user_id", DEFAULT_ID));
        final String cookie = sharedPref.getString("user_cookie", DEFAULT_COOKIE);

        Log.d("Logout", "Sending logout request");

        StringRequest request = new StringRequest(Request.Method.POST, getString(R.string.url_logout),
                this, this)
        {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getParams() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("usr_id", id);
                headers.put("cookie", cookie);
                return headers;
            }
        };
        volley.addToRequestQueue(request);
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        VolleySingleton.LogNetworkError(error);
    }

    @Override
    public void onResponse(String response) {
        SharedPreferences sharedPref = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
        Log.d("Logout Response", response);
        if (response.equals("ok")){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_user_cookie), DEFAULT_COOKIE);
            editor.putInt(getString(R.string.saved_user_id), DEFAULT_ID);
            editor.apply();

            Log.d("onLogout", "cookie: " + sharedPref.getString("user_cookie", DEFAULT_COOKIE));
            Log.d("onLogout", "user_id: " + sharedPref.getInt("user_id", DEFAULT_ID));
            Intent loginIntent = new Intent(LogoutActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }
}
