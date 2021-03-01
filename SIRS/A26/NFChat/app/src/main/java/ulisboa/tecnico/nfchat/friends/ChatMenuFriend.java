package ulisboa.tecnico.nfchat.friends;


import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.HashMap;

public class ChatMenuFriend {
    private MenuItem item;
    private FriendData friendData;
    public static HashMap<String, ChatMenuFriend> friendsMenuItems = new HashMap<>();


    public ChatMenuFriend(Menu menu, FriendData friend) {
        int id = View.generateViewId();
        MenuItem item = menu.add(0, id,0, friend.getName());
        item.setIcon(friend.getIcon_id());

        this.item = item;
        this.friendData = friend;
        Log.d("ID_FRIEND", Integer.toString(id));
        ChatMenuFriend.friendsMenuItems.put(Integer.toString(id),this);

    }

    public FriendData getFriendData() {
        return friendData;
    }

}
