package ulisboa.tecnico.nfchat.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ulisboa.tecnico.nfchat.R;
import ulisboa.tecnico.nfchat.friends.FriendData;

public class ChatOnClickListener implements View.OnClickListener  {
    public static boolean counter = false;
    private EditText text;
    private FriendData friend;
    private Context context;
    private  LinearLayout messages;
    private ScrollView scroll;
    public ChatOnClickListener(EditText textBox, FriendData friend,
                               Context context, LinearLayout messages,
                               ScrollView scroll) {
        this.text = textBox;
        this.friend = friend;
        this.context = context;
        this.messages = messages;
        this.scroll = scroll;
    }

    @Override
    public void onClick(View v) {

        Log.d("FRAGMENT", friend.getName());
        Log.v("FRAGMENT", this.text.getText().toString());

        View message;
        DateFormat df = new SimpleDateFormat("HH:mm");
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);

        if(ChatOnClickListener.counter) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            message = vi.inflate(R.layout.item_message_sent, null);
            TextView textView = (TextView) message.findViewById(R.id.text_message_body);
            textView.setText(this.text.getText());

            TextView date = (TextView) message.findViewById(R.id.text_message_time);
            date.setText(reportDate);
            ChatOnClickListener.counter = false;

        }else {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            message = vi.inflate(R.layout.item_message_received, null);
            TextView textView = (TextView) message.findViewById(R.id.text_message_body);

            ImageView imageView = message.findViewById(R.id.image_message_profile);
            imageView.setBackgroundResource(this.friend.getIcon_id());

            TextView nickName = message.findViewById(R.id.text_message_name);
            nickName.setText(this.friend.getName());
            TextView date = (TextView) message.findViewById(R.id.text_message_time);
            date.setText(reportDate);
            textView.setText(this.text.getText());
            ChatOnClickListener.counter = true;
        }

        messages.addView(message);
        scroll.fullScroll(ScrollView.FOCUS_DOWN);
        //RecyclerView list = v.findViewById(R.id.reyclerview_message_list);

    }
}
