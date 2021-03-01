package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.fragments;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.UserSessionDetails;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.services.ServiceData;

public class DeviceListFragment extends ListFragment {

    private List<ServiceData> peers = new ArrayList<>();
    ProgressDialog progressDialog = null;
    View mContentView = null;
    private WifiP2pDevice device;

    static final String TAG = "WifiDirectActivity";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, peers));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        return mContentView;
    }

    /**
     * @return this device
     */
    public WifiP2pDevice getDevice() {
        return device;
    }

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }

    /**
     * Initiate a connection with the peer.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ServiceData data = (ServiceData) getListAdapter().getItem(position);
        ((DeviceActionListener) getActivity()).showDetails(data);
    }

    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    public class WiFiPeerListAdapter extends ArrayAdapter<ServiceData> {

        private List<ServiceData> items;
        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                                   List<ServiceData> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            ServiceData data = items.get(position);
            if (device != null) {
                TextView top = v.findViewById(R.id.device_name);
                TextView bottom = v.findViewById(R.id.device_details);
                if (top != null) {
                    if(data.username == null) {
                        top.setText(data.device.deviceName);
                    } else {
                        top.setText(data.username + " (" + data.device.deviceName+ ")");
                    }
                }
                if (bottom != null) {
                    bottom.setText(getDeviceStatus(data.device.status));
                }
            }

            return v;

        }

        @Override
        public void add(ServiceData o) {
            if(!items.contains(o)) {
                super.add(o);
            }
        }

        public void update(WifiP2pDevice o, String username, int user_id) {
            for(ServiceData s: items) {
                if(s.device.equals(o)) {
                    s.username = username;
                    s.id = user_id;
                }
            }
        }
    }

    /**
     * Update UI for this device.
     *
     * @param device WifiP2pDevice object
     */
    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
        TextView view = (TextView) mContentView.findViewById(R.id.my_name);
        view.setText(UserSessionDetails.user_name + " (" + device.deviceName+ ")");
        view = (TextView) mContentView.findViewById(R.id.my_status);
        view.setText(getDeviceStatus(device.status));
    }

    public void clearPeers() {
        peers.clear();
        WiFiPeerListAdapter peerListAdapter = ((WiFiPeerListAdapter) getListAdapter());
        if(peerListAdapter != null) peerListAdapter.notifyDataSetChanged();
    }


    /**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
    public interface DeviceActionListener {

        void showDetails(ServiceData device);

        void connect(ServiceData config);

        void disconnect();
    }

}
