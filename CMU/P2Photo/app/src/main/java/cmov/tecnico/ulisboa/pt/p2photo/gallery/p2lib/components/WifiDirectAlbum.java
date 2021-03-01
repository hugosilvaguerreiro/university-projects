package cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components;

import java.util.ArrayList;
import java.util.List;

public class WifiDirectAlbum extends Album{
    public List<AlbumSlice> albumSlices;
    public Url url;
    public String name;
    public int album_id;
    public boolean album_loaded;

    public WifiDirectAlbum(List<AlbumSlice> albSlices, Url url, String name, int album_id) {
        super(albSlices, url, name, album_id);
        album_loaded = false;

    }
    public WifiDirectAlbum(Url url, String name) {
        super(url, name);
        album_loaded = false;
    }
}
