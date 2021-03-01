package cmov.tecnico.ulisboa.pt.p2photo.gallery.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.RegisterActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.ShowPhotoActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.GenericFileProvider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.Provider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.CachedPhoto;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Photo;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.AsyncResponse;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.LoadPhotoFromProviderTask;

public class PhotoViewHolder extends RecyclerView.ViewHolder implements AsyncResponse<LoadPhotoFromProviderTask.LoadPhotoResponse>, View.OnClickListener {
    Context ctx;
    Provider provider;
    ImageView mImageView;
    TextView mTextView;
    Photo photo;

    public PhotoViewHolder(Context ctx, View itemView, Provider provider) {
        super(itemView);
        this.ctx = ctx;
        this.provider = provider;
        this.mImageView = (ImageView) itemView.findViewById(R.id.image);
        this.mTextView = (TextView) itemView.findViewById(R.id.text);
    }


    public void bind(Photo imageToFetch) {
        this.photo = imageToFetch;
        this.itemView.setOnClickListener(this);
        Drawable d = this.ctx.getDrawable(R.drawable.placeholder);
        TextView text = (TextView) this.itemView.findViewById(R.id.text);

        this.mImageView.setImageDrawable(d);
        LoadPhotoFromProviderTask load_image = new LoadPhotoFromProviderTask(imageToFetch, this.provider, this);
        load_image.execute();

    }

    @Override
    public void onResponse(LoadPhotoFromProviderTask.LoadPhotoResponse response) {
        this.photo = response.photo;
        this.mImageView = (ImageView) itemView.findViewById(R.id.image);
        this.mTextView = (TextView) itemView.findViewById(R.id.text);
        mImageView.setImageBitmap(response.thumbImage);
        mTextView.setText(response.photoName);
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onClick(View view) {
        if(this.photo != null) {
            Intent showPhotoIntent = new Intent(ctx, ShowPhotoActivity.class);
            showPhotoIntent.putExtra("file_location", ((CachedPhoto) this.photo).localFile.getAbsolutePath());
            ctx.startActivity(showPhotoIntent);
        }
    }
}
