package cmov.tecnico.ulisboa.pt.p2photo.gallery.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import cmov.tecnico.ulisboa.pt.p2photo.ListUsersActivity;
import cmov.tecnico.ulisboa.pt.p2photo.LoginActivity;
import cmov.tecnico.ulisboa.pt.p2photo.LogoutActivity;
import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.adapters.AlbumAdapter;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.adapters.PhotoAdapter;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.LocalFileSystemProvider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.InitializeGalleryTask;

public class LocalPhotosActivity extends GalleryBaseActivity{
    private File[] localFiles;
    LocalFileSystemProvider provider;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.provider = new LocalFileSystemProvider(LocalPhotosActivity.this, new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                Log.d("HELLO", "update: ");
                LocalPhotosActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        albumAdapter.notifyDataSetChanged();
                        photoAdapter.notifyDataSetChanged();
                    }
                });

            }
        });

        InitializeGalleryTask initialize = new InitializeGalleryTask(LocalPhotosActivity.this, this.provider, this.albumAdapter);
        initialize.execute();

        //task.execute();
        this.albumAdapter.setProvider(this.provider);
        this.albumAdapter.setContext(LocalPhotosActivity.this);
        this.photoAdapter.setProvider(this.provider);
        this.photoAdapter.setContext(LocalPhotosActivity.this);

        sharedPref = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
    }

    @Override
    public void onCreateAlbum() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText text = new EditText(this);
        builder.setIcon(R.drawable.album3);
        builder.setTitle("Create album").setMessage("Please choose the name for the new album").setView(text);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface di, int i) {
                //final String name = text.getText().toString();
                provider.createAlbum(new Url(provider.LOCAL_CACHE_GALLERY_NAME + "/" + text.getText().toString()), text.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface di, int i) {
            }
        });
        builder.create().show();
    }

    @Override
    public void onTest() {

    }

    @Override
    public void onAlbumCLick(Album album) {
        super.onAlbumCLick(album);
    }

    @Override
    public void onViewAlbum() {

    }

    @Override
    public void onAddPhoto(File file) {
        String url = this.currentAlbum.url.url+ "/" + file.getName();
        Toast t = Toast.makeText(this, "message", Toast.LENGTH_SHORT);
        //toast.setGravity(Gravity.CENTER, 0, 0);
       /* try {
            this.provider.createPhoto(file, this.currentAlbum.albumSlices.get(0), new Url(url)); //fix this slice
            t.setText("Upload successeful");
        } catch (IOException e) {
            e.printStackTrace();
            t.setText("Failed uploading photo "+ file.getName());
        }
        t.show();*/
    }

    @Override
    public void onAddUsersToAlbum(int userId, int albumId, String publicKey) {

    }

    @Override
    public void onListUsersAlbum() {

    }

    @Override
    public void onListUsers() {
        Intent listUsers = new Intent(this, ListUsersActivity.class);
        startActivity(listUsers);
    }

    @Override
    public void onLogout() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent logout = new Intent(getApplicationContext(), LogoutActivity.class);
                        startActivity(logout);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

}


