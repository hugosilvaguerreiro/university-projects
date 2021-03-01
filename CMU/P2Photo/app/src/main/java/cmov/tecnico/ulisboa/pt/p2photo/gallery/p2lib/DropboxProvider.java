package cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.CreateSharedLinkWithSettingsErrorException;
import com.dropbox.core.v2.sharing.ListSharedLinksResult;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.UserSessionDetails;
import cmov.tecnico.ulisboa.pt.p2photo.Util;
import cmov.tecnico.ulisboa.pt.p2photo.VolleySingleton;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.AlbumSlice;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.CachedPhoto;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.DropboxAlbum;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Photo;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.CreateDropboxAlbumTask;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.DownloadFileFromDropboxTask;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.GetSharedUrlTask;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.UploadFileToDropboxTask;
import cmov.tecnico.ulisboa.pt.p2photo.security.SecurityHelper;

public class DropboxProvider extends Provider {

    final static private int DEFAULT_ID = -1;
    final static private String DEFAULT_COOKIE = "cookie";

    private Map<String, Album> albums;
    public Observer observer;

    public DropboxProvider(Context ctx, Observer observer){
        super(ctx);
        this.albums = new HashMap<>();
        this.observer = observer;
    }
    //================================================================================
    // INITIALIZE GALLERY RELATED METHODS
    //================================================================================
    public boolean initializeGalleryRefactored() {
        try {
            JSONArray responseData = Util.parseJsonArray(listUserAlbumRequest());

            for (int i = 0; i < responseData.length(); i++) {
                JSONObject album = responseData.getJSONObject(i);
                ArrayList<AlbumSlice> slices = new ArrayList<>();
                JSONArray serverSlices = album.getJSONArray("slices");

                for(int j = 0; j < serverSlices.length();j++) {
                    JSONObject slice = serverSlices.getJSONObject(j);
                    checkForNullSlices(album, slice, slices);
                }
                initializeAlbum(album, slices);
            }
            DropboxProvider.this.observer.update(DropboxProvider.this, null);
            return true;
        } catch (ExecutionException | InterruptedException | JSONException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public String listUserAlbumRequest() throws ExecutionException, InterruptedException {
        VolleySingleton volley = VolleySingleton.getInstance(ctx);
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, ctx.getString(R.string.url_list_user_album), future, future){
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("usr_id", Integer.toString(UserSessionDetails.user_id));
                params.put("cookie", UserSessionDetails.cookie);
                return  params;
            }
        };
        volley.addToRequestQueue(request);
        return future.get();
    }

    private void initializeAlbum(JSONObject album, ArrayList<AlbumSlice> slices) throws JSONException, IOException {

        String album_name = album.getString("name");

        int album_id = album.getInt("id");

        DropboxAlbum album1 = new DropboxAlbum(slices, new Url("/"+album_name), album_name, album_id);
        checkForAvailableLocalPhotos(album1);
        File f = new File(ctx.getFilesDir(), album_name);
        f.mkdirs();
        File catalog = new File(ctx.getFilesDir(), album_name+"/"+"catalog.txt");
        Util.writeStringToFile(catalog, "[]");
        DropboxProvider.this.albums.put("/"+album_name, album1);
    }

    private void checkForAvailableLocalPhotos(Album album) {
        File localAlbum = new File(ctx.getFilesDir(), album.name);
        File[] photos = localAlbum.listFiles();
        if(photos == null) {
            localAlbum.mkdirs();
        } else {
            for(File photo : photos) {
                if(!photo.getName().equals("catalog.txt")) {
                    CachedPhoto newPhoto = new CachedPhoto(null, new Url(photo.getName()), photo);
                    album.addPhoto(photo.getName(), newPhoto);
                }
            }
        }

    }

