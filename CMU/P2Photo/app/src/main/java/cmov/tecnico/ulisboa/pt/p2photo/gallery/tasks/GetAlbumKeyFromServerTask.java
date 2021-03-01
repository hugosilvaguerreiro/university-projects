package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxProvider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.Provider;


public class GetAlbumKeyFromServerTask extends AsyncTask<String, Void, String> {

    private String albumName;
    private Provider provider;
    private Callback callback;
    private Exception mException;

    public interface Callback {
        void onDataLoaded(String key);
        void onError(Exception e);
    }

    public GetAlbumKeyFromServerTask(String albumName, Provider provider, Callback callback){
        this.albumName = albumName;
        this.provider = provider;
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (mException != null) {
            callback.onError(mException);
        } else {
            callback.onDataLoaded(result);
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            return ((DropboxProvider)this.provider).getAlbumKeyFromServer(this.albumName);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "oopsie";
    }
}
