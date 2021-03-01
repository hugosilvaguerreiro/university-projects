package cmov.tecnico.ulisboa.pt.p2photo.p2lib.cloud;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.users.FullAccount;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.DropboxGalleryActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.GalleryBaseActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.LocalPhotosActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxClientFactory;


/**
 * Activity that shows information about the currently logged in user
 */
public class UserActivity extends DropboxActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user);

        Button loginButton = (Button) findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.startOAuth2Authentication(UserActivity.this, getString(R.string.app_key));
            }
        });

        Button filesButton = (Button) findViewById(R.id.files_button);
        filesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(FilesActivity.getIntent(UserActivity.this, ""));
                //startActivity(GalleryBaseActivity.getIntent(UserActivity.this, LocalPhotosActivity.class, null));
                startActivity(GalleryBaseActivity.getIntent(UserActivity.this, DropboxGalleryActivity.class, null));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d("onResume -> Token?" , Boolean.toString(hasToken()));


        if (hasToken()) {
            findViewById(R.id.login_button).setVisibility(View.GONE);
            findViewById(R.id.email_text).setVisibility(View.VISIBLE);
            findViewById(R.id.name_text).setVisibility(View.VISIBLE);
            findViewById(R.id.type_text).setVisibility(View.VISIBLE);
            findViewById(R.id.files_button).setEnabled(true);
        } else {
            findViewById(R.id.login_button).setVisibility(View.VISIBLE);
            findViewById(R.id.email_text).setVisibility(View.GONE);
            findViewById(R.id.name_text).setVisibility(View.GONE);
            findViewById(R.id.type_text).setVisibility(View.GONE);
            findViewById(R.id.files_button).setEnabled(false);
        }
    }

    @Override
    protected void loadData() {
        new GetCurrentAccountTask(DropboxClientFactory.getClient(), new GetCurrentAccountTask.Callback() {
            @Override
            public void onComplete(FullAccount result) {
                ((TextView) findViewById(R.id.email_text)).setText(result.getEmail());
                ((TextView) findViewById(R.id.name_text)).setText(result.getName().getDisplayName());
                ((TextView) findViewById(R.id.type_text)).setText(result.getAccountType().name());
            }

            @Override
            public void onError(Exception e) {
                Log.e(getClass().getName(), "Failed to get account details.", e);
            }
        }).execute();
    }


    protected boolean hasToken() {
        SharedPreferences prefs = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        if(accessToken != null){
            Log.d("hasToken", accessToken);
        }
        return accessToken != null;
    }
}
