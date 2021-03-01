package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

public class RevokeDropboxTokenTask extends AsyncTask<Void, Void, Void> {

    private final DbxClientV2 mDbxClient;

    public RevokeDropboxTokenTask(DbxClientV2 dbxClientV2) {
        this.mDbxClient = dbxClientV2;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            mDbxClient.auth().tokenRevoke();
        } catch (DbxException e) {
            Log.e("RevokeDropboxTokenTask", e.getMessage());
        }
        return null;
    }
}
