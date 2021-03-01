package cmov.tecnico.ulisboa.pt.p2photo.p2lib.cloud.gallery;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Arrays;

import cmov.tecnico.ulisboa.pt.p2photo.R;

public class GalleryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View content =  inflater.inflate(R.layout.example_fragment, container, false);

        GridView grid = content.findViewById(R.id.gallery_content);
        String[] views = new String[]{
                "hey",
                "hey",
                "hey",
                "hey",
        };

        // Populate a List from Array elements
        ArrayList<String> plantsList = new ArrayList<String>(Arrays.asList(views));

        // Create a new ArrayAdapter


        grid.setAdapter(new GalleryPhotoAdapter(getContext(), plantsList));
        return content;

    }
}



