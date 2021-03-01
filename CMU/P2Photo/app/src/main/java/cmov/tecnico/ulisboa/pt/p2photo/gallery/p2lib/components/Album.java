package cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class Album {
    public List<AlbumSlice> albumSlices;
    public ArrayList<CachedPhoto> localPhotos;
    public Url url;
    public String name;
    public int album_id;

    public Album(List<AlbumSlice> albumSlices, Url url, String name, int album_id) {
        this.albumSlices = albumSlices;
        this.localPhotos = new ArrayList<>();
        this.url = url;
        this.name = name;
        this.album_id = album_id;
    }

    public Album(List<AlbumSlice> albumSlices, Url url, String name) {
        this.albumSlices = albumSlices;
        this.localPhotos = new ArrayList<>();
        this.url = url;
        this.name = name;
    }

    public Album(Url url, String name) {
        this.albumSlices = new ArrayList<>();
        this.localPhotos = new ArrayList<>();
        this.url = url;
        this.name = name;
    }

    public boolean containsLocalPhoto(String name) {
        for(CachedPhoto photo : localPhotos){
            if(photo.getLocalFile().getName().equals(name))
                return true;
        }
        return false;
    }

    public void addPhoto(String name, CachedPhoto localPhoto) {


        if(!containsLocalPhoto(name)) {
            localPhotos.add(localPhoto);
        }

    }
}
