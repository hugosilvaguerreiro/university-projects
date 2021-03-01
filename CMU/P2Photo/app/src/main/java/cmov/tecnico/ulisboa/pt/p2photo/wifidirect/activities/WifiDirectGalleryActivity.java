package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.dropbox.core.android.Auth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import cmov.tecnico.ulisboa.pt.p2photo.ListUsersActivity;
import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.DropboxGalleryActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.activities.GalleryBaseActivity;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxClientFactory;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxProvider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.AddUserToAlbumTask;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.CreateAlbumTask;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.LoadAlbumFromProviderTask;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.UploadPhotoTask;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.ServerSocketHandler;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.SocketManager;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.WifiDirectConnectionsManager;

public class WifiDirectGalleryActivity extends GalleryBaseActivity {

    private WifiDirectProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        this.provider = new WifiDirectProvider(WifiDirectGalleryActivity.this, new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                WifiDirectGalleryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        albumAdapter.notifyDataSetChanged();
                        photoAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        this.albumAdapter.setProvider(this.provider);
        this.albumAdapter.setContext(WifiDirectGalleryActivity.this);
        this.photoAdapter.setProvider(this.provider);
        this.photoAdapter.setContext(WifiDirectGalleryActivity.this);
        initAndLoadData();

    }

    private void initAndLoadData() {
        //this.provider.initializeGallery();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                WifiDirectGalleryActivity.this.provider.initializeGalleryRefactored();
            }
        });
        t.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK) {
                final String share_user_name = data.getStringExtra("share_user_name");
                final int share_user_id = data.getIntExtra("share_user_id", -1);
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(WifiDirectGalleryActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.album_spinner, null);
                mBuilder.setTitle("Pick an album to share with " + share_user_name );
                final Spinner mSpinner = mView.findViewById(R.id.album_spinner);
                ArrayList<String> albumNames = new ArrayList<>();
                for (Album album : this.provider.getAlbums()){
                    albumNames.add(album.name);
                    //Log.d("onActivityResult", "name: " + album.name + " id: " + album.album_id);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, albumNames );
                mSpinner.setAdapter(adapter);

                mBuilder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO dropbox albums do not have ids..
                        //int albumId = 0;
                        int albumId = provider.getAlbumIdByName(mSpinner.getSelectedItem().toString());
                        onAddUsersToAlbum(share_user_id, albumId, null);
                    }
                });
                mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("DropboxGalleryActivity" , "Shared: CANCELLED");
                    }
                });
                mBuilder.setView(mView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
            if(resultCode == Activity.RESULT_CANCELED){
                Log.d("ShareResult", "Result Canceled");
            }
        }
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

                WifiDirectGalleryActivity.this.prepareLoadingSlider("Creating", "Creating new album, please wait ...");
                WifiDirectGalleryActivity.this.toggleLoadingSlider();
                callProviderCreateAlbum(new Url(text.getText().toString()), text.getText().toString());
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
        try {
            //SocketManager.getInstance().write("123456789");
            Random rand = new Random();
            JSONObject obj = new JSONObject();
            for(int i=0; i < rand.nextInt(10); i++ ) {
                try {
                    obj.put(String.valueOf(i), "TESST" );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.d("SocketManager", "onTest: "+obj.toString());
            SocketManager.getInstance().sendJson(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void callProviderCreateAlbum(Url url, String albumName) {
        CreateAlbumTask task = new CreateAlbumTask(this.provider, url, albumName, new CreateAlbumTask.Callback() {
            @Override
            public void onDataLoaded(Boolean success) {
                WifiDirectGalleryActivity.this.toggleLoadingSlider();
                if(success) {
                    Toast.makeText(WifiDirectGalleryActivity.this, "Album successfully created",
                            Toast.LENGTH_SHORT).show();
                    WifiDirectGalleryActivity.this.albumAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(WifiDirectGalleryActivity.this, "Error creating album",
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(WifiDirectGalleryActivity.this, "Album successfully created",
                        Toast.LENGTH_SHORT).show();
            }
        });
        task.execute();
    }

    @Override
    public void onViewAlbum() {

    }

    @Override
    public void onAddPhoto(File file) {
        String url = "/"+this.currentAlbum.name+ "/" + file.getName();

        UploadPhotoTask task = new UploadPhotoTask(this.provider, file, this.currentAlbum, new Url(url),
                new UploadPhotoTask.Callback() {

                    @Override
                    public void onDataLoaded(Boolean success) {
                        WifiDirectGalleryActivity.this.toggleLoadingSlider();
                        if(success) {
                            Toast.makeText(WifiDirectGalleryActivity.this, "Upload successful",
                                    Toast.LENGTH_SHORT).show();
                            WifiDirectGalleryActivity.this.photoAdapter.notifyDataSetChanged();

                        }else {
                            Toast.makeText(WifiDirectGalleryActivity.this, "Upload failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        WifiDirectGalleryActivity.this.toggleLoadingSlider();
                        Toast.makeText(WifiDirectGalleryActivity.this, "Upload failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        task.execute();
    }


    @Override
    public void onAddUsersToAlbum(int userId, int albumId, String publicKey) {
        new AddUserToAlbumTask(this, new AddUserToAlbumTask.Callback() {
            @Override
            public void onDataLoaded(String result) {
                Log.d("AddUserToAlbumTask", result);
                if(result.equals("ok")){
                    Toast.makeText(getApplicationContext(), "Album share successful!", Toast.LENGTH_LONG).show();
                    //TODO create album slice for new user?
                }
            }

            @Override
            public void onError(Exception e) {

            }
        }).execute(userId, albumId);
    }

    @Override
    public void onListUsersAlbum() {

    }

    @Override
    public void onListUsers() {
        Intent listUsers = new Intent(this, ListUsersActivity.class);
        startActivityForResult(listUsers, 2);

    }

    @Override
    public void onLogout() {

    }


    @Override
    public void onRefresh() {
        if(this.showingPhotos) {
            mSwipeRefreshLayoutPhotos.setRefreshing(true);
            //super.onAlbumCLick(this.currentAlbum);
            LoadAlbumFromProviderTask task = new LoadAlbumFromProviderTask(this.provider, new LoadAlbumFromProviderTask.Callback() {
                @Override
                public void onDataLoaded(Album album) {
                    mSwipeRefreshLayoutPhotos.setRefreshing(false);
                    //Toast.makeText(getApplicationContext(), "Synchronize "+album.name +" Initialized" , Toast.LENGTH_SHORT).show();
                    WifiDirectGalleryActivity.this.photoAdapter.notifyDataSetChanged();
                }

                @Override
                public void onError(Exception e) {
                    mSwipeRefreshLayoutPhotos.setRefreshing(false);
                }
            });
            task.execute(this.currentAlbum);
        } else {
            //this.provider.initializeGallery();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    WifiDirectGalleryActivity.this.provider.initializeGalleryRefactored();
                }
            });
            t.start();
            mSwipeRefreshLayoutAlbums.setRefreshing(false);
        }
    }
}
