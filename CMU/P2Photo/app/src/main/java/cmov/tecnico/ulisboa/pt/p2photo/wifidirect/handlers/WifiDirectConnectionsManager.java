package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.sax.EndTextElementListener;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import cmov.tecnico.ulisboa.pt.p2photo.UserSessionDetails;
import cmov.tecnico.ulisboa.pt.p2photo.Util;

import static cmov.tecnico.ulisboa.pt.p2photo.wifidirect.activities.WiFiDirectActivity.SERVER_PORT;
import static cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.SocketManager.TAG;


public class WifiDirectConnectionsManager implements Runnable, Handler.Callback {



    public int groupOwnerUserid;
    public HashMap<Integer, InetSocketAddress> peers;
    public HashMap<Integer, Boolean> peersAlive;
    public Context ctx;
    public boolean isGroupOwner;

    public boolean shouldExit = false;

    public static final int REGISTER = 1;
    public static final int SEND_PHOTO = 2;
    public static final int REQUEST_PEER_LIST = 3;
    public static final int SEND_PEER_LIST = 4;
    public static final int REQUEST_PHOTOS = 5;
    public static final int HEARTBEAT = 6;
    public static final int REQUEST_PEER_PHOTOS = 7;

    private static WifiDirectConnectionsManager instance = null;
    private ServerSocketHandler handler;

    public synchronized static WifiDirectConnectionsManager getInstance() {
        return instance;
    }

    public synchronized static void setInstance(WifiDirectConnectionsManager instance2) {
        instance = instance2;
    }

    public static BlockingQueue<JSONObject> sendPhotosQueue;

    public synchronized static WifiDirectConnectionsManager getNewInstance(Context ctx, int groupOwnerUserid, InetAddress groupOwnerLocation) {
        instance =  new WifiDirectConnectionsManager(ctx, groupOwnerUserid, groupOwnerLocation, false);
        return instance;
    }

    public synchronized static void stopInstance() {


        if(instance != null) {
            try {
                instance.handler.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            instance.shouldExit = true;
        }
    }

    public WifiDirectConnectionsManager(Context ctx, int groupOwnerUserid, InetAddress groupOwnerLocation, boolean isGroupOwner) {
        this.groupOwnerUserid = groupOwnerUserid;
        this.peers = new HashMap<>();
        this.peersAlive = new HashMap<>();
        this.isGroupOwner = isGroupOwner;

        this.peers.put(groupOwnerUserid, new InetSocketAddress(groupOwnerLocation, SERVER_PORT));
        this.ctx = ctx;
        WifiDirectConnectionsManager.setInstance(this);
    }

    public void sendMessage(final int toUserId, final JSONObject content) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = new Socket();

                InetSocketAddress address = WifiDirectConnectionsManager.this.peers.get(toUserId);
                if(address != null) {
                    try {
                        Looper.prepare();
                        socket.bind(null);
                        socket.connect(address, 7000);

                        SocketManager manager = new SocketManager(socket,
                                new Handler(WifiDirectConnectionsManager.this));
                        manager.sendJson(content);

                        Looper.loop();

                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            socket.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });
        t.start();
    }

