package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.activities;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.dropbox.core.v2.DbxClientV2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.UserSessionDetails;
import cmov.tecnico.ulisboa.pt.p2photo.Util;
import cmov.tecnico.ulisboa.pt.p2photo.VolleySingleton;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.DropboxGalleryActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxClientFactory;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxProvider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.Provider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.AlbumSlice;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.CachedPhoto;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.DropboxAlbum;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Photo;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.WifiDirectAlbum;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.LoadAlbumFromProviderTask;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.SocketManager;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.WifiDirectConnectionsManager;

public class WifiDirectProvider extends Provider {


    private Map<String, Album> albums = new HashMap<>();
    private boolean showingPhotos = false;


    public WifiDirectProvider(Context ctx) {
        super(ctx);
    }


    public WifiDirectProvider(Context ctx, Observer observer) {
        super(ctx, observer);
    }



    public boolean initializeGalleryRefactored() {
        try {
            JSONArray responseData = Util.parseJsonArray(listUserAlbumRequest());
            Log.d("initializeGal", responseData.toString());
            for (int i = 0; i < responseData.length(); i++) {
                JSONObject album = responseData.getJSONObject(i);
                ArrayList<AlbumSlice> slices = new ArrayList<>();
                JSONArray serverSlices = album.getJSONArray("slices");

                for(int j = 0; j < serverSlices.length();j++) {
                    JSONObject slice = serverSlices.getJSONObject(j);

                    slices.add(new AlbumSlice(slice.getInt("user"),
                            new Url("wifi-direct/"+album.getString("name") + "/" + slice.getInt("user"))));
                    (new File(ctx.getFilesDir(), "wifi-direct")).mkdirs();
                    (new File(ctx.getFilesDir(), "wifi-direct/" + album.getString("name"))).mkdirs();
                    (new File(ctx.getFilesDir(), "wifi-direct/" + album.getString("name") + "/" + slice.getInt("user"))).mkdirs();
                    //checkForNullSlices(album, slice, slices);
                }
                initializeAlbum(album, slices);
            }
            WifiDirectProvider.this.observer.update(WifiDirectProvider.this, null);
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

    private void checkForNullSlices(final JSONObject album, JSONObject slice, ArrayList<AlbumSlice> slices) throws JSONException, ExecutionException, InterruptedException {
        //Check if someone added me to an album and create the url.
        if(slice.getString("slice_url").equals("null")
                && slice.getInt("user") == UserSessionDetails.user_id) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //createAlbumOnDropbox(album.getString("name"));
                        //String sharedLink = getSharedLink("/" + album.getString("name") + "/" + "catalog.txt");
                        String sharedLink = album.get("name") + "/" +  UserSessionDetails.user_id;
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


    private void initializeAlbum(JSONObject album, ArrayList<AlbumSlice> slices) throws JSONException, IOException {

        String album_name = album.getString("name");

        int album_id = album.getInt("id");

        (new File(ctx.getFilesDir(), "wifi-direct/"+ album_name)).mkdirs(); //Create the album locally
        (new File(ctx.getFilesDir(), "wifi-direct/"+ album_name + "/"+ UserSessionDetails.user_id)).mkdirs(); //create the user slice locally
        WifiDirectAlbum album1 = new WifiDirectAlbum(slices, new Url("wifi-direct/"+album_name), album_name, album_id);
        checkForAvailableLocalPhotos(album1);
        //File catalog = new File(ctx.getFilesDir(), album_name+"/"+"catalog.txt");
        //Util.writeStringToFile(catalog, "[]");
        Log.d("PUTOWO", album_name + " " + album1);
        WifiDirectProvider.this.albums.put(album_name, album1);
    }

    private void checkForAvailableLocalPhotos(Album album) {
        File localAlbum = new File(ctx.getFilesDir(), "wifi-direct/"+album.name);
        File[] localSlices = localAlbum.listFiles();
        if(localSlices == null) {
            Log.d("availableLocalPhotos", "localslice is null");
            localAlbum.mkdirs();
            (new File(ctx.getFilesDir(), "wifi-direct/"+ localAlbum.getName() + "/" + UserSessionDetails.user_id)).mkdirs();
        } else {
            Log.d("availableLocalPhotos", album.name);
            for(File slice : localSlices) {
                Log.d("availableLocalPhotos", slice.getName());
                File[] photos = slice.listFiles();
                if(photos == null) {
                    Log.d("availableLocalPhotos", "photo is null");
                    slice.mkdirs();
                }else {
                    for(File photo : photos) {
                        Log.d("availableLocalPhotos", photo.getName());
                        CachedPhoto newPhoto = new CachedPhoto(null, new Url(photo.getName()), photo);
                        album.addPhoto(photo.getName(), newPhoto);
                    }
                }
            }
        }
    }

    @Override
    public Album getAlbumService(Url url) {
        Album album = this.albums.get(url.url.replaceFirst("wifi-direct/", ""));
        Log.d("albumtest", "getAlbumService: "+url.url);
        if(album != null) {
            Log.d("albumtestNotNull", url.url.replaceFirst("wifi-direct/", ""));
            checkForAvailableLocalPhotos(album);
        }

        return album;

    }

    @Override
    public boolean createPhotoRefactored(File local, Album album, Url url) {
        //boolean uploadSuccess = uploadFileToDropbox(local, url);

        //String sharedLinkUrl = getSharedLink(album.url.url +"/"+local.getName());

        //boolean updateCatalogSuccess = updateCatalogRefactored(album, sharedLinkUrl, local.getName());
        AlbumSlice slice = WifiDirectProvider.this.getAlbumSlice(album, UserSessionDetails.user_id);
        //slice.photos.add(new CachedPhoto(slice, new Url(album.url+"/"+local.getName()), local));
        File output = new File(ctx.getFilesDir(), "wifi-direct/"+album.name + "/" + UserSessionDetails.user_id + "/" + local.getName());
        boolean result = Util.copyFile(local, output);
        if(result) {
            album.localPhotos.add(new CachedPhoto(slice, new Url(album.name + "/" + local.getName()), output));
        }

        //SocketManager.getInstance().sendPhoto(album.name, local.getName(), output);
        WifiDirectConnectionsManager.getInstance().broadcastPhoto(album.name,
                new File(ctx.getFilesDir(), "wifi-direct/"+album.name + "/" + UserSessionDetails.user_id + "/" + local.getName()));

        return result;
    }

    @Override
    public boolean registerNewAlbum(Url url, String albumName) {
        try {
            //Register the new album on the central server
            JSONObject result = Util.parseJson(createAlbumOnCentralServer(albumName));

            //Create the album on dropbox
            //boolean createAlbumResult = createAlbumOnDropbox(albumName);
            //String sharedLink = getSharedLink("/" + albumName + "/" + "catalog.txt");
            //String sharedLink = albumName + "/" +  UserSessionDetails.user_id;
            //registerSharedLinkOnCentralServer(result.getInt("id"), sharedLink);
            //Create the album locally
            //if(result != null) {
            createAlbumLocally(result.getInt("id"), albumName);
            //}
            return result != null;

        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
            return false;
        }
    }


    public int getAlbumIdByName(String albumName) {
        for(Album al : albums.values()){
            if(al.name.equals(albumName)){
                return al.album_id;
            }
        }
        return -1;
    }

    private void createAlbumLocally(int albumId, String albumName) {
        AlbumSlice albumSlice = new AlbumSlice(UserSessionDetails.user_id, new Url("wifi-direct/"+albumName + "/" + UserSessionDetails.user_id));
        ArrayList<AlbumSlice> albumSlices = new ArrayList<>();
        albumSlices.add(albumSlice);
        (new File(ctx.getFilesDir(), "wifi-direct/"+ albumName)).mkdirs();
        (new File(ctx.getFilesDir(), "wifi-direct/"+albumName + "/" + UserSessionDetails.user_id)).mkdirs();
        //Util.writeStringToFile(new File(ctx.getFilesDir(), albumName+"/"+"catalog.txt"), "[]");

        Album album = new WifiDirectAlbum(albumSlices, new Url("wifi-direct/"+albumName), albumName, albumId);
        Log.d("PUTOWO", albumName + " " + album);
        WifiDirectProvider.this.albums.put(albumName, album);
    }



    private String createAlbumOnCentralServer(final String albumName) throws ExecutionException, InterruptedException {
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
                params.put("secret", "e");
                return params;
            }
        };
        volley.addToRequestQueue(request);
        return future.get();
    }

    @Override
    public void initializeGallery() {

    }

    @Override
    public List<Album> getAlbums() {
        return new ArrayList<>(this.albums.values());
    }

    @Override
    public Album getAlbum(Url url) {
        Album album = this.albums.get(url.url);
        final String albumName = album.name;
        File localAlbum = new File(WifiDirectProvider.this.ctx.getFilesDir(), albumName);
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

    @Override
    public void createAlbum(Url url, String albumName) {

    }

    @Override
    public AlbumSlice getAlbumSlice(Album album, int user_id) {
        for(AlbumSlice slice : album.albumSlices) {
            if(slice.user_id == user_id) {
                return slice;
            }
        }
        return  null;
    }

    @Override
    public AlbumSlice createAlbumSlice(AlbumSlice slice) {
        return null;
    }

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

}
