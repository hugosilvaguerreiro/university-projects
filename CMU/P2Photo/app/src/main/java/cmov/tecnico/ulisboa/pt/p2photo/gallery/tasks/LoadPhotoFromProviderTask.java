package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.dropbox.core.v2.files.FolderMetadata;

import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.Provider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.CachedPhoto;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Photo;

public class LoadPhotoFromProviderTask extends AsyncTask<Void, Void, LoadPhotoFromProviderTask.LoadPhotoResponse> {
    public class LoadPhotoResponse {
        public Bitmap thumbImage;
        public String photoName;
        public CachedPhoto photo;

        public LoadPhotoResponse(Bitmap bitmap, String name, CachedPhoto photo) {
            this.thumbImage = bitmap;
            this.photoName = name;
            this.photo = photo;
        }
    }

    private Photo photo;

    private Provider provider;
    public AsyncResponse<LoadPhotoResponse> result;
    public Exception mException;

    final int THUMBSIZE = 256;

    public LoadPhotoFromProviderTask(Photo photo, Provider provider, AsyncResponse<LoadPhotoResponse> result) {
        this.photo = photo;
        this.provider = provider;
        this.result = result;
    }

    @Override
    protected void onPostExecute(LoadPhotoResponse photoResult) {
        super.onPostExecute(photoResult);
        if (mException != null) {
            this.result.onError(mException);
        } else {
            this.result.onResponse(photoResult);
        }
    }

    @Override
    protected LoadPhotoResponse doInBackground(Void... voids) {
        CachedPhoto ph = (CachedPhoto) provider.getPhoto(this.photo);


        final String name = ph.getLocalFile().getName();
        final Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(ph.getLocalFile().getAbsolutePath()),
                THUMBSIZE, THUMBSIZE);
        //Bitmap myBitmap = BitmapFactory.decodeFile(localImage.getAbsolutePath());

        return new LoadPhotoResponse(ThumbImage, name, ph);
    }
}
