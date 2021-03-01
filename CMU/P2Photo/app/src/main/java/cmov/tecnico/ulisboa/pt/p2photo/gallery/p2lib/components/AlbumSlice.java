package cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components;

import java.util.ArrayList;
import java.util.List;

public class AlbumSlice {
    public List<Photo> photos;
    public Url url;
    public int user_id;


    public AlbumSlice(Url url) {
        this.url = url;
        this.photos = new ArrayList<>();
    }
    public AlbumSlice(int user_id, Url url) {
        this.url = url;
        this.user_id  = user_id;
        this.photos = new ArrayList<>();
    }
    public AlbumSlice(List<Photo> photos, Url url){
        this.photos = photos;
        this.url = url;
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);

    }
}
