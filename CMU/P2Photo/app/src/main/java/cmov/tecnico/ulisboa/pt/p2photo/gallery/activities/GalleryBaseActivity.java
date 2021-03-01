package cmov.tecnico.ulisboa.pt.p2photo.gallery.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.io.File;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.adapters.AlbumAdapter;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.adapters.PhotoAdapter;
import cmov.tecnico.ulisboa.pt.p2photo.gallery.p2lib.components.Album;


/**
 * Activity that displays the content of a path in dropbox and lets users navigate folders,
 * and upload/download files
 */
public abstract class GalleryBaseActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    private final int UPLOAD_PHOTO = 10; // request code fot gallery intent
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE=20;

    public AlbumAdapter albumAdapter;
    public Parcelable albumState;
    public Parcelable photosState;
    public PhotoAdapter photoAdapter;
    public RecyclerView albumRecyclerView;
    public RecyclerView photosRecyclerView;
    public SwipeRefreshLayout mSwipeRefreshLayoutAlbums;
    public SwipeRefreshLayout mSwipeRefreshLayoutPhotos;
    public ProgressDialog progressDialog;

    final static public int DEFAULT_ID = -1;
    final static public String DEFAULT_COOKIE = "cookie";

    private static final String EXTRA_BASEADAPTER = "base_adapter";
    public boolean showingPhotos = false;
    public Album currentAlbum = null;

    public static Intent getIntent(Context context, Class activity, PhotoAdapter adapter) {
        Intent filesIntent = new Intent(context, activity);
        filesIntent.putExtra(GalleryBaseActivity.EXTRA_BASEADAPTER, adapter);
        return filesIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //albumAdapter = (PhotoAdapter) getIntent().getSerializableExtra(GalleryBaseActivity.EXTRA_BASEADAPTER);
        albumAdapter = new AlbumAdapter();
        albumAdapter.setContext(GalleryBaseActivity.this);
        photoAdapter = new PhotoAdapter();
        photoAdapter.setContext(GalleryBaseActivity.this);

        setContentView(R.layout.activity_gallery);

        setToolbarTitle("Albums");
        //mDrawerToggle.setDrawerIndicatorEnabled(false);
        //mDrawerLayout.addDrawerListener(mDrawerToggle);


        /*ConstraintLayout l = findViewById(R.id.SetNameForm);
        l.setVisibility(View.GONE);*/
        addToolbarIcon();
        toggleFab(false);









        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(GalleryBaseActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (GalleryBaseActivity.this.shouldShowRequestPermissionRationale(GalleryBaseActivity.this,
                            Manifest.permission.READ_CONTACTS)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(GalleryBaseActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                // permission has been granted, continue as usual
                Intent captureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(captureIntent, UPLOAD_PHOTO);
            }
        }});


        RecyclerView recyclerView = findViewById(R.id.albums_list);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        recyclerView.setAdapter(this.albumAdapter);
        this.albumRecyclerView = recyclerView;

        // SwipeRefreshLayout
        mSwipeRefreshLayoutPhotos = (SwipeRefreshLayout) findViewById(R.id.swipe_container_photos);
        mSwipeRefreshLayoutPhotos.setEnabled(false);
        mSwipeRefreshLayoutPhotos.setOnRefreshListener(this);

        mSwipeRefreshLayoutAlbums = (SwipeRefreshLayout) findViewById(R.id.swipe_container_albums);
        mSwipeRefreshLayoutAlbums.setEnabled(true);
        mSwipeRefreshLayoutAlbums.setOnRefreshListener(this);


        RecyclerView photorecyclerView = findViewById(R.id.photos_list);
        photorecyclerView.setVisibility(View.INVISIBLE);
        photorecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        photorecyclerView.setAdapter(this.photoAdapter);
        this.photosRecyclerView = photorecyclerView;

        NavigationView navView = findViewById(R.id.nav_view);
        if (navView != null)
            navView.setNavigationItemSelectedListener(this);

    }
    private boolean shouldShowRequestPermissionRationale(GalleryBaseActivity galleryBaseActivity, String readContacts) {
            return false;
    }
    @Override
    protected void onResume() {

        super.onResume();

        if (photosState != null) {
            System.out.println("######## PHOTOS STATE ######");
            photosRecyclerView.getLayoutManager().onRestoreInstanceState(photosState);
        }
        if (albumState != null) {
            albumRecyclerView.getLayoutManager().onRestoreInstanceState(albumState);
        }
    }

    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Save list state
        photosState = this.photosRecyclerView.getLayoutManager().onSaveInstanceState();
        albumState = this.albumRecyclerView.getLayoutManager().onSaveInstanceState();

        state.putParcelable("photos_state", photosState);
        state.putParcelable("album_state", albumState);
    }


    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        // Retrieve list state and list/item positions
        if(state != null) {
            photosState = state.getParcelable("photos_state");
            albumState = state.getParcelable("album_state");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    FloatingActionButton fab = findViewById(R.id.fab);
                    fab.callOnClick();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast t = Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT);
                    t.show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    @Override
    protected void onActivityResult(int requestcode, int resultcode,
                                    Intent data) {
        super.onActivityResult(requestcode, resultcode, data);
        switch (requestcode) {
            case UPLOAD_PHOTO:
                if (resultcode == RESULT_OK){


                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };

                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);

                    cursor.close();

                    prepareLoadingSlider("Uploading", "Uploading photo. please wait ...");
                    toggleLoadingSlider();
                    onAddPhoto(new File(picturePath));
                } else {
                    Log.d("ONADDPHOTO", "onAddPhoto: -> rip");
                }
        }
    }
    public void prepareLoadingSlider(String title, String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog

    }
    public void toggleLoadingSlider() {
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else {
            progressDialog.show();
        }
    }

    private void addToolbarIcon() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle;

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.hello_world, R.string.hello_world)
            {

                public void onDrawerClosed(View view)
                {
                    supportInvalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView)
                {
                    supportInvalidateOptionsMenu();
                    //drawerOpened = true;
                }
            };
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            drawerLayout.addDrawerListener(mDrawerToggle);
            //drawerLayout.setD
            mDrawerToggle.syncState();
        }
    }

    private void toggleFab(boolean show) {
        FloatingActionButton fab = findViewById(R.id.fab);
        if(show) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
    }

    private void setToolbarTitle(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(this.showingPhotos){
            //this.photoAdapter.setAlbum(null);
            this.showingPhotos = false;
            setToolbarTitle("Albums");
            toggleFab(false);
            RecyclerView photorecyclerView = findViewById(R.id.photos_list);
            photorecyclerView.setVisibility(View.INVISIBLE);
            mSwipeRefreshLayoutPhotos.setEnabled(false);
            photoAdapter.setAlbum(null);
            mSwipeRefreshLayoutAlbums.setEnabled(true);
            RecyclerView albumRecyclerView = findViewById(R.id.albums_list);
            albumRecyclerView.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Intent newActivityIntent;

        if (id == R.id.nav_logout) {
            onLogout();
            //newActivityIntent = new Intent(this, LogoutActivity.class);
            //startActivity(newActivityIntent);
        } else if (id == R.id.nav_new_album) {
            onCreateAlbum();
            //newActivityIntent = new Intent(this, NewAlbumActivity.class);
            //startActivity(newActivityIntent);
        } else if (id == R.id.nav_list_users) {
            onListUsers();
        } /*else if (id == R.id.nav_test) {
            onTest();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void onAlbumCLick(Album album) {
        Log.d("ALBUMNAME", "onAlbumCLick: "+album.name);
        this.photoAdapter.setAlbum(album);
        //this.photosRecyclerView.swapAdapter(this.photoAdapter, true);
        this.photoAdapter.notifyDataSetChanged();
        RecyclerView photorecyclerView = findViewById(R.id.photos_list);

        photorecyclerView.setVisibility(View.VISIBLE);
        mSwipeRefreshLayoutPhotos.setEnabled(true);
        RecyclerView albumRecyclerView = findViewById(R.id.albums_list);
        albumRecyclerView.setVisibility(View.INVISIBLE);
        mSwipeRefreshLayoutAlbums.setEnabled(false);
        setToolbarTitle("Album " + album.name);
        this.showingPhotos = true;
        this.currentAlbum = album;
        toggleFab(true);

    }

    @Override
    public void onRefresh() {

    }

    public abstract void onCreateAlbum();
    public abstract void onTest();
    public abstract void onViewAlbum();
    public abstract void onAddPhoto(File file);
    public abstract void onAddUsersToAlbum(int userId, int albumId, String publicKey);
    public abstract void onListUsersAlbum();
    public abstract void onListUsers();
    public abstract void onLogout();


}