    public void registerOnGroupOwner() {
        Log.d(TAG+"-register", "registerOnGroupOwner: ");
        JSONObject obj = new JSONObject();

        try {
            obj.put("type", REGISTER);
            obj.put("user_id", UserSessionDetails.user_id);

            this.sendMessage(groupOwnerUserid, obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //===================== PEERS LIST ==================================
    public void requestPeersList() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("type", REQUEST_PEER_LIST);
            obj.put("user_id", UserSessionDetails.user_id);

            this.sendMessage(groupOwnerUserid, obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendPeersList(int toUserId) {
        JSONObject obj = new JSONObject();

        try {
            obj.put("type", SEND_PEER_LIST);
            obj.put("user_id", UserSessionDetails.user_id);
            JSONArray array = new JSONArray();

            for (Map.Entry<Integer, InetSocketAddress> entry : peers.entrySet()) {
                String encoded = Base64.encodeToString(entry.getValue().getAddress().getAddress(), Base64.NO_WRAP);
                JSONObject peerEntry = new JSONObject();
                peerEntry.put("user_id", entry.getKey());
                peerEntry.put("addr", encoded);
                array.put(peerEntry);
            }
            obj.put("peers", array);
            this.sendMessage(toUserId, obj);

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    public void broadcastPhoto(String albumName, File photo) {
        File album = new File(ctx.getFilesDir(), "wifi-direct/" + albumName);
        album.mkdirs();
        for(File userIds : album.listFiles()) {
            int userId = Integer.valueOf(userIds.getName());

            if(peers.containsKey(userId) && userId != UserSessionDetails.user_id) {
                sendPhoto(userId, albumName, photo.getName(), photo);
            }
        }
    }

    //===================== PHOTOS ==================================

    public void sendPhoto(int toUserId, String albumName, String photoName, File photo) {
        try {
            byte[] photoBytes= Util.fileToByteArray(photo);
            String photoString = Base64.encodeToString(photoBytes, Base64.NO_WRAP);
            JSONObject content = new JSONObject();

            content.put("type", SEND_PHOTO);
            content.put("user_id", UserSessionDetails.user_id);
            content.put("photo_name", photoName);
            content.put("album_name", albumName);
            content.put("photo", photoString);
            content.put("to_user_id", toUserId);

            //this.sendMessage(toUserId, content);
            this.sendPhotosQueue.add(content);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void requestMissingAlbumPhotos(int toUserId, String albumName, ArrayList<String> availablePhotos) {

        JSONObject content = new JSONObject();

        try {
            content.put("type", REQUEST_PHOTOS);
            content.put("user_id", UserSessionDetails.user_id);
            content.put("album_name", albumName);

            JSONArray array = new JSONArray();
            for(String photo : availablePhotos) {
                array.put(photo);
            }
            content.put("photos", array);
            Log.d(TAG, "requestMissingAlbumPhotos: " + content.toString());
            this.sendMessage(toUserId, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void startServer() {
        try {
            handler = new ServerSocketHandler(new Handler(this));
            handler.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        sendPhotosQueue = new LinkedBlockingQueue<>();

        Log.d(TAG, "START WIFIDIRECTMANAGER THREAD");
        //Start thread to synchronize every N secs
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                missingPhotosLoop();
            }
        });
        t.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                processQueueLoop();
            }
        });
        t2.start();

        if(!isGroupOwner) {
            Thread t3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    heartBeatLoop();
                }
            });
            t3.start();
        }else {
            Thread t3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    garbageCollectorLoop();
                }
            });
            t3.start();
        }

        // Starts the server to receive connections
        // Registers the node on the group owner
        // Keeps getting the new list of peers
        // Starts a worker for handling requests for missing photos

        if(!isGroupOwner)
            registerOnGroupOwner(); //Register myself on the group owner peer

        while(!shouldExit) {
            try {
                Thread.sleep(10000);
                if(!isGroupOwner) {
                    Log.d(TAG, "REQUESTING PEER LIST");
                    requestPeersList();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                shouldExit = true;
            }
        }
        Log.d("EXIT-SIGNAL", "main Loop exit");

    }

    private void heartBeatLoop() {
        while (!shouldExit) {
            try {
                Thread.sleep(3000);
                JSONObject request = new JSONObject();
                request.put("type", HEARTBEAT);
                request.put("user_id", UserSessionDetails.user_id);
                this.sendMessage(-1, request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("EXIT-SIGNAL", "heart beat exit");
    }

    private void garbageCollectorLoop() {
        while (!shouldExit) {
            try {
                Thread.sleep(10000);

                for(Map.Entry<Integer, Boolean> entry : this.peersAlive.entrySet() ) {
                    int userId = entry.getKey();
                    boolean alive = entry.getValue();
                    if(alive && userId != UserSessionDetails.user_id) {
                        this.peersAlive.put(userId, false);
                    } else if(userId != UserSessionDetails.user_id){
                        this.peersAlive.remove(userId);
                        this.peers.remove(userId);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d("EXIT-SIGNAL", "garbage collector exit");
    }

    private void processQueueLoop() {
        while(!shouldExit) {
            try {
                Thread.sleep(100);
                Log.d(TAG, "processQueueLoop: waiting");
                JSONObject request = sendPhotosQueue.take();
                Log.d(TAG, "processQueueLoop: sending");
                WifiDirectConnectionsManager.this.sendMessage(request.getInt("to_user_id"), request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d("EXIT-SIGNAL", "process queue exit");
    }



    private void missingPhotosLoop() {
        while (!shouldExit) {
            try {
                Thread.sleep(20000);
                Log.d(TAG, "---------- SYNCHRONIZING ALBUMS START --------");
                File wifiDirectDir = new File(ctx.getFilesDir(), "wifi-direct");
                wifiDirectDir.mkdirs();
                for(File album : wifiDirectDir.listFiles()) {
                    Log.d(TAG, album.getName());
                    for(File userIds : album.listFiles()) {
                        Log.d(TAG, userIds.getName());
                        int userId = Integer.valueOf(userIds.getName());
                        if(peers.containsKey(userId) && userId != UserSessionDetails.user_id) {
                            ArrayList<String> myPhotos = new ArrayList<>(); //TODO this only supports sharing the photos from a specific user
                            for(File photo : userIds.listFiles()) {
                                myPhotos.add(photo.getName());
                            }
                            WifiDirectConnectionsManager.this.requestMissingAlbumPhotos(userId, album.getName(), myPhotos);
                        } else if(!peers.containsKey(userId)) { //broadcast request for this user photos
                            ArrayList<String> myPhotos = new ArrayList<>();
                            for(File photo : userIds.listFiles()) {
                                myPhotos.add(photo.getName());
                            }
                            WifiDirectConnectionsManager.this.broadcastMissingAlbumPhotos(userId, album.getName(), myPhotos);
                        }
                    }
                }
                Log.d(TAG, "---------- SYNCHRONIZING ALBUMS END --------");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d("EXIT-SIGNAL", "missing photos exit");
    }

    private void broadcastMissingAlbumPhotos(int userId, String albumName, ArrayList<String> availablePhotos) {
        JSONObject content = new JSONObject();

        try {
            content.put("type", REQUEST_PEER_PHOTOS);
            content.put("user_id", UserSessionDetails.user_id);
            content.put("requested_user_id", userId);
            content.put("album_name", albumName);

            JSONArray array = new JSONArray();
            for(String photo : availablePhotos) {
                array.put(photo);
            }
            content.put("photos", array);
            Log.d(TAG, "requestMissingAlbumPhotos: " + content.toString());

            for(Map.Entry<Integer, InetSocketAddress> entry : this.peers.entrySet() ) {
                sendMessage(entry.getKey(), content);

            }


            //this.sendMessage(toUserId, content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handleMessage(Message message) {
        ReceivedMessage content = (ReceivedMessage) message.obj;

        JSONObject obj = content.result;
        InetSocketAddress addr = content.remoteAddress;

        try {
            int type = obj.getInt("type");
            Log.d(TAG, "arrived here");
            Log.d(TAG, "type");
            switch (type) {
                case REGISTER:
                    peers.put(obj.getInt("user_id"), new InetSocketAddress(addr.getAddress(), SERVER_PORT));
                    peersAlive.put(obj.getInt("user_id"), true);
                    break;
                case SEND_PHOTO:
                    receivePhoto(obj);
                    break;
                case REQUEST_PEER_LIST:
                    Log.d(TAG, "PROCESSING PEER LIST");
                    processRequestPeerList(addr, obj);
                    break;
                case SEND_PEER_LIST:
                    Log.d(TAG, "PROCESSING RECEIVING PEER LIST");
                    processSendPeerList(addr, obj);
                    break;
                case REQUEST_PHOTOS:
                    Log.d(TAG, obj.toString());
                    processRequestPhotos(obj);
                    break;
                case HEARTBEAT:
                    processHeartBeat(addr, obj);
                    break;
                case REQUEST_PEER_PHOTOS:
                    processRequestPeerPhotos(addr, obj);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void processRequestPeerPhotos(InetSocketAddress addr, JSONObject obj) {

        try {
            int requesterUserId = obj.getInt("user_id");
            int targetUserId = obj.getInt("requested_user_id");

            String albumName = obj.getString("album_name");

            JSONArray peerPhotos = obj.getJSONArray("photos");


            Set<String> peerAvailablePhotosOfMySlice = new HashSet<>();
            for(int i=0; i < peerPhotos.length(); i++) {
                peerAvailablePhotosOfMySlice.add(peerPhotos.getString(i));
            }

            Set<String> myPhotos = getMyPhotosOfOtherPeer(targetUserId, albumName);

            myPhotos.removeAll(peerAvailablePhotosOfMySlice);

            for(String photo : myPhotos) {
                sendPhoto(requesterUserId, albumName,
                        photo, new File(ctx.getFilesDir(), "wifi-direct/" + albumName + "/" + targetUserId + "/" +photo));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Set<String> getMyPhotosOfOtherPeer(int targetUserId, String albumName) {
        File album = new File(ctx.getFilesDir(), "wifi-direct/" + albumName + "/" + targetUserId);

        Set<String> myPhotos = new HashSet<>();
        if(album.exists()) {
            Log.d(TAG, " >>> my photos start <<<");

            for(File photo : album.listFiles()) {
                myPhotos.add(photo.getName());
                Log.d(TAG, photo.getName());
            }
            Log.d(TAG, " >>> my photos end <<<");
        }
        return myPhotos;

    }

    private void processHeartBeat(InetSocketAddress addr, JSONObject obj) {
        try {
            int userId = obj.getInt("user_id");
            this.peersAlive.put(userId, true);
            this.peers.put(userId, new InetSocketAddress(addr.getAddress(), SERVER_PORT));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processRequestPhotos(JSONObject obj) {

        Log.d(TAG+"-diff", " ------ processRequestPhotos start --------");
        try {
            int userId = obj.getInt("user_id");
            String albumName = obj.getString("album_name");
            Log.d(TAG+"-diff", " >>> " +albumName+ " <<<");

            JSONArray peerPhotos = obj.getJSONArray("photos");
            Set<String> peerAvailablePhotosOfMySlice = new HashSet<>();

            Log.d(TAG+"-diff", " >>> peer photos start <<<");
            for(int i=0; i < peerPhotos.length(); i++) {
                peerAvailablePhotosOfMySlice.add(peerPhotos.getString(i));
                Log.d(TAG+"-diff", peerPhotos.getString(i));
            }
            Log.d(TAG+"-diff", " >>> peer photos  end <<<");

            Set<String> myPhotos = getMyPhotos(albumName);

             myPhotos.removeAll(peerAvailablePhotosOfMySlice);

             Log.d(TAG+"-diff", " >>> difference start <<<");
             for(String photo : myPhotos) {
                 Log.d(TAG+"-diff", "processRequestPhotos: " + photo);
                 sendPhoto(userId, albumName,
                         photo, new File(ctx.getFilesDir(), "wifi-direct/" + albumName + "/" + UserSessionDetails.user_id + "/" +photo));
             }
            Log.d(TAG+"-diff", " >>> difference end <<<");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG+"-diff", " ------ processRequestPhotos end --------");
    }


    private Set<String> getMyPhotos(String albumName) {
        (new File(ctx.getFilesDir(), "wifi-direct/")).mkdirs();
        (new File(ctx.getFilesDir(), "wifi-direct/" + albumName)).mkdirs();
        (new File(ctx.getFilesDir(), "wifi-direct/" + albumName + "/" + UserSessionDetails.user_id)).mkdirs();
        File album = new File(ctx.getFilesDir(), "wifi-direct/" + albumName + "/" + UserSessionDetails.user_id);
        Set<String> myPhotos = new HashSet<>();
        Log.d(TAG, " >>> my photos start <<<");

        for(File photo : album.listFiles()) {
            myPhotos.add(photo.getName());
            Log.d(TAG, photo.getName());
        }
        Log.d(TAG, " >>> my photos end <<<");
        return myPhotos;
    }

    private void processRequestPeerList(InetSocketAddress addr, JSONObject obj) {
        try {
            this.sendPeersList(obj.getInt("user_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processSendPeerList(InetSocketAddress addr, JSONObject obj) {

        try {
            JSONArray array = obj.getJSONArray("peers");
            //peers = new HashMap<>();
            Log.d(TAG+"-register", "#### PROCESS START ########");
            for (int i = 0; i < array.length(); i++) {
                Log.d(TAG+"-register", "my userid: " + UserSessionDetails.user_id);
                JSONObject peer = (JSONObject)array.get(i);
                int user_id = peer.getInt("user_id");
                String encodedAddr = peer.getString("addr");
                byte[] address = Base64.decode(encodedAddr, Base64.NO_WRAP);
                InetAddress inet = InetAddress.getByAddress(address);

                peers.put(user_id, new InetSocketAddress(inet, SERVER_PORT));
                Log.d(TAG+"-register", "////////////");
                Log.d(TAG+"-register", String.valueOf(user_id));
                Log.d(TAG+"-register", encodedAddr);
                Log.d(TAG+"-register", inet.getHostAddress());
                Log.d(TAG+"-register", "////////////");
            }
            Log.d(TAG+"-register", "###### PROCESS END ######");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void receivePhoto(JSONObject obj) {
        try {

            byte[] recvPhoto = Base64.decode(obj.getString("photo"), Base64.NO_WRAP);
            String album_name = obj.getString("album_name");
            int user_id = obj.getInt("user_id");
            String photo_name = obj.getString("photo_name");

            if (!(new File(ctx.getFilesDir(), "wifi-direct/" + album_name + "/" + user_id + "/" + photo_name)).exists()) {
                (new File(ctx.getFilesDir(),  "wifi-direct/" + obj.getString("album_name"))).mkdirs();
                (new File(ctx.getFilesDir(), "wifi-direct/" + album_name + "/" + user_id)).mkdirs();
                (new File(ctx.getFilesDir(), "wifi-direct/" + album_name + "/" + user_id + "/" + photo_name)).createNewFile();
                FileOutputStream os = new FileOutputStream(new File(ctx.getFilesDir(),
                        "wifi-direct/" + album_name + "/" + user_id + "/" + photo_name));
                os.write(recvPhoto);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
