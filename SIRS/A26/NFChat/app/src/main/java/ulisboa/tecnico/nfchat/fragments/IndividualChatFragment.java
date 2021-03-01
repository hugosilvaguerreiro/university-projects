package ulisboa.tecnico.nfchat.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import ulisboa.tecnico.nfchat.R;
import ulisboa.tecnico.nfchat.friends.FriendData;

//The button and the text view you see when i click on the name are defined by this class
// You should try to make the ui using something like this class


public class IndividualChatFragment extends Fragment {
    private Button sendButton;
    private EditText messageBox;
    private FriendData friend;

    public void setFriendData(FriendData friend) {
        this.friend = friend;
    }

    public static IndividualChatFragment newInstance(FriendData friend) {
        IndividualChatFragment f = new IndividualChatFragment();
        f.setFriendData(friend);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.main_fragment, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        //Do stuff here
        this.sendButton = view.findViewById(R.id.button_chatbox_send);
        this.messageBox = view.findViewById(R.id.edittext_chatbox);
        Context c = getContext();
        Log.d("FRAGMENT", friend.getName());
        ScrollView v =  view.findViewById(R.id.layout_messages);

        LinearLayout list = view.findViewById(R.id.layout_messages2);
        sendButton.setOnClickListener(
                new ChatOnClickListener(this.messageBox, this.friend, c, list, v));
    }
}