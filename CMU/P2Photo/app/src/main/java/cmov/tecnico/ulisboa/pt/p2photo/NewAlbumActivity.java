package cmov.tecnico.ulisboa.pt.p2photo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewAlbumActivity extends AppCompatActivity {

    private EditText mAlbumName;
    private Button mNewAlbumButton;
    private View mProgressView;
    private View mNewAlbumFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_album);

        mAlbumName = findViewById(R.id.new_album_name_text);

        mNewAlbumButton = findViewById(R.id.new_album_button);
        mNewAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptNewAlbum();
            }
        });

        mProgressView = findViewById(R.id.new_album_progress);
        mNewAlbumFormView = findViewById(R.id.new_album_form);
    }

    private void attemptNewAlbum() {
        mNewAlbumButton.setEnabled(false);

        mAlbumName.setError(null);

        String albumName = mAlbumName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(albumName) && Util.isUsernameInvalid(albumName)) {
            mAlbumName.setError(getString(R.string.error_inalid_album_name));
            focusView = mAlbumName;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            mNewAlbumButton.setEnabled(true);
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Util.showProgress(mNewAlbumFormView, mProgressView, true);
        }

    }


}
