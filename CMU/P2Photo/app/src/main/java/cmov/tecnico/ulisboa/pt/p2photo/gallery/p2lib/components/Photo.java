package cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components;

import java.io.File;


public class Photo {
    public AlbumSlice slice;
    public Url url;
    public File localFile;

    public Photo(AlbumSlice slice, Url url){
        this.slice = slice;
        this.url = url;
    }
}
