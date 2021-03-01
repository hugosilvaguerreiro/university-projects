package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.SocketManager;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        Button b = findViewById(R.id.sendButton);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    SocketManager.getInstance().write("hello there ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        EditText edit = findViewById(R.id.setText);

    }
}