    private void checkForNullSlices(final JSONObject album, JSONObject slice, ArrayList<AlbumSlice> slices) throws JSONException, ExecutionException, InterruptedException {
        //Check if someone added me to an album and create the url.
        if(slice.getString("slice_url").equals("null")
                && slice.getInt("user") == UserSessionDetails.user_id) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        createAlbumOnDropbox(album.getString("name"));
                        String sharedLink = getSharedLink("/" + album.getString("name") + "/" + "catalog.txt");
                        registerSharedLinkOnCentralServer(album.getInt("id"), sharedLink);
                    } catch (JSONException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();

        } else {
            slices.add(new AlbumSlice(slice.getInt("user"), new Url(slice.getString("slice_url"))));
        }
    }


    //TODO REMOVE THIS
    private void processAlbumResponse(String response) {
        JSONArray responseData = Util.parseJsonArray(response);
        for (int i = 0; i < responseData.length(); i++) {
            try {
                JSONObject album = responseData.getJSONObject(i);
                ArrayList<AlbumSlice> slices = new ArrayList<>();
                JSONArray serverSlices = album.getJSONArray("slices");
                for(int j = 0; j < serverSlices.length();j++) {
                    JSONObject slice = serverSlices.getJSONObject(j);

                    //Check if someone added me to an album and create the url
                    if(slice.getString("slice_url").equals("null")
                            && slice.getInt("user") == UserSessionDetails.user_id) {
                        createAlbumSlice(album.getString("name"), album.getInt("id"));
                    }else {
                        slices.add(new AlbumSlice(slice.getInt("user"), new Url(slice.getString("slice_url"))));
                    }

                }
                String album_name = album.getString("name");
                int album_id = album.getInt("id");

                DropboxAlbum album1 = new DropboxAlbum(slices, new Url("/"+album_name), album_name, album_id);

                File f = new File(ctx.getFilesDir(), album_name);
                f.mkdirs();
                File catalog = new File(ctx.getFilesDir(), album_name+"/"+"catalog.txt");
                Util.writeStringToFile(catalog, "[]");
                DropboxProvider.this.albums.put("/"+album_name, album1);
                DropboxProvider.this.observer.update(DropboxProvider.this, null);

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //TODO REMOVE THIS
    @Override
    public void initializeGallery() {

        VolleySingleton volley = VolleySingleton.getInstance(ctx);
        SharedPreferences sharedPref = ctx.getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
        final String id = Integer.toString(sharedPref.getInt("user_id", DEFAULT_ID));
       final String cookie = sharedPref.getString("user_cookie", DEFAULT_COOKIE);
       StringRequest postRequest = new StringRequest(Request.Method.POST, ctx.getString(R.string.url_list_user_album),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                DropboxProvider.this.processAlbumResponse(response);
                            }
                        });
                        t.start();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int duration = Toast.LENGTH_SHORT;

                        Toast t = Toast.makeText(ctx, "Error creating album", duration);
                        t.show();
                        error.printStackTrace();
                        VolleySingleton.LogNetworkError(error);
                    }
                }
            ){
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("usr_id", id);
                params.put("cookie", cookie);

                return  params;
            }
        };
        volley.addToRequestQueue(postRequest);
    }

    public void printAlbumInfo(){
        for(Album al : this.albums.values()){
            System.out.println("Album name -> " + al.name + " ## Album ID -> " + al.album_id + " ## Album url" + al.url.url);
        }
    }

    public int getAlbumIdByName(String albumName){
        for(Album al : albums.values()){
            if(al.name.equals(albumName)){
                return al.album_id;
            }
        }
        return -1;
    }

    public String getAlbumNamebyId(int albumId){
        for(Album al : albums.values()){
            if(al.album_id == albumId){
                return al.name;
            }
        }
        return null;
    }

    //================================================================================
    // LOAD ALBUM RELATED METHODS //TODO
    //================================================================================

    @Override
    public List<Album> getAlbums() {
        return new ArrayList<>(this.albums.values());
    }

    @Override
    public Album getAlbum(final Url url) {
        Album album = this.albums.get(url.url);
        final String albumName = album.name;
        File localAlbum = new File(DropboxProvider.this.ctx.getFilesDir(), albumName);
        File[] photos = localAlbum.listFiles();

        for(File photo : photos) {
            if(!photo.getName().equals("catalog.txt")) {
                CachedPhoto newPhoto = new CachedPhoto(null, new Url(photo.getName()), photo);
                album.addPhoto(photo.getName(), newPhoto);
            }
        }
        //DropboxProvider.this.observer.update(DropboxProvider.this, null);
        return album;
    }


    public Album getAlbumService(final Url url) {
        final DbxClientV2 client = DropboxClientFactory.getClient();
        final DropboxAlbum album = (DropboxAlbum) this.albums.get(url.url);

        final String albumName = this.albums.get(url.url).name;
        for(final AlbumSlice slice : this.albums.get(url.url).albumSlices) {
            File outputFile = new File(ctx.getFilesDir(), url.url +"/"+"catalog_"+slice.url+".txt");

            StringRequest getRequest = new StringRequest(Request.Method.GET,slice.url.url+"&raw=1",
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(final String response) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    getAlbumServiceOnResponse(album, response);
                                }
                            });
                            t.start();
                        }
                    },
                    new Response.ErrorListener()
                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response","");
                        }
                    }
            );
            VolleySingleton.getInstance(this.ctx).addToRequestQueue(getRequest);
        }
        return album;
    }

    private void getAlbumServiceOnResponse(final Album album, String response) {
        int nrOfPhotosDownload = 0;
        String b = null;
        String albumSecret = null;
        try {
            albumSecret = getAlbumKeyFromServer(album.name);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        String albumEncodedKey = null;
        try {
            albumEncodedKey = SecurityHelper.decryptRSA(albumSecret, "rsa_" + UserSessionDetails.user_name);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                IllegalBlockSizeException | UnrecoverableKeyException | InvalidKeyException |
                IOException | NoSuchPaddingException | BadPaddingException e) {

            e.printStackTrace();
        }
        byte[] albumDecodedKey = Base64.decode(albumEncodedKey, Base64.NO_WRAP);


        SecretKey albumKey = new SecretKeySpec(albumDecodedKey, 0, albumDecodedKey.length,
                KeyProperties.KEY_ALGORITHM_AES);

        String decryptedContent = null;
        byte[] decodedContent = null;
            Log.d("DROPBOXPROVIDER" , response);
        try {
            decryptedContent = SecurityHelper.decryptGCM(response,
                    albumKey);
            decodedContent = Base64.decode(decryptedContent, Base64.DEFAULT);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                IOException | InvalidKeyException | UnrecoverableEntryException |
                IllegalBlockSizeException | NoSuchPaddingException |
                InvalidAlgorithmParameterException | BadPaddingException e) {

            e.printStackTrace();
            return;
        }
        JSONArray array = Util.parseJsonArray(new String(decodedContent, StandardCharsets.UTF_8));
        if (array != null && array.length() > 0) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    final JSONObject photo_url = array.getJSONObject(i);
                    final String photoName = photo_url.getString("name");
                    if(!album.containsLocalPhoto(photoName)) {
                        nrOfPhotosDownload ++;
                    ImageRequest getRequest = new ImageRequest(photo_url.getString("url") + "&raw=1", new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(final Bitmap response) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    processDownloadedSharedPhoto(album, photoName, response);
                                }
                            });
                            t.start();

                        }
                    }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            });

                    VolleySingleton.getInstance(DropboxProvider.this.ctx).addToRequestQueue(getRequest);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        final int nr = nrOfPhotosDownload;
        ((Activity)ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(nr == 0)
                    Toast.makeText(ctx, "There are no photos to download" , Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(ctx, "Started downloading "+ Integer.toString(nr)+ " photos" , Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void processDownloadedSharedPhoto(Album album, final String photoName, Bitmap response) {
        File out = new File(DropboxProvider.this.ctx.getFilesDir(), album.name+"/"+photoName);
        try {
            FileOutputStream outs = new FileOutputStream(out);
            response.compress(Bitmap.CompressFormat.PNG, 50, outs);

            CachedPhoto newPhoto = new CachedPhoto(null, new Url(photoName),out);
            album.addPhoto(photoName, newPhoto);
            DropboxProvider.this.observer.update(DropboxProvider.this, null);

            //TODO Maybe delete this
            ((Activity)ctx).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ctx, "Download "+ photoName + " finished" , Toast.LENGTH_SHORT).show();
                }
            });


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    //================================================================================
    // CREATE ALBUM RELATED METHODS
    //================================================================================

    @Override
    public void createAlbum(Url url, final String albumName) {
        String b = null;
        String albumSecret = null;
        try {
            albumSecret = getAlbumKeyFromServer(albumName);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        String albumEncodedKey = null;
        try {
            albumEncodedKey = SecurityHelper.decryptRSA(albumSecret
                    , "rsa_" + UserSessionDetails.user_name);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                IOException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException | UnrecoverableKeyException | InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] albumDecodedKey = Base64.decode(albumEncodedKey, Base64.NO_WRAP);

        SecretKey albumKey = new SecretKeySpec(albumDecodedKey, 0, albumDecodedKey.length,
                KeyProperties.KEY_ALGORITHM_AES);


        VolleySingleton volley = VolleySingleton.getInstance(ctx);
        StringRequest postRequest = new StringRequest(Request.Method.POST, ctx.getString(R.string.url_create_album),
                new CreateAlbumResponseListener(url, albumKey),
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int duration = Toast.LENGTH_SHORT;
                        Toast t = Toast.makeText(ctx, "Error creating album", duration);
                        t.show();
                        error.printStackTrace();
                        VolleySingleton.LogNetworkError(error);
                    }
                }
        ){
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                //params.put("name", "Alif");
                //params.put("domain", "http://itsalif.info");
                params.put("usr_id", String.valueOf(UserSessionDetails.user_id));
                params.put("cookie", UserSessionDetails.cookie);
                params.put("name", albumName);
                return params;
            }
        };
        volley.addToRequestQueue(postRequest);
    }

    @Override
    public boolean registerNewAlbum(Url url, final String albumName) {
        try {

            // Generate album secret key
            String encodedSecret = null;
            try {
                encodedSecret = SecurityHelper.generateAlbumKey(albumName);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            assert encodedSecret != null;
            Log.d("DropboxProvider" , "PUBLICKEYALIAS -> " + "rsa_" + UserSessionDetails.user_name);
            String encryptedSecret = Base64.encodeToString(
                    SecurityHelper.encryptRSA(encodedSecret,
                            "rsa_" + UserSessionDetails.user_name), Base64.NO_WRAP);

            //Register the new album on the central server
            JSONObject result = Util.parseJson(createAlbumOnCentralServer(albumName, encryptedSecret));

            //Create the album on dropbox
            boolean createAlbumResult = createAlbumOnDropbox(albumName);
            String sharedLink = getSharedLink("/" + albumName + "/" + "catalog.txt");

            registerSharedLinkOnCentralServer(result.getInt("id"), sharedLink);
            //Create the album locally
            if(createAlbumResult && sharedLink != null) {
                createAlbumLocally(result.getInt("id"), albumName);
            }
            return createAlbumResult && sharedLink != null;

        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
            return false;
        } catch (IOException | KeyStoreException | CertificateException |
                NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException e) {
            Log.d("DropboxProvider", "Error while handling album key");
            e.printStackTrace();
            return false;
        }
    }

    private boolean createAlbumOnDropbox(String albumName) {
        DbxClientV2 client = DropboxClientFactory.getClient();
        try {
            CreateFolderResult folder = client.files().createFolderV2("/test_java_createFolder");
        } catch (CreateFolderErrorException e) {
            if (!(e.errorValue.isPath() && e.errorValue.getPathValue().isConflict())) {
                //The folder already exists
                return false;
            }
        } catch (DbxException e) {
            e.printStackTrace();
            return false;
        }
        String b = null;
        String albumSecret = null;
        try {
            albumSecret = getAlbumKeyFromServer(albumName);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("CreateDropboxAlbumTask", String.valueOf(albumSecret.length()));

        String albumEncodedKey = null;
        try {
            albumEncodedKey = SecurityHelper.decryptRSA(albumSecret, "rsa_" + UserSessionDetails.user_name);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                IOException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException | UnrecoverableKeyException | InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] albumDecodedKey = Base64.decode(albumEncodedKey, Base64.NO_WRAP);
        SecretKey albumKey = new SecretKeySpec(albumDecodedKey, 0, albumDecodedKey.length,
                KeyProperties.KEY_ALGORITHM_AES);

        b = "[]";

        try {
            b = SecurityHelper.encryptGCM(b, albumKey);
        } catch (KeyStoreException | NoSuchPaddingException | NoSuchAlgorithmException |
                IOException | CertificateException | UnrecoverableEntryException |
                InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }


        InputStream in = new ByteArrayInputStream(b.getBytes(StandardCharsets.UTF_8));
        try {
            client.files().uploadBuilder("/" + albumName + "/catalog.txt")
                    .withMode(WriteMode.ADD)
                    .uploadAndFinish(in);
            return true;
        } catch (DbxException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String createAlbumOnCentralServer(final String albumName, final String secret) throws ExecutionException, InterruptedException {
        VolleySingleton volley = VolleySingleton.getInstance(ctx);

        RequestFuture<String> future = RequestFuture.newFuture();
        //JsonObjectRequest request = new JsonObjectRequest(Method.POST, SIGNUP_URL, reqBody, future, future)
        StringRequest request = new StringRequest(Request.Method.POST, ctx.getString(R.string.url_create_album), future, future) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("usr_id", String.valueOf(UserSessionDetails.user_id));
                params.put("cookie", UserSessionDetails.cookie);
                params.put("name", albumName);
                params.put("secret", secret);
                return params;
            }
        };
        volley.addToRequestQueue(request);
        return future.get();
    }

    public void registerSharedLinkOnCentralServer(final int album_id, final String sharedLink) throws ExecutionException, InterruptedException {
        VolleySingleton volley = VolleySingleton.getInstance(ctx);
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest postRequest = new StringRequest(Request.Method.POST, ctx.getString(R.string.url_create_slice),future, future) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("usr_id", String.valueOf(UserSessionDetails.user_id));
                params.put("cookie", UserSessionDetails.cookie);
                params.put("alb_id", String.valueOf(album_id));
                params.put("url", sharedLink);
                return params;
            }
        };
        volley.addToRequestQueue(postRequest);
        future.get();
    }

    private void createAlbumLocally(int albumId, String albumName) {
        try {
            AlbumSlice albumSlice = new AlbumSlice(UserSessionDetails.user_id, new Url(albumName + "/catalog.txt"));
            ArrayList<AlbumSlice> albumSlices = new ArrayList<>();
            albumSlices.add(albumSlice);
            (new File(ctx.getFilesDir(), albumName)).mkdirs();
            Util.writeStringToFile(new File(ctx.getFilesDir(), albumName+"/"+"catalog.txt"), "[]");

            Album album = new DropboxAlbum(albumSlices, new Url("/"+albumName), albumName, albumId);
            DropboxProvider.this.albums.put(album.url.url, album);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //================================================================================
    // CREATE ALBUM SLICE RELATED METHODS //TODO
    //================================================================================

    @Override
    public AlbumSlice getAlbumSlice(Album album, int user_id ) {
        for(AlbumSlice slice : album.albumSlices) {
            System.out.println(slice.user_id);
            if(slice.user_id == user_id) {
                return slice;
            }
        }
        return  null;
    }

    public void createAlbumSlice(final String albumName, final int albumId) {

        String b = null;
        String albumSecret = null;
        try {
            albumSecret = getAlbumKeyFromServer(albumName);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        String albumEncodedKey = null;
        try {
            albumEncodedKey = SecurityHelper.decryptRSA(albumSecret
                    , "rsa_" + UserSessionDetails.user_name);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                IOException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException | UnrecoverableKeyException | InvalidKeyException e) {
            e.printStackTrace();
        }

        byte[] albumDecodedKey = Base64.decode(albumEncodedKey, Base64.NO_WRAP);

        SecretKey albumKey = new SecretKeySpec(albumDecodedKey, 0, albumDecodedKey.length,
                KeyProperties.KEY_ALGORITHM_AES);


        /* This is used when the album already exists */
        final DbxClientV2 client = DropboxClientFactory.getClient();

        //Create a dropbox folder with the name album name on the users own dropbox and a catalog file inside
        CreateDropboxAlbumTask task = new CreateDropboxAlbumTask(client, new CreateDropboxAlbumTask.Callback() {
            @Override
            public void onDataLoaded(FolderMetadata result) {
                //After the creation of the folder, obtain the sharable link
                GetSharedUrlTask sharedLink = new GetSharedUrlTask(DropboxClientFactory.getClient(), new GetSharedUrlTask.Callback() {
                    @Override
                    public void onDataLoaded(String url) {
                        registerSliceUrl(url, albumId); //register the new slice url
                        Album album = DropboxProvider.this.albums.get("/"+albumName);
                        album.albumSlices.add(new AlbumSlice(UserSessionDetails.user_id, new Url(url)));
                        DropboxProvider.this.observer.update(DropboxProvider.this, null);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                }, "/" + albumName+"/catalog.txt");
                sharedLink.execute();

            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Toast t = Toast.makeText(ctx, "Error creating album folder", Toast.LENGTH_SHORT);
                t.show();
            }
        });
        task.setAlbumKey(albumKey);
        task.execute("/"+albumName);
    }
    @Override
    public AlbumSlice createAlbumSlice(AlbumSlice slice) {
        return null;
    }

    //================================================================================
    // PHOTOS RELATED METHODS
    //================================================================================

    @Override
    public Photo getPhoto(Photo photo) {
        if(photo instanceof CachedPhoto) {
            return photo;
        }
        return null;
    }

    @Override
    public CachedPhoto createPhoto(File local, Album album, Url url) throws IOException {
        return null;
    }

    public boolean createPhotoRefactored(final File local, final Album album, final Url url) {
        boolean uploadSuccess = uploadFileToDropbox(local, url);

        String sharedLinkUrl = getSharedLink(album.url.url +"/"+local.getName());

        boolean updateCatalogSuccess = updateCatalogRefactored(album, sharedLinkUrl, local.getName());
        AlbumSlice slice = DropboxProvider.this.getAlbumSlice(album, UserSessionDetails.user_id);
        slice.photos.add(new CachedPhoto(slice, new Url(album.url+"/"+local.getName()), local));
        File output = new File(ctx.getFilesDir(), album.name + "/" + local.getName());
        boolean result = Util.copyFile(local, output);
        if(result) {
            album.localPhotos.add(new CachedPhoto(slice, new Url(album.name + "/" + local.getName()), output));
        }
        return uploadSuccess && sharedLinkUrl != null && updateCatalogSuccess && result;
    }

    private boolean uploadFileToDropbox(File localFile, Url url) {
        // upload the file
        final DbxClientV2 dbClient = DropboxClientFactory.getClient();
        // Note - this is not ensuring the name is a valid dropbox file name
        String remoteFileName = localFile.getName();
        try (InputStream inputStream = new FileInputStream(localFile)) {
            dbClient.files().uploadBuilder(url.url)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);
        } catch (DbxException | IOException e) {
            //mException = e;
            return false;
        }
        return true;
    }

    private String getSharedLink(String path) {
        // get shared url
        DbxClientV2 client = DropboxClientFactory.getClient();
        try {

            SharedLinkMetadata sharedLinkMetadata = client.sharing().createSharedLinkWithSettings(path);
            return sharedLinkMetadata.getUrl();
        } catch (CreateSharedLinkWithSettingsErrorException ex) {
            if(ex.errorValue.toString().equals("\"shared_link_already_exists\"")){
                try {
                    ListSharedLinksResult a = client.sharing().listSharedLinksBuilder().withPath(path).start();
                    List<SharedLinkMetadata> b = a.getLinks();
                    return b.get(0).getUrl();
                } catch (DbxException e) {
                    e.printStackTrace();
                }
            }
        } catch (DbxException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //================================================================================
    // CATALOG RELATED METHODS
    //================================================================================

    public boolean downloadCatalog(File outputFile, final Album album) {
        final DbxClientV2 client = DropboxClientFactory.getClient();
        String url = "/"+album.name + "/"+"catalog.txt";

        try {
            //output file for download --> storage location on local system to download file
            OutputStream downloadFile = new FileOutputStream(outputFile);
            try {
                FileMetadata metadata = client.files().downloadBuilder(url)
                        .download(downloadFile);
            } finally {
                downloadFile.close();
            }
        } catch (DbxException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updateCatalogRefactored(final Album album, final String url, final String name)  {
        final DbxClientV2 client = DropboxClientFactory.getClient();
        final String tmp_loc =  album.url.url+"/"+"catalog.txt";
        File outputFile = new File(ctx.getFilesDir(), tmp_loc);

        //download the most recent catalog
        boolean catalogSuccess = downloadCatalog(outputFile, album);

        Log.d("UpdateCatalog", Boolean.toString(catalogSuccess));

        if(catalogSuccess) {
            try {
                //update the catalog locally
                String b = null;
                String albumSecret = null;
                try {
                    albumSecret = getAlbumKeyFromServer(album.name);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }

                String albumEncodedKey = null;
                try {
                    albumEncodedKey = SecurityHelper.decryptRSA(albumSecret
                            , "rsa_" + UserSessionDetails.user_name);
                } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                        IOException | NoSuchPaddingException | BadPaddingException |
                        IllegalBlockSizeException | UnrecoverableKeyException | InvalidKeyException e) {
                    e.printStackTrace();
                }

                byte[] albumDecodedKey = Base64.decode(albumEncodedKey, Base64.NO_WRAP);

                SecretKey albumKey = new SecretKeySpec(albumDecodedKey, 0, albumDecodedKey.length,
                        KeyProperties.KEY_ALGORITHM_AES);

                String encryptedContent = Util.fileToString(outputFile);
                Log.d("updateCatalog" , "encryptedContent -> "+encryptedContent);
                String decryptedContent = SecurityHelper.decryptGCM(encryptedContent, albumKey);
                byte[] decodedContent = Base64.decode(decryptedContent, Base64.DEFAULT);
                //JSONArray array = Util.parseJsonArray(Util.fileToString(outputFile));
                JSONArray array = Util.parseJsonArray(new String(decodedContent, StandardCharsets.UTF_8));
                JSONObject obj = new JSONObject();
                obj.put("name", name);
                obj.put("url", url);
                array.put(obj);
                //write the catalog to local storage

                //Util.writeStringToFile(outputFile, array.toString());
                Util.writeStringToFile(outputFile,
                        SecurityHelper.encryptGCM(array.toString(), albumKey));

                //Upload the catalog to dropbox
                InputStream inputStream = new FileInputStream(outputFile);
                client.files().uploadBuilder("/" + album.name+ "/" + outputFile.getName())
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);

            }catch (IOException | JSONException | DbxException e) {
                e.printStackTrace();
                return false;
            }finally {
                return true;
            }
        }else {
            return false;
        }
    }

    public void updateCatalog(final Album album, final String url, final String name) {
        final DbxClientV2 client = DropboxClientFactory.getClient();
        AlbumSlice slice = this.getAlbumSlice(album, UserSessionDetails.user_id);
        final String tmp_loc =  album.url.url+"/"+"catalog.txt";
        File outputFile = new File(ctx.getFilesDir(), tmp_loc);


        DownloadFileFromDropboxTask task = new DownloadFileFromDropboxTask(outputFile, client, new DownloadFileFromDropboxTask.Callback() {
            @Override
            public void onDataLoaded(File out) {
                //File out = new File(ctx.getCacheDir(),tmp_loc);
                JSONArray array = null;
                try {
                    array = Util.parseJsonArray(Util.fileToString(out));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JSONObject obj = new JSONObject();
                try {
                    obj.put("name", name);
                    obj.put("url", url);
                    array.put(obj);
                    Util.writeStringToFile(out, array.toString());
                    UploadFileToDropboxTask task = new UploadFileToDropboxTask(out, client, new UploadFileToDropboxTask.Callback() {
                        @Override
                        public void onDataLoaded() {
                            System.out.println("IT WORKED");
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                        }
                    });
                    task.execute(new Url("/"+album.name));

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
        task.execute(new Url("/"+album.name + "/"+"catalog.txt"));
    }

    class CreateAlbumResponseListener implements Response.Listener<String> {
        Url url;
        SecretKey albumKey;
        CreateAlbumResponseListener(Url url, SecretKey albumKey) {
            this.url = url;
        }

        @Override
        public void onResponse(final String response) {

        // Create folder on dropbox
        final Album[] album = new Album[1];
        final JSONObject responseData = Util.parseJson(response);
        CreateDropboxAlbumTask task = new CreateDropboxAlbumTask(DropboxClientFactory.getClient(), new CreateDropboxAlbumTask.Callback() {
            @Override
            public void onDataLoaded(FolderMetadata result) {
                AlbumSlice albumSlice = new AlbumSlice(UserSessionDetails.user_id, new Url(result.getPathLower() + "/catalog.txt"));
                ArrayList<AlbumSlice> albumSlices = new ArrayList<>();
                albumSlices.add(albumSlice);
                try {
                    File file = new File(ctx.getFilesDir(), result.getName());
                    file.mkdirs();
                    File catalog = new File(ctx.getFilesDir(), result.getName()+"/"+"catalog.txt");
                    Util.writeStringToFile(catalog, "[]");
                    album[0] = new DropboxAlbum(albumSlices, new Url("/"+result.getName()), result.getName(), responseData.getInt("id"));
                    DropboxProvider.this.albums.put(album[0].url.url, album[0]);
                    DropboxProvider.this.observer.update(DropboxProvider.this, null);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                GetSharedUrlTask getUrl = new GetSharedUrlTask(DropboxClientFactory.getClient(),
                        new GetSharedUrlTask.Callback() {
                            @Override
                            public void onDataLoaded(String url) {
                                registerSliceUrl(url, album[0].album_id);
                            }
                            @Override
                            public void onError(Exception e) {
                                Toast t = Toast.makeText(ctx, "Error creating shared url", Toast.LENGTH_SHORT);
                                t.show();
                            }
                        }, result.getPathLower()+"/"+"catalog.txt");
                getUrl.execute();
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
        task.setAlbumKey(albumKey);
        task.execute(url.url);
        }
    }

    public void registerSliceUrl(final String url, final int album_id) {
        VolleySingleton volley = VolleySingleton.getInstance(ctx);
        StringRequest postRequest = new StringRequest(Request.Method.POST, ctx.getString(R.string.url_create_slice),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast t = Toast.makeText(ctx, "Success creating new album", Toast.LENGTH_SHORT );
                        t.show();
                        initializeGallery();
                    }
                },
        new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        int duration = Toast.LENGTH_SHORT;
                        Log.d("DropboxProvider", error.getMessage());

                        Toast t = Toast.makeText(ctx, "Error creating album", duration);
                        t.show();
                    }
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("usr_id", String.valueOf(UserSessionDetails.user_id));
                params.put("cookie", UserSessionDetails.cookie);
                params.put("alb_id", String.valueOf(album_id));
                params.put("url", url);
                return params;
            }
        };
        volley.addToRequestQueue(postRequest);
    }


    //================================================================================
    // SECURITY RELATED METHODS
    //================================================================================


    public String getAlbumKeyFromServer(final String albumName) throws ExecutionException, InterruptedException {

        VolleySingleton volley = VolleySingleton.getInstance(ctx);

        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.POST, ctx.getString(R.string.url_get_albums), future, future) {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("usr_id", String.valueOf(UserSessionDetails.user_id));
                params.put("cookie", UserSessionDetails.cookie);
                return params;
            }
        };
        volley.addToRequestQueue(request);

        JSONArray responseData = Util.parseJsonArray(future.get());
        String secret = null;
        for (int i = 0; i < responseData.length(); i++) {
            try {
                JSONObject album = responseData.getJSONObject(i);
                Log.d("LOLOLOL", album.getString("name"));
                if (album.getString("name").equals(albumName)) {
                    secret = album.getString("secret");
                    Log.d("LOLOLOL", secret);
                    return secret;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return secret;
    }
}
