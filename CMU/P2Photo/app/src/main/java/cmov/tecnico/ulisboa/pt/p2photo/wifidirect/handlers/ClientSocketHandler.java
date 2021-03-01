package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.activities.WiFiDirectActivity;

public class ClientSocketHandler extends Thread {

    private static final String TAG = "ClientSocketHandler";
    private Handler handler;
    private SocketManager s;
    private InetAddress mAddress;


    public ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress) {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
                    WiFiDirectActivity.SERVER_PORT), 5000);
            Log.d(TAG, "Launching the I/O handler");
            s = SocketManager.getNewInstance(socket, this.handler);
            new Thread(s).start();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

}
