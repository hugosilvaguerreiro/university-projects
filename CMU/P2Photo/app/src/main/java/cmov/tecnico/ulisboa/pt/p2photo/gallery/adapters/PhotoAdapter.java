package cmov.tecnico.ulisboa.pt.p2photo.gallery.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.Provider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.AlbumSlice;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.CachedPhoto;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Photo;


public class PhotoAdapter extends RecyclerView.Adapter implements Serializable {

    Context ctx;
    Provider provider;
    Album album;

    public void setContext(Context ctx) { this.ctx = ctx; }
    public void setAlbum(Album album) {this.album = album;}
    public void setProvider(Provider provider) { this.provider  = provider; }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.files_item, parent, false);
        return new PhotoViewHolder(ctx, view, provider);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d("BIND", "onBindViewHolder: "+position+" "+album.localPhotos.get(position).toString());
        ((PhotoViewHolder) holder).bind(album.localPhotos.get(position));
    }

    @Override
    public int getItemCount() {
        if(album != null) {
            Log.d("TESTBLA", "getItemCount: "+album.localPhotos.size());
            return album.localPhotos.size();
        }
        return 0;
    }

}


