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

public class CreateAlbumTask extends AsyncTask<String, Void, Boolean> {


    private Provider provider;
    private Url url;
    private String albumName;
    private Callback callback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(Boolean result);
        void onError(Exception e);
    }

    public CreateAlbumTask(Provider provider, Url url, String albumName, Callback callback) {

        this.provider = provider;
        this.url = url;
        this.albumName = albumName;
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
        return this.provider.registerNewAlbum(this.url, this.albumName);
    }
}
