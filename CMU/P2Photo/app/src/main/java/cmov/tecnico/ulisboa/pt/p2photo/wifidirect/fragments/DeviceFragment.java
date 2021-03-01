package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.fragments;

import android.app.ProgressDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import cmov.tecnico.ulisboa.pt.p2photo.UserSessionDetails;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.activities.WiFiDirectActivity;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.fragments.DeviceListFragment.DeviceActionListener;

import cmov.tecnico.ulisboa.pt.p2photo.R;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.ClientSocketHandler;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.MessageHandler;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.ServerSocketHandler;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.WifiDirectConnectionsManager;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.services.FileTransferService;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.services.ServiceData;

public class DeviceFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private ServiceData data;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;

    static final String TAG = "DeviceFragment";


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((DeviceListFragment.DeviceActionListener) getActivity()).connect(data);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);


                    }
                });

        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Uri uri = data.getData();
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending: " + uri);
        Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText("Am I the Group Owner? "+ ((info.isGroupOwner == true) ? "yes": "no"));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {


            /*new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();*/

            //MessageHandler
            ServerSocketHandler handler = null;
//            try {
//                handler = new ServerSocketHandler(new Handler(
//                        new MessageHandler(((WiFiDirectActivity)((DeviceActionListener) getActivity())))));
//                handler.start();
//                ((WiFiDirectActivity)((DeviceActionListener) getActivity()))
//                        .setGoSockerHandler(handler);
                if(WifiDirectConnectionsManager.getInstance() != null) {
                    WifiDirectConnectionsManager.stopInstance();
                }

                Context ctx = ((WiFiDirectActivity)((DeviceActionListener) getActivity()));
                WifiDirectConnectionsManager manager =
                        new WifiDirectConnectionsManager(ctx, UserSessionDetails.user_id, info.groupOwnerAddress, true);
                manager.startServer();
                new Thread(manager).start();


//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        } else if (info.groupFormed) {


            // The other device acts as the client. In this case, we enable the
            // get file button.
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(
                    "This device will act as a client."
            );


            //MessageHandler
            if(WifiDirectConnectionsManager.getInstance() != null) {
                WifiDirectConnectionsManager.stopInstance();
            }
            Context ctx = ((WiFiDirectActivity)((DeviceActionListener) getActivity()));
            WifiDirectConnectionsManager manager =
                    new WifiDirectConnectionsManager(ctx, -1, info.groupOwnerAddress, false);
            manager.startServer();
            new Thread(manager).start();

            /*ClientSocketHandler handler = new ClientSocketHandler(new Handler(new MessageHandler(
                    ((WiFiDirectActivity)((DeviceActionListener) getActivity())))), info.groupOwnerAddress);
            ((WiFiDirectActivity)((DeviceActionListener) getActivity()))
                    .setCliSocketHandler(handler);
            handler.start();*/
            //ClientSocketHandler
        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     *
     * @param data the device to be displayed
     */
    public void showDetails(ServiceData data) {
        this.data = data;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(data.device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(data.device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText("");
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("");
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText("");
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText("");
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;

        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(TAG, "Server: connection done");
                final File f = new File(context.getExternalFilesDir("received"),
                        "wifip2pshared-" + System.currentTimeMillis()
                                + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied - " + result);

                File recvFile = new File(result);
                Uri fileUri = FileProvider.getUriForFile(
                        context,
                        "cmov.tecnico.ulisboa.pt.p2photo.fileprovider",
                        recvFile);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "image/*");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return false;
        }
        return true;
    }

}
