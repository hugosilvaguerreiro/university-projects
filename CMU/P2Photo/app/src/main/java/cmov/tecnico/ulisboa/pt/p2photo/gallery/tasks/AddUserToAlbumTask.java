package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.VolleySingleton;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.GalleryBaseActivity;
import cmov.tecnico.ulisboa.pt.p2photo.security.SecurityHelper;

public class AddUserToAlbumTask extends AsyncTask<Integer, Void, Void> {

    private final Callback mCallback;
    private final Context mContext;

    private String publicKey;
    private SecretKey albumKey;

    public void setAlbumKey(SecretKey albumKey){
        this.albumKey = albumKey;
    }

    public void setPublicKey(String publicKey){
        this.publicKey = publicKey;
    }


    public interface Callback {
        void onDataLoaded(String result);
        void onError(Exception e);
    }

    public AddUserToAlbumTask(Context ctx, Callback callback){
        this.mCallback = callback;
        this.mContext = ctx;
    }

    private void addUserToAlbum(final int userId, final int albumId){

        // Encrypting albumkey with other users public key
        String encryptedAlbumKey = "placeholder";
        if(this.albumKey != null) {
            String encodedAlbumKey= Base64.encodeToString(this.albumKey.getEncoded(), Base64.DEFAULT);
            encryptedAlbumKey = null;
            byte[] encryptedAlbumKeyBytes = null;
            try {
                encryptedAlbumKeyBytes = SecurityHelper.encryptRSAShare(encodedAlbumKey, this.publicKey);
                encryptedAlbumKey = Base64.encodeToString(encryptedAlbumKeyBytes, Base64.NO_WRAP);
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                    IOException | NoSuchPaddingException | InvalidKeyException | BadPaddingException |
                    IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }


        VolleySingleton volley = VolleySingleton.getInstance(mContext);

        Response.Listener<String> addUserToAlbumListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(AddUserToAlbumTask.this.mContext, "Shared album successful", Toast.LENGTH_SHORT).show();
                //Log.d("addUserToAlbumListener", response);
            }
        };

        Response.ErrorListener addUserToAlbumErrorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleySingleton.LogNetworkError(error);
                Toast.makeText(AddUserToAlbumTask.this.mContext, "Error sharing album", Toast.LENGTH_SHORT).show();
            }
        };

        SharedPreferences sharedPref = mContext.getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
        final String id = Integer.toString(sharedPref.getInt("user_id", GalleryBaseActivity.DEFAULT_ID));
        final String cookie = sharedPref.getString("user_cookie", GalleryBaseActivity.DEFAULT_COOKIE);
        final String shareKey = encryptedAlbumKey;


        StringRequest request = new StringRequest(Request.Method.POST, mContext.getString(R.string.url_add_user_to_album),
                addUserToAlbumListener, addUserToAlbumErrorListener)
        {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getParams() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("usr_id", id);
                headers.put("cookie", cookie);
                headers.put("n_usr_id", Integer.toString(userId));
                headers.put("alb_id", Integer.toString(albumId));
                headers.put("secret", shareKey);
                return headers;
            }
        };
        volley.addToRequestQueue(request);


    }

    @Override
    protected Void doInBackground(Integer... integers) {
        Log.d("AddUserToAlbumTask", "DoInBackground arguments: " + integers[0] + "::" + integers[1] );
        addUserToAlbum(integers[0], integers[1]);
        return null;
    }
}
