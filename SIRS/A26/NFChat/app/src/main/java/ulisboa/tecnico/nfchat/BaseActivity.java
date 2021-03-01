package ulisboa.tecnico.nfchat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuItemImpl;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ulisboa.tecnico.nfchat.fragments.IndividualChatFragment;
import ulisboa.tecnico.nfchat.friends.ChatMenuFriend;
import ulisboa.tecnico.nfchat.friends.FriendData;
import ulisboa.tecnico.nfchat.friends.FriendDataManager;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
           //setSupportActionBar(toolbar);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            Menu menu = navigationView.getMenu();

            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.getString("uuid") != null && extras.getString("cert") != null) {
                addUserDialog(extras.getString("uuid"), extras.getString("cert"));
            }

            FriendData data1 = new FriendData("Huhgo",UUID.randomUUID().toString(), R.drawable.doot);
            FriendData data2 = new FriendData("Mhatilde",UUID.randomUUID().toString(), R.drawable.shrek);
            FriendData data3 = new FriendData("Mhatilde2",UUID.randomUUID().toString(), R.drawable.dm);

            //FriendData data2 = new FriendData("bla2", "test2");

            Context ctx = getApplicationContext();
            try {
                FriendDataManager.clearFriends(ctx);
                FriendDataManager.addNewFriend(ctx,data1);
                FriendDataManager.addNewFriend(ctx,data2);
                FriendDataManager.addNewFriend(ctx,data3);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /*
            List<ChatMenuFriend> friendsList = new ArrayList<>();
            HashMap<String, ChatMenuFriend> friendsMap = new HashMap<>();
            try {
                List<FriendData> friendx = FriendDataManager.getFriends(ctx);
                for(FriendData f : friendx) {
                    Log.d("FRIENDS",f.toJson().toString());
                    friendsList.add(new ChatMenuFriend(menu, f));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
*/

            updateMenuFriends();
            //ChatMenuFriend friend = new ChatMenuFriend(menu, "test");
            //ChatMenuFriend friend2 = new ChatMenuFriend(menu, "test2");

        }

        @Override
        public void onBackPressed() {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();



            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

       /* @SuppressWarnings("StatementWithEmptyBody")
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();
            Log.d("HEYY", item.toString());
            //Intent myIntent = new Intent(this, testActivity.class);
            //startActivity(myIntent);
            if (id == R.id.add_chat) {
                // Handle the camera action
            } else if (id == R.id.example_chat1) {

            }

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }*/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.add_chat) {
            Intent intent = new Intent(this, AddUserActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        // Create a new fragment and specify the fragment to show based on nav item clicked
       // Fragment fragment = null;
        //Class fragmentClass;
        //fragmentClass = IndividualChatFragment.class;

        //switch(menuItem.getItemId()) {
        //  case R.id.nav_first_fragment:
        //     fragmentClass = FirstFragment.class;
        //break;
        //}
        IndividualChatFragment fragment = null;
        try {
            Log.d("HELLO",item.getTitle().toString());
            String id = Integer.toString(item.getItemId());
            ChatMenuFriend friend  = ChatMenuFriend.friendsMenuItems.get(id);
            FriendData otherFriend = friend.getFriendData();
            fragment = IndividualChatFragment.newInstance(otherFriend);
        } catch (Exception e) {
            Log.d("HELLO", "shit");
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        //menuItem.setChecked(true);
        // Set action bar title
        //setTitle(menuItem.getTitle());
        // Close the navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);
    }

    private void addUserDialog(final String uuid,final String cert) {
        final EditText txt = new EditText(this);
        txt.setHint("Hugo");

        new AlertDialog.Builder(this)
                .setTitle("Name new Contact")
                .setView(txt)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = txt.getText().toString();
                        FriendData data = new FriendData(name, uuid, R.drawable.doot);
                        try {
                            FriendDataManager.addNewFriend(getApplicationContext(), data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        updateMenuFriends();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    private void updateMenuFriends() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Menu menu = navigationView.getMenu();

        List<ChatMenuFriend> friendsList = new ArrayList<>();
        HashMap<String, ChatMenuFriend> friendsMap = new HashMap<>();
        try {
            List<FriendData> friendx = FriendDataManager.getFriends(getApplicationContext());
            for(FriendData f : friendx) {
                Log.d("FRIENDS",f.toJson().toString());
                friendsList.add(new ChatMenuFriend(menu, f));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}


