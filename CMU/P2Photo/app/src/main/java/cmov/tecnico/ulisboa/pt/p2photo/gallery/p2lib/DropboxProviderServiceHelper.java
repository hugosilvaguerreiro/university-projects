package cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.VolleySingleton;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.AlbumSlice;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.CachedPhoto;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Photo;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;

public class DropboxProviderServiceHelper {
    public int userId;
    public String cookie;
    public Context ctx;

    public DropboxProviderServiceHelper(Context ctx, String cookie, int userId) {
        this.cookie = cookie;
        this.userId = userId;
        this.ctx = ctx;
    }

    public void initializeGallery() {
    }

    public void getAlbums() {

    }

    public Album getAlbum(Url url) {
        return null;
    }

    public void createAlbum(Url url, String albumName) {

    }

    public AlbumSlice getAlbumSlice(Album album, int user_id) {
        return null;
    }

    public AlbumSlice createAlbumSlice(AlbumSlice slice) {
        return null;
    }

    public Photo getPhoto(Photo photo) {
        return null;
    }

    public CachedPhoto createPhoto(File local, Album album, Url url) throws IOException {
        return null;
    }
}
