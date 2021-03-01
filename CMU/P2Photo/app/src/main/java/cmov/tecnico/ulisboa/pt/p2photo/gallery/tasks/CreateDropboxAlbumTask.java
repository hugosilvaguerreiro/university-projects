package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.os.AsyncTask;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import cmov.tecnico.ulisboa.pt.p2photo.UserSessionDetails;
import cmov.tecnico.ulisboa.pt.p2photo.security.SecurityHelper;

public class CreateDropboxAlbumTask extends AsyncTask<String, Void, FolderMetadata> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;
    private SecretKey albumKey;

    public void setAlbumKey(SecretKey albumKey) {
        this.albumKey = albumKey;
    }

    public interface Callback {
        void onDataLoaded(FolderMetadata result);
        void onError(Exception e);
    }

    public CreateDropboxAlbumTask(DbxClientV2 dbxClientV2, Callback callback) {
        this.mDbxClient = dbxClientV2;
        this.mCallback = callback;
    }

    @Override
    protected void onPostExecute(FolderMetadata result) {
        super.onPostExecute(result);

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(result);
        }
    }

    @Override
    protected FolderMetadata doInBackground(String... strings) {
        try {
            CreateFolderResult folder = mDbxClient.files().createFolderV2(strings[0]);

            InputStream in = new ByteArrayInputStream("[]".getBytes());
            mDbxClient.files().uploadBuilder(strings[0] + "/catalog.txt")
                    .withMode(WriteMode.ADD)
                    .uploadAndFinish(in);
            return folder.getMetadata();

        } catch (DbxException e) {
            mException = e;
        } catch (IOException e) {
            mException = e;
        }
        return null;
    }
}
