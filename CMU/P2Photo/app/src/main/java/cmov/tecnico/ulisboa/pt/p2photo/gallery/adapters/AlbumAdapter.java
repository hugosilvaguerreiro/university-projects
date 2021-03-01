package cmov.tecnico.ulisboa.pt.p2photo.gallery.adapters;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.GalleryBaseActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.Provider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;

public class AlbumAdapter extends RecyclerView.Adapter implements Serializable {

    Context ctx;
    Provider provider;


    public void setContext(Context ctx) { this.ctx = ctx; }
    public void setProvider(Provider provider) { this.provider  = provider; }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.files_item, parent, false);
        Log.d("AlbumViewHolder", "getItemCount: "+String.valueOf(i));
        return new AlbumViewHolder(ctx, view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((AlbumViewHolder) holder).setAlbum(provider.getAlbums().get(position));
        ((AlbumViewHolder) holder).bind();
    }


    @Override
    public int getItemCount() {
        if(provider.getAlbums() != null) {
            return provider.getAlbums().size();
        }else {
            return 0;
        }

    }

}
