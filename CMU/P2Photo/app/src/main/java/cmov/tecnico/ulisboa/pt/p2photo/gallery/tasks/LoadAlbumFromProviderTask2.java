package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.os.AsyncTask;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxProvider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.Provider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;

public class LoadAlbumFromProviderTask2 extends AsyncTask<Album, Void, Album> {
    private Exception mException;
    private String path;
    private Provider provider;
    private Callback mCallback;

    public interface Callback {
        void onDataLoaded(Album album);

        void onError(Exception e);
    }
    public LoadAlbumFromProviderTask2(Provider provider, Callback callback) {
        this.provider = provider;
        this.mCallback = callback;
    }

    @Override
    protected Album doInBackground(Album... albums) {
        return ((DropboxProvider)this.provider).getAlbum(albums[0].url);
    }

    @Override
    protected void onPostExecute(Album result) {
        super.onPostExecute(result);

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(result);
        }
    }

}
