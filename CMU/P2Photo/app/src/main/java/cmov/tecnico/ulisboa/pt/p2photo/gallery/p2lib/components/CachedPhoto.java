package cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components;

import java.io.File;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.AlbumSlice;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Photo;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;

public class CachedPhoto extends Photo {
    public File localFile;
    public CachedPhoto(AlbumSlice slice, Url url, File file) {
        super(slice, url);

        this.localFile = file;
    }

    public boolean equals(Object obj) {
        if(obj instanceof  CachedPhoto) {
            CachedPhoto other = (CachedPhoto)obj;
            return (this.localFile.getAbsolutePath().equals(other.localFile.getAbsolutePath())
                    && this.url.url.equals(other.url.url));
        }
        return false;

    }


    public void setLocalFile(File file) {
        this.localFile = file;
    }

    public File getLocalFile(){return  localFile;}
}
