package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.util.concurrent.TimeUnit;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.adapters.AlbumAdapter;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.Provider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.CachedPhoto;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Photo;

public class InitializeGalleryTask extends AsyncTask<Void, Void, Void> {
    private Provider provider;
    private AlbumAdapter adapter;
    private Context ctx;

    public InitializeGalleryTask(Context ctx, Provider provider, AlbumAdapter adapter) {
        this.provider = provider;
        this.adapter = adapter;
        this.ctx = ctx;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        this.provider.initializeGallery();

        Activity a = (Activity) this.ctx;

        return null;
    }
}
