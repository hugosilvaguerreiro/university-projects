package cmov.tecnico.ulisboa.pt.p2photo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.dropbox.core.v2.files.FolderMetadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.DropboxGalleryActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.AddUserToAlbumTask;

public class ListUsersActivity extends AppCompatActivity implements
        Response.Listener<String>, Response.ErrorListener {

    final static public int DEFAULT_ID = -1;
    final static public String DEFAULT_COOKIE = "cookie";
    private ListView mListView;
    private LinkedHashMap<String, Pair<Integer, String>> usersResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usersResult = new LinkedHashMap<>();
        setContentView(R.layout.activity_list_users);

        mListView = findViewById(R.id.userList);
        final TextInputEditText mSearchUserView = findViewById(R.id.searchUserText);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("ListUsersActivity" ,
                        "onItemClick: name : " + adapterView.getItemAtPosition(i) + "id : " +
                        usersResult.get(adapterView.getItemAtPosition(i)).toString());

                final String share_name = (String) adapterView.getItemAtPosition(i);
                final int share_id = usersResult.get(adapterView.getItemAtPosition(i)).first;
                final String share_pub = usersResult.get(adapterView.getItemAtPosition(i)).second;

                AlertDialog.Builder builder = new AlertDialog.Builder(ListUsersActivity.this);
                builder.setIcon(R.drawable.album3);
                builder.setTitle("Share an album with " + adapterView.getItemAtPosition(i) + " ?");
                builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent returnIntent = new Intent(ListUsersActivity.this, DropboxGalleryActivity.class);
                        returnIntent.putExtra("share_user_name", share_name);
                        returnIntent.putExtra("share_user_id", share_id);
                        returnIntent.putExtra("share_public_key", share_pub);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("ListUsersActivity" , "CANCEL");
                    }
                });
                builder.create().show();
            }
        });


        Button mSearchUserButton = findViewById(R.id.searchUserButton);
        mSearchUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchUser(mSearchUserView.getText().toString());
            }
        });
    }


    private void searchUser(final String query){
        VolleySingleton volley = VolleySingleton.getInstance(getApplicationContext());
        final SharedPreferences sharedPref = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
        final String id = Integer.toString(sharedPref.getInt("user_id", DEFAULT_ID));
        final String cookie = sharedPref.getString("user_cookie", DEFAULT_COOKIE);

        StringRequest request = new StringRequest(Request.Method.POST, getString(R.string.url_listusers),
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
                headers.put("query", query);
                return headers;
            }
        };
        volley.addToRequestQueue(request);
        Log.d("Request Parameters: ", id + " " + cookie + " " + query);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        VolleySingleton.LogNetworkError(error);
        if(error.networkResponse.statusCode == 422){
            Toast.makeText(getApplicationContext(), "Bad arguments", Toast.LENGTH_SHORT).show();
        } else if (error.networkResponse.statusCode == 403){
            Toast.makeText(getApplicationContext(), "Invalid Session Cookie. Please re-login.", Toast.LENGTH_LONG).show();
            Intent logout = new Intent(this, LoginActivity.class);
            startActivity(logout);
        }
    }

    @Override
    public void onResponse(String response) {
        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();

        ArrayList<String> userSearchResult = new ArrayList<>();
        try {
            JSONArray responseData = new JSONArray(response);
            int i;
            for (i=0; i < responseData.length(); i++)
            {
                try {
                    JSONObject userObject = responseData.getJSONObject(i);
                    // Pulling items from the array
                    String username = userObject.getString("user");
                    int id = userObject.getInt("id");
                    String pub = userObject.getString("pub");
                    usersResult.put(username, new Pair(id, pub));
                    userSearchResult.add(username);
                } catch (JSONException e) {
                    Log.e("P2PhotoE", "Could not parse malformed JSON: " + e.getMessage());
                }
            }
            Toast.makeText(getApplicationContext(), "Number of users found : " + Integer.toString(i), Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Log.e("P2ServerE", "Could not parse malformed JSON: " + e.getMessage());
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, userSearchResult);
        mListView.setAdapter(adapter);
    }
}
