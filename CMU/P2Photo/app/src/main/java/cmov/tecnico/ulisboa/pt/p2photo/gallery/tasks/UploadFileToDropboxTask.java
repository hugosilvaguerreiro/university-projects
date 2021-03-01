package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;


public class UploadFileToDropboxTask  extends AsyncTask<Url, Void, Void> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;
    private File inputLocation;

    public interface Callback {
        void onDataLoaded();

        void onError(Exception e);
    }

    public UploadFileToDropboxTask(File inputLocation, DbxClientV2 dbxClientV2, Callback callback){
        this.mDbxClient = dbxClientV2;
        this.mCallback = callback;
        this.inputLocation = inputLocation;
    }

    @Override
    protected Void doInBackground(Url... url) {

        // Note - this is not ensuring the name is a valid dropbox file name
        String remoteFileName = inputLocation.getName();

        try (InputStream inputStream = new FileInputStream(inputLocation)) {
            mDbxClient.files().uploadBuilder(url[0].url+ "/" + remoteFileName)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded();
        }
    }
}


