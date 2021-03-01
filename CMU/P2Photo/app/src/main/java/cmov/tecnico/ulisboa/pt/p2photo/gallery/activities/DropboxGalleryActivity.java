package cmov.tecnico.ulisboa.pt.p2photo.gallery.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.dropbox.core.android.Auth;
import com.dropbox.core.android.AuthActivity;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import cmov.tecnico.ulisboa.pt.p2photo.ListUsersActivity;
import cmov.tecnico.ulisboa.pt.p2photo.LogoutActivity;
import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.UserSessionDetails;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxClientFactory;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.DropboxProvider;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Url;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.AddUserToAlbumTask;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.CreateAlbumTask;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.GetAlbumKeyFromServerTask;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.LoadAlbumFromProviderTask;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.LoadAlbumFromProviderTask2;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.RevokeDropboxTokenTask;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks.UploadPhotoTask;
import cmov.tecnico.ulisboa.pt.p2photo.security.SecurityHelper;

public class DropboxGalleryActivity extends GalleryBaseActivity {
    DropboxProvider provider ;

    private static String TAG = "DropboxGalleryActivity";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK) {
                final String share_user_name = data.getStringExtra("share_user_name");
                final int share_user_id = data.getIntExtra("share_user_id", -1);
                final String share_public_key = data.getStringExtra("share_public_key");
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(DropboxGalleryActivity.this);
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
                        Log.d(TAG, share_public_key);
                        onAddUsersToAlbum(share_user_id, albumId, share_public_key);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        this.provider = new DropboxProvider(DropboxGalleryActivity.this, new Observer() {
            @Override
            public void update(Observable observable, Object o) {
                DropboxGalleryActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        albumAdapter.notifyDataSetChanged();
                        photoAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        this.albumAdapter.setProvider(this.provider);
        this.albumAdapter.setContext(DropboxGalleryActivity.this);
        this.photoAdapter.setProvider(this.provider);
        this.photoAdapter.setContext(DropboxGalleryActivity.this);

        if(!hasToken()){
            Auth.startOAuth2Authentication(DropboxGalleryActivity.this, getString(R.string.app_key));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        SharedPreferences sharedPref = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);

        String accessToken = sharedPref.getString("access-token", null);
        Log.d(TAG, "access-token onResume -> " + accessToken);
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken != null) {
                sharedPref.edit().putString("access-token", accessToken).apply();
                initAndLoadData(accessToken);
            }
        } else {
            Log.d(TAG, "initAndLoadData with accessToken");
            initAndLoadData(accessToken);
        }

        String uid = Auth.getUid();
        String storedUid = sharedPref.getString("dropbox-user-id", null);
        if (uid != null && !uid.equals(storedUid)) {
            sharedPref.edit().putString("dropbox-user-id", uid).apply();
        }
        Log.d(TAG, "uid: " + uid);
        Log.d(TAG, "storedUid: " + storedUid);

    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        //this.provider.initializeGallery();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                DropboxGalleryActivity.this.provider.initializeGalleryRefactored();
            }
        });
        t.start();
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

                DropboxGalleryActivity.this.prepareLoadingSlider("Creating", "Creating new album, please wait ...");
                DropboxGalleryActivity.this.toggleLoadingSlider();
                callProviderCreateAlbum(new Url("/" + text.getText().toString()), text.getText().toString());

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

    private void callProviderCreateAlbum(Url url, String albumName) {
        CreateAlbumTask task = new CreateAlbumTask(this.provider, url, albumName, new CreateAlbumTask.Callback() {
            @Override
            public void onDataLoaded(Boolean success) {
                DropboxGalleryActivity.this.toggleLoadingSlider();
                if(success) {
                    Toast.makeText(DropboxGalleryActivity.this, "Album successfully created",
                            Toast.LENGTH_SHORT).show();
                    DropboxGalleryActivity.this.albumAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(DropboxGalleryActivity.this, "Error creating album",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(DropboxGalleryActivity.this, "Album successfully created",
                        Toast.LENGTH_SHORT).show();
            }
        });
        task.execute();
    }

    @Override
    public void onAlbumCLick(Album album) {
        //mSwipeRefreshLayoutPhotos.setRefreshing(true);
        super.onAlbumCLick(album);

        LoadAlbumFromProviderTask2 task = new LoadAlbumFromProviderTask2(this.provider, new LoadAlbumFromProviderTask2.Callback() {
            @Override
            public void onDataLoaded(Album album) {
                mSwipeRefreshLayoutPhotos.setRefreshing(false);
            }

            @Override
            public void onError(Exception e) {
                mSwipeRefreshLayoutPhotos.setRefreshing(false);
            }
        });
        task.execute(album);
        //this.provider.getAlbum(album);
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
                        DropboxGalleryActivity.this.toggleLoadingSlider();
                        if(success) {
                            Toast.makeText(DropboxGalleryActivity.this, "Upload successful",
                                    Toast.LENGTH_SHORT).show();
                            DropboxGalleryActivity.this.photoAdapter.notifyDataSetChanged();

                        }else {
                            Toast.makeText(DropboxGalleryActivity.this, "Upload failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        DropboxGalleryActivity.this.toggleLoadingSlider();
                        Toast.makeText(DropboxGalleryActivity.this, "Upload failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        task.execute();
    }


    @Override
    public void onAddUsersToAlbum(final int userId, final int albumId, final String publicKey) {

        String b = null;
        final String[] albumSecret = {null};
        final String albumName = provider.getAlbumNamebyId(albumId);
        Log.d(TAG, "GOing to share album -> " + albumName);

        final SecretKey[] albumKey = new SecretKey[1];

        GetAlbumKeyFromServerTask task = new GetAlbumKeyFromServerTask(albumName, this.provider, new GetAlbumKeyFromServerTask.Callback() {
            @Override
            public void onDataLoaded(String key) {
                albumSecret[0] = key;
                String albumEncodedKey = null;
                try {
                    albumEncodedKey = SecurityHelper.decryptRSA(albumSecret[0], "rsa_" + UserSessionDetails.user_name);
                } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                        IOException | NoSuchPaddingException | BadPaddingException |
                        IllegalBlockSizeException | UnrecoverableKeyException | InvalidKeyException e) {
                    e.printStackTrace();
                }

                byte[] albumDecodedKey = Base64.decode(albumEncodedKey, Base64.DEFAULT);

                albumKey[0] = new SecretKeySpec(albumDecodedKey, 0, albumDecodedKey.length,
                        KeyProperties.KEY_ALGORITHM_AES);

                Log.d("INSIDE TASK", albumEncodedKey);
                Log.d("INSIDE TASK", Base64.encodeToString(albumKey[0].getEncoded(), Base64.DEFAULT));


                AddUserToAlbumTask addTask = new AddUserToAlbumTask(getApplicationContext(), new AddUserToAlbumTask.Callback() {
                    @Override
                    public void onDataLoaded(String result) {
                        Log.d("AddUserToAlbumTask", result);
                        if(result.equals("ok")){
                            Toast.makeText(getApplicationContext(), "Album share successful!", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onError(Exception e) {

                    }
                });
                addTask.setPublicKey(publicKey);
                addTask.setAlbumKey(albumKey[0]);
                addTask.execute(userId, albumId);

            }


            @Override
            public void onError(Exception e) {

            }
        });
        task.execute("");

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

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPref = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.remove("access-token");
                        editor.remove("dropbox-user-id");
                        editor.apply();

                        new RevokeDropboxTokenTask(DropboxClientFactory.getClient()).execute();
                        DropboxClientFactory.resetClient();
                        AuthActivity.result = null;

                        Log.d("onLogout", "access-token: " + sharedPref.getString("access-token", "BAMBOOZLE"));

                        Intent logout = new Intent(DropboxGalleryActivity.this, LogoutActivity.class);
                        startActivity(logout);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }


    protected boolean hasToken() {
        SharedPreferences prefs = getSharedPreferences("sessionPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("access-token", null);
        if(accessToken != null){
            Log.d(TAG + " hasToken", accessToken);
        }
        return accessToken != null;
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
                    DropboxGalleryActivity.this.photoAdapter.notifyDataSetChanged();
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
                    DropboxGalleryActivity.this.provider.initializeGalleryRefactored();
                }
            });
            t.start();
            mSwipeRefreshLayoutAlbums.setRefreshing(false);
        }
    }
}
