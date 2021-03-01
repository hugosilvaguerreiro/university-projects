package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.filerequests.FileRequest;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cmov.tecnico.ulisboa.pt.p2photo.Util;
import cmov.tecnico.ulisboa.pt.p2photo.VolleySingleton;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxClientFactory;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.AlbumSlice;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;


public class DownloadFileFromDropboxTask extends AsyncTask<Url, Void, File> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;
    private File outputLocation;
    private Boolean isSharedLink;
    private VolleySingleton volleySingle;

    public interface Callback {
        void onDataLoaded(File output);

        void onError(Exception e);
    }

    public DownloadFileFromDropboxTask(File outputLocation, DbxClientV2 dbxClientV2, Callback callback){
        this.mDbxClient = dbxClientV2;
        this.mCallback = callback;
        this.outputLocation = outputLocation;
        this.isSharedLink = false;

    }
    public DownloadFileFromDropboxTask(File outputLocation, DbxClientV2 dbxClientV2, Callback callback, Boolean sharedLink, VolleySingleton single){
        this.mDbxClient = dbxClientV2;
        this.mCallback = callback;
        this.outputLocation = outputLocation;
        this.isSharedLink = sharedLink;
        this.volleySingle = single;
    }

    @Override
    protected File doInBackground(Url... url) {
        try
        {
            //output file for download --> storage location on local system to download file
            OutputStream downloadFile = new FileOutputStream(this.outputLocation);
            try
            {
                if(!isSharedLink) {
                    FileMetadata metadata = this.mDbxClient.files().downloadBuilder(url[0].url)
                            .download(downloadFile);
                } else {
                        //todo refactor this, we dont need this anymore I think

                    }
            }
            finally
            {
                downloadFile.close();
            }
        }
        //exception handled
        catch (DbxException e)
        {
            //error downloading file
            e.printStackTrace();
            Log.d("CatalogDropboxTask", "Unable to download file to local system\n Error: " + e);
        }
        catch (IOException e)
        {
            Log.d("CatalogDropboxTask", "Unable to download file to local system\n Error: " + e);
            //error downloading file
        }
        return this.outputLocation;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(result);
        }
    }
}


