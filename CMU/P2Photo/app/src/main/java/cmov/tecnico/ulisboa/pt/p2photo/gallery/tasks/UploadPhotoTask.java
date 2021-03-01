package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;

import java.io.File;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxProvider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.Provider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;

/**
 * Async task to list items in a folder
 */

public class UploadPhotoTask extends AsyncTask<String, Void, Boolean> {

    private Exception mException;
    private Provider provider;
    private File local;
    private Album album;
    private Url url;
    private Callback callback;

    public interface Callback {
        void onDataLoaded(Boolean result);
        void onError(Exception e);
    }

    public UploadPhotoTask(Provider provider, File local, final Album album, final Url url, Callback callback) {
        this.provider = provider;
        this.local = local;
        this.album = album;
        this.url = url;
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        if (mException != null) {
            callback.onError(mException);
        } else {
            callback.onDataLoaded(result);
        }
    }
    @Override
    protected Boolean doInBackground(String... params) {
        return this.provider.createPhotoRefactored(this.local, this.album, this.url);
    }
}
