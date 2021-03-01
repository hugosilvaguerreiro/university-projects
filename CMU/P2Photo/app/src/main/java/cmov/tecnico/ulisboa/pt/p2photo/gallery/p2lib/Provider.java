package cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.AlbumSlice;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.CachedPhoto;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Photo;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;

public abstract class Provider extends Observable {
    public Context ctx;
    public Observer observer;
    public Provider(Context ctx) {
        this.ctx = ctx;
        this.observer = new Observer() {
            @Override
            public void update(Observable observable, Object o) {

            }
        };
    }
    public Provider(Context ctx, Observer observer) {
        this.ctx = ctx;
        this.observer = observer;
    }

    public abstract Album getAlbumService(final Url url);
    public abstract boolean createPhotoRefactored(final File local, final Album album, final Url url);
    public abstract boolean registerNewAlbum(Url url, final String albumName);
    public abstract void initializeGallery();
    public abstract List<Album> getAlbums();
    public abstract Album getAlbum(Url url);
    public abstract void createAlbum(Url url, String albumName);
    public abstract AlbumSlice getAlbumSlice(Album album, int user_id );
    public abstract AlbumSlice createAlbumSlice(AlbumSlice slice);
    public abstract Photo getPhoto(Photo photo);
    public abstract CachedPhoto createPhoto(File local, Album album, Url url) throws IOException;
    public void notifyObserverOnStateChanged(Object value) {
        observer.update(this, value);
    }

}
