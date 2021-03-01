package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.UserSessionDetails;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.WiFiDirectBroadcastReceiver;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.fragments.DeviceFragment;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.fragments.DeviceListFragment;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.ClientSocketHandler;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.ServerSocketHandler;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.WifiDirectConnectionsManager;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.services.ServiceData;

public class WiFiDirectActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener,
        DeviceListFragment.DeviceActionListener, Handler.Callback {

    public static final String TAG = "WiFiDirectActivity";

    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;

    // TXT RECORD properties
    public static final String TXTRECORD_NAME = "name";
    public static final String TXTRECORD_ID = "id";

    public static final String SERVICE_INSTANCE = "_p2photowifi_fhhfggh";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    public static final int MESSAGE_READ = 0x1337BEEF + 1;
    public static final int HANDLE = 0x1337BEEF + 2;
    private static final int SERVICE_BROADCASTING_INTERVAL = 20000;
    private static final long SERVICE_DISCOVERING_INTERVAL = 10000;
    private static final String SOCKET_HANDLER = "socker_handler";

    private WifiP2pDnsSdServiceRequest serviceRequest;

    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private Handler mServiceBroadcastingHandler;
    private Handler mServiceDiscoveringHandler;



    public static final int SERVER_PORT = 8988;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;



    private ClientSocketHandler cliSocketHandler;
    public void setCliSocketHandler(ClientSocketHandler cliSocketHandler) {
        //manager.requestGroupInfo(channel,);
        this.cliSocketHandler = cliSocketHandler;
    }
    private ServerSocketHandler goSockerHandler;
    public void setGoSockerHandler(ServerSocketHandler goSockerHandler) {
        this.goSockerHandler = goSockerHandler;
    }

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION:
                if  (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Coarse location permission is not granted!");
                    finish();
                }
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_direct);
        // add necessary intent values to be matched.

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        mServiceBroadcastingHandler = new Handler();
        mServiceDiscoveringHandler = new Handler();
        startBroadcastingService();
        prepareServiceDiscovery();
        startServiceDiscovery();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    WiFiDirectActivity.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            // After this point you wait for callback in
            // onRequestPermissionsResult(int, String[], int[]) overridden method
        }
    }

    public void clearBroadcastingService() {
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int error) {
                // react to failure of clearing the local services
            }
        });
    }

    public void startBroadcastingService(){
        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_NAME, UserSessionDetails.user_name);
        record.put(TXTRECORD_ID, Integer.toString(UserSessionDetails.user_id));

        final WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                manager.addLocalService(channel, service,
                        new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                // service broadcasting started
                                mServiceBroadcastingHandler
                                        .postDelayed(mServiceBroadcastingRunnable,
                                                SERVICE_BROADCASTING_INTERVAL);
                                Toast.makeText(WiFiDirectActivity.this, "Added Local Service", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(int error) {
                                // react to failure of adding the local service
                            }
                        });
            }

            @Override
            public void onFailure(int error) {
                // react to failure of clearing the local services
            }
        });
    }

    private Runnable mServiceBroadcastingRunnable = new Runnable() {
        @Override
        public void run() {
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "mServiceBroadcastingRunnable");

                }

                @Override
                public void onFailure(int error) {
                }
            });
            mServiceBroadcastingHandler
                    .postDelayed(mServiceBroadcastingRunnable, SERVICE_BROADCASTING_INTERVAL);
        }
    };

    public void prepareServiceDiscovery() {

        manager.setDnsSdResponseListeners(channel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {
                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                            // update the UI and add the item the discovered
                            // device.
                            DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                                    .findFragmentById(R.id.frag_list);
                            if (fragment != null) {
                                DeviceListFragment.WiFiPeerListAdapter adapter = ((DeviceListFragment.WiFiPeerListAdapter) fragment
                                        .getListAdapter());
                                ServiceData service = new ServiceData();
                                service.device = srcDevice;
                                service.instanceName = instanceName;
                                service.serviceRegistrationType = registrationType;
                                adapter.add(service);
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, "ServiceAvailable " + instanceName);
                            }
                        }
                    }
                }, new WifiP2pManager.DnsSdTxtRecordListener() {

                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record, WifiP2pDevice device) {

                        if (fullDomainName.equalsIgnoreCase("_p2photowifi_fhhfggh._presence._tcp.local.")) {
                            DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                                    .findFragmentById(R.id.frag_list);
                            if (fragment != null) {
                                DeviceListFragment.WiFiPeerListAdapter adapter = ((DeviceListFragment.WiFiPeerListAdapter) fragment
                                        .getListAdapter());
                                adapter.update(device, record.get(TXTRECORD_NAME), Integer.valueOf(record.get(TXTRECORD_ID)));
                                adapter.notifyDataSetChanged();

                            }
                        }
                    }
                });

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
    }

    private Runnable mServiceDiscoveringRunnable = new Runnable() {
        @Override
        public void run() {
            startServiceDiscovery();
        }
    };

    private void startServiceDiscovery() {
        manager.removeServiceRequest(channel, serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        manager.addServiceRequest(channel, serviceRequest,
                                new WifiP2pManager.ActionListener() {


                                    @Override
                                    public void onSuccess() {
                                        manager.discoverServices(channel,
                                                new WifiP2pManager.ActionListener() {

                                                    @Override
                                                    public void onSuccess() {
                                                        Log.d(TAG, "startServiceDiscovery");

                                                        mServiceDiscoveringHandler.postDelayed(
                                                                mServiceDiscoveringRunnable,
                                                                SERVICE_DISCOVERING_INTERVAL);
                                                    }

                                                    @Override
                                                    public void onFailure(int error) {
                                                        // react to failure of starting service discovery
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onFailure(int error) {
                                        // react to failure of adding service request
                                    }
                                });
                    }
                    @Override
                    public void onFailure(int reason) {
                        // react to failure of removing service request
                    }
                });
    }


    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceFragment fragmentDetails = (DeviceFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wifi_direct_items, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {

                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.

                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;

            case R.id.atn_direct_discover:
                //cliSocketHandler

                Intent intent = new Intent(this, WifiDirectGalleryActivity.class);
                startActivity(intent);
                return true;
//                if (!isWifiP2pEnabled) {
//                    Toast.makeText(WiFiDirectActivity.this, "Enable P2P from action bar button above or system settings",
//                            Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
//                        .findFragmentById(R.id.frag_list);
//                fragment.onInitiateDiscovery();
//                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
//
//                    @Override
//                    public void onSuccess() {
//                        Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated",
//                                Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailure(int reasonCode) {
//                        Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode,
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDetails(ServiceData data) {
        DeviceFragment fragment = (DeviceFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(data);

    }

    @Override
    public void onDestroy() {
        WifiDirectConnectionsManager.stopInstance();
        clearBroadcastingService();
        super.onDestroy();
    }


    @Override
    public void connect(ServiceData data) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = data.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (serviceRequest != null)
            manager.removeServiceRequest(channel, serviceRequest,
                    new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(int arg0) {
                        }
                    });

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(WiFiDirectActivity.this, "Connecting to service", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int errorCode) {
                Toast.makeText(WiFiDirectActivity.this, "Failed connecting to service:" + errorCode, Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    public void disconnect() {
        final DeviceFragment fragment = (DeviceFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                WifiDirectConnectionsManager.stopInstance();

            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
                WifiDirectConnectionsManager.stopInstance();
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            WifiDirectConnectionsManager.stopInstance();
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
            WifiDirectConnectionsManager.stopInstance();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        /*switch (message.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
                (chatFragment).pushMessage("Buddy: " + readMessage);
                break;
            case MY_HANDLE:
                Object obj = msg.obj;
                (chatFragment).setChatManager((ChatManager) obj);
        }*/
        return true;
    }
}
