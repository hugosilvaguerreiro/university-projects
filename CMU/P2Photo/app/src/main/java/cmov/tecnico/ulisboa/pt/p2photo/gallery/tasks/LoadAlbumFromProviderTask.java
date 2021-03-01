package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.os.AsyncTask;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.Provider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;

public class LoadAlbumFromProviderTask extends AsyncTask<Album, Void, Album> {
    private Exception mException;
    private String path;
    private Provider provider;
    private Callback mCallback;

    public interface Callback {
        void onDataLoaded(Album album);

        void onError(Exception e);
    }
    public LoadAlbumFromProviderTask(Provider provider, Callback callback) {
        this.provider = provider;
        this.mCallback = callback;
    }

    @Override
    protected Album doInBackground(Album... albums) {
        return this.provider.getAlbumService(albums[0].url);
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
