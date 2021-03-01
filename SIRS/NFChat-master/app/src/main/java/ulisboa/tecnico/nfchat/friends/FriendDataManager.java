package ulisboa.tecnico.nfchat.friends;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ulisboa.tecnico.nfchat.FileHelper;

public class FriendDataManager {
    private static String FRIEND_DATA_PATH = "friends";

    public static void addNewFriend(Context ctx, FriendData newFriend) throws JSONException {
        List<FriendData> friends = FriendDataManager.getFriends(ctx);
        boolean shouldAdd = true;
        for(FriendData friend : friends){
            if(newFriend.equals(friend))
                shouldAdd = false;
        }
        if(shouldAdd | friends.size() == 0) {
            friends.add(newFriend);
            FriendDataManager.storeFriends(ctx, friends);
        }
    }

    public static void clearFriends(Context ctx) throws JSONException {
        List<FriendData> friends = new ArrayList<>();
        JSONObject result = new JSONObject();
        result.put("friends_list", friends);
        FileHelper.saveToFile(FriendDataManager.FRIEND_DATA_PATH, ctx, result.toString());
    }


    public static void storeFriends(Context ctx, List<FriendData> friends) throws JSONException {
        JSONArray array = new JSONArray();

        for(FriendData friend : friends) {
            JSONObject obj = friend.toJson();
            array.put(obj);
        }
        JSONObject result = new JSONObject();
        result.put("friends_list",array);
        FileHelper.saveToFile(FriendDataManager.FRIEND_DATA_PATH, ctx, result.toString(), false);
    }

    public static List<FriendData> getFriends(Context ctx) throws JSONException {
        JSONObject friends = FriendDataManager.loadFriends(ctx);

        JSONArray array = null;
        try {
            array = friends.getJSONArray("friends_list");
        } catch (JSONException e) {
            array = new JSONArray();
        }
        List<FriendData> result = new ArrayList<>();
        int n = array.length();
        for(int i=0; i < n; i++) {
            JSONObject friend = array.getJSONObject(i);
            FriendData newFriend = new FriendData(friend);
            result.add(newFriend);
        }
        return result;
    }

    private static JSONObject loadFriends(Context ctx) {
        String friends =  FileHelper.readFile(FriendDataManager.FRIEND_DATA_PATH, ctx);
        try {
            JSONObject obj = new JSONObject(friends);
            Log.d("FRIENDS",obj.toString());
            return obj;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
