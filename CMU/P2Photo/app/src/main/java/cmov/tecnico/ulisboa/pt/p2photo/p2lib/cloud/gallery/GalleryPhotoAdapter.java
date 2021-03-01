package cmov.tecnico.ulisboa.pt.p2photo.p2lib.cloud.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import cmov.tecnico.ulisboa.pt.p2photo.R;

public class GalleryPhotoAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> empList;
    private static LayoutInflater inflater = null;

    public GalleryPhotoAdapter(Context context, ArrayList<String> empList) {
        this.context = context;
        this.empList = empList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return empList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = inflater.inflate(R.layout.gallery_photo, null);
        return v;
    }

}
