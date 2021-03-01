package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxClientFactory;


public class GetSharedUrlTask extends AsyncTask<String, Void, String> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;
    private String path;

    public interface Callback {
        void onDataLoaded(String url);

        void onError(Exception e);
    }

    public GetSharedUrlTask(DbxClientV2 dbxClientV2, Callback callback, String path){
        this.mDbxClient = dbxClientV2;
        this.mCallback = callback;
        this.path = path;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(result);
        }
    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            SharedLinkMetadata sharedLinkMetadata = DropboxClientFactory.getClient().sharing().createSharedLinkWithSettings(this.path);
            return sharedLinkMetadata.getUrl();
        } catch (CreateSharedLinkWithSettingsErrorException ex) {
            ex.printStackTrace();
            mException = ex;
        } catch (DbxException ex) {
            ex.printStackTrace();
            mException = ex;
        }
        return null;
    }

}
