package cmov.tecnico.ulisboa.pt.p2photo.gallery.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

import cmov.tecnico.ulisboa.pt.p2photo.R;

public class ShowPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo);

        String path = getIntent().getStringExtra("file_location");

        Bitmap bit = BitmapFactory.decodeFile(path);

        ImageView img = (ImageView)this.findViewById(R.id.show_photo);

        img.setImageBitmap(bit);

    }
}
