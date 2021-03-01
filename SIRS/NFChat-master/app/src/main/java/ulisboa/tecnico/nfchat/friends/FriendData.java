package ulisboa.tecnico.nfchat.friends;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class FriendData {
    private String name;
    private int icon_id;
    private String id;


    public FriendData(JSONObject object) throws JSONException {
        this.name = object.getString("name");
        this.id = object.getString("id");
        this.icon_id = object.getInt("icon_id");
    }
    public FriendData(String name, String id, int icon_id) {
        this.name = name;
        this.icon_id = icon_id;
        this.id = id;
    }

    public String getName() {
        return name;
    }


    public int getIcon_id() {
        return icon_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JSONObject toJson() throws JSONException{
        JSONObject result = new JSONObject();
        result.put("name", this.name);
        result.put("icon_id", this.icon_id);
        result.put("id", this.id);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FriendData) {
            FriendData otherFriend = (FriendData) obj;
            return  this.name.equals(otherFriend.getName()) &&
                    this.id.equals(otherFriend.getId()) &&
                    this.icon_id == otherFriend.getIcon_id();
        }
        return false;
    }


}
