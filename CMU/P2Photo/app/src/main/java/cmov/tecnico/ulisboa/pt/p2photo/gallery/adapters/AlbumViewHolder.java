package cmov.tecnico.ulisboa.pt.p2photo.gallery.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.GalleryBaseActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;

public class AlbumViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{


    Context ctx;
    Album album;
    ImageView mImageView;
    View itemView;
    public void setAlbum(Album album) {
        this.album = album;
    }

    public AlbumViewHolder(Context ctx, View itemView) {
        super(itemView);

        this.ctx = ctx;
        //this.album = album;
        this.mImageView = (ImageView) itemView.findViewById(R.id.image);
        this.itemView = itemView;

    }

    public void bind() {

        Drawable d = this.ctx.getDrawable(R.drawable.album3);
        // Read your drawable from somewhere

        // Set your new, scaled drawable "d"
        this.mImageView.setImageDrawable(d);
        //LoadPhotoFromProviderTask load_image = new LoadPhotoFromProviderTask(this.ctx, imageToFetch, this.mImageView, this.provider);
        //load_image.execute();
        this.itemView.setOnClickListener(this);
        TextView text = (TextView) this.itemView.findViewById(R.id.text);
        text.setText(album.name);
    }

    @Override
    public void onClick(View view) {
        ((GalleryBaseActivity)this.ctx).onAlbumCLick(this.album);
    }
}
