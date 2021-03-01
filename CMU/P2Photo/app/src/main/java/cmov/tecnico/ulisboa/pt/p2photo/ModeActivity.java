package cmov.tecnico.ulisboa.pt.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);
    }

    public void cloudMode(View v){
        Intent cloudIntent = new Intent(this, LoginActivity.class);
        cloudIntent.putExtra("mode", getString(R.string.mode_cloud));
        startActivity(cloudIntent);
    }

    public void wifiDirectMode(View v) {
        Intent wifidirectIntent = new Intent(this, LoginActivity.class);
        wifidirectIntent.putExtra("mode", getString(R.string.mode_wifi));
        startActivity(wifidirectIntent);
    }
}
