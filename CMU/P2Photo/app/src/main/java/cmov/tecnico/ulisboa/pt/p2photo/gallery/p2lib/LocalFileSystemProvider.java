package cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.AlbumSlice;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.CachedPhoto;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Photo;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;

public class LocalFileSystemProvider extends Provider {
    public final String LOCAL_CACHE_GALLERY_NAME = "P2PHOTO.cache";
    public Map<String, Album> albums;
    public LocalFileSystemProvider(Context ctx) {
        super(ctx);
        this.albums = new HashMap<>();

    }

    public LocalFileSystemProvider(Context ctx, Observer observer) {
        super(ctx, observer);
        this.albums = new HashMap<>();
    }

    @Override
    public Album getAlbumService(Url url) {
        return null;
    }

    @Override
    public boolean createPhotoRefactored(File local, Album album, Url url) {
        return false;
    }

    @Override
    public boolean registerNewAlbum(Url url, String albumName) {
        return false;
    }

    @Override
    public void initializeGallery() {
        Activity a = (Activity) this.ctx;

        File dir = new File(a.getFilesDir(), LOCAL_CACHE_GALLERY_NAME);
        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            File[] albums = dir.listFiles();
            for (int i = 0; i < albums.length; i++)
            {
                Url new_url = new Url(LOCAL_CACHE_GALLERY_NAME +"/"+ albums[i].getName());
                File[] photos = albums[i].listFiles();
                AlbumSlice slice = new AlbumSlice(null); // Storing everything in the same slice because we dont have
                                                            // the information of slices implemented yet.
                for (int j = 0; j < photos.length; j++) {
                    Log.d("PHOTOS", "onBindViewHolder: "+photos[j].getName());
                    slice.addPhoto(new CachedPhoto(slice, new Url(new_url.url + "/"+photos[j].getName()), photos[j]));
                }

                List<AlbumSlice> slices = new ArrayList<>();
                slices.add(slice);
                this.albums.put(new_url.url, new Album(slices, new_url, albums[i].getName()));
                notifyObserverOnStateChanged(null);
            }
        }
    }

    @Override
    public List<Album> getAlbums() {
        return new ArrayList<Album>(albums.values());
    }

    @Override
    public Album getAlbum(Url url) {

        return albums.get(url.url);
    }

    @Override
    public void createAlbum(Url url, String albumName) {
        Activity a = (Activity) this.ctx;
        File newAlbum = new File(a.getFilesDir(), url.url);
        if (!newAlbum.exists()) {
            newAlbum.mkdirs();
            Album al = new Album(url, albumName);
            this.albums.put(url.url, al);
            //return al;
        } else {
            String text = "Album "+albumName+" already exists";
            Toast toast = Toast.makeText(this.ctx, text, Toast.LENGTH_SHORT);
            toast.show();
            //return this.albums.get(url.url);
        }
    }

    @Override
    public AlbumSlice getAlbumSlice(Album album, int user_id) {
        return null;
    }

    //@Override
    //public AlbumSlice getAlbumSlice(AlbumSlice slice) {
        //return null;
    //}

    @Override
    public AlbumSlice createAlbumSlice(AlbumSlice slice) {
        return null;
    }

    @Override
    public CachedPhoto getPhoto(Photo photo) {
        if(photo instanceof CachedPhoto ) {
            return (CachedPhoto) photo;
        }
        return null;
    }

    @Override
    public CachedPhoto createPhoto(File local, Album album, Url url) throws IOException {
        return null;
    }

    /*@Override
    public CachedPhoto createPhoto(File local, AlbumSlice slice, Url url) throws IOException {
        Activity a = (Activity) this.ctx;
        File newPhoto = new File(a.getCacheDir(), url.url);

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(local).getChannel();
            destination = new FileOutputStream(newPhoto).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
            CachedPhoto cached = new CachedPhoto(slice, url, newPhoto);
            slice.addPhoto(cached);
            this.observer.update(this, null);
            return cached;
        }
    }*/


}
