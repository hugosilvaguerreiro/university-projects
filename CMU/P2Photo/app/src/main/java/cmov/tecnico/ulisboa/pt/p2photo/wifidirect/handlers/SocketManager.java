package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import cmov.tecnico.ulisboa.pt.p2photo.UserSessionDetails;
import cmov.tecnico.ulisboa.pt.p2photo.Util;
import cmov.tecnico.ulisboa.pt.p2photo.wifidirect.activities.WiFiDirectActivity;

import static cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.MessageHandler.JSON;
import static cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.MessageHandler.PHOTO;

public class SocketManager implements Runnable {


    private static final int PHOTO2 = 5;
    private Socket socket = null;
    private Handler handler;


    private static SocketManager instance = null;
    public synchronized static SocketManager getInstance() {
        return instance;
    }

    public synchronized static SocketManager getNewInstance(Socket socket, Handler handler) {
        instance =  new SocketManager(socket, handler);
        return instance;
    }


    public SocketManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    private InputStream iStream;
    private OutputStream oStream;
    public static final String TAG = "SocketManager";
    private static final int HEADER_SIZE =  24;

    @Override
    public void run() {

        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            int bytes;
            //handler.obtainMessage(MessageHandler.SET_SOCKET_MANAGER, this)
            //        .sendToTarget();
            SocketManager.setInstance(this);

            byte[] header = new byte[HEADER_SIZE]; //read header
            // Read from the InputStream
            bytes = iStream.read(header);
            if (bytes == -1) {
                Log.d(TAG, "-1");
                return;
            }
            int length = convertByteToInt(Arrays.copyOfRange(header, 0, 4)); //get length
            int typeOfContent = convertByteToInt(Arrays.copyOfRange(header, 4, 8)); //get length

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Log.d(TAG, Integer.toString(typeOfContent));

            switch (typeOfContent) {
                case JSON:
                    byte[] buffer = new byte[1024*8];
                    int count;
                    while((count = iStream.read(buffer)) > 0) {
                        out.write(buffer, 0, count);
                    }
                    out.close();
                    iStream.close();
                    oStream.close();
                    /*handler.obtainMessage(JSON,
                            length, -1, Util.parseJson(new String(out.toByteArray()))).sendToTarget();*/
                    JSONObject result = Util.parseJson(new String(out.toByteArray()));

                    try {
                        InetSocketAddress addr = (InetSocketAddress) socket.getRemoteSocketAddress();

                        result.put("sender_address", socket.getRemoteSocketAddress());
                        socket.getRemoteSocketAddress();

                        handler.obtainMessage(JSON,
                                length, -1, new ReceivedMessage(addr, result)).sendToTarget();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    return;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //@Override
    public void run2() {
        /*
         * PROTOCOL:
         *
         * [HEADER] [CONTENT]
         *
         * [HEADER] - 16 bytes
         *      [[MSG_LENGTH - 4 bytes][TYPE_OF_CONTENT - 4 byte][ARG1 - 8 bytes][ARG2 - 8 bytes]]
         *
         *
         *  TYPE_OF_CONTENT:
         *      PHOTO:
         *          - identifier: 1
         *          - content: [FILE_NAME] [PHOTO]
         *          - arg1: int representing the size of the file name
         *
         *      JSON:
         *          - identifier: 2
         *          - content: [JSON STRING]
         *          - arg1: nothing
         *
         */

        try {

            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            int bytes;
            /*handler.obtainMessage(MessageHandler.SET_SOCKET_MANAGER, this)
                    .sendToTarget();*/
            SocketManager.setInstance(this);

            while (true) {
                try {

                    byte[] header = new byte[HEADER_SIZE]; //read header
                    // Read from the InputStream
                    bytes = iStream.read(header);
                    if (bytes == -1) {
                        Log.d(TAG, "-1");
                        break;
                    }
                    int length = convertByteToInt(Arrays.copyOfRange(header, 0, 4)); //get length
                    int typeOfContent = convertByteToInt(Arrays.copyOfRange(header, 4, 8)); //get length

                    byte[] result;
                    Log.d(TAG, "#######1");
                    Log.d(TAG, "run: "+length);
                    Log.d(TAG, "run: "+typeOfContent);
                    Log.d(TAG, "run: "+header.length);
                    Log.d(TAG, "#######1");
                    int succ=0;
                    switch (typeOfContent) {
                        case PHOTO:
                            break;
                        case PHOTO2:
                            result = new byte[length];
                            bytes = iStream.read(result, 0 ,length);
                            if (bytes == -1) {
                                Log.d(TAG, "-1");
                                break;
                            }

                            int nameLength = convertByteToInt(Arrays.copyOfRange(header, 8, 16));
                            int userId = convertByteToInt(Arrays.copyOfRange(header, 16, 24));
                            Log.d(TAG, "#######2");
                            Log.d(TAG, "run: "+length);
                            Log.d(TAG, "run: "+typeOfContent);
                            Log.d(TAG, "run: "+nameLength);
                            Log.d(TAG, "run: "+userId);
                            Log.d(TAG, "run: "+header.length);
                            Log.d(TAG, "#######2");

                            //handler.obtainMessage(PHOTO,
                            //        userId, nameLength, result).sendToTarget();

                            break;
                        case JSON:
                            result = new byte[length];

                            bytes = iStream.read(result, 0 , length);
                            if (bytes == -1) {
                                Log.d(TAG, "-1");
                                break;
                            }

                            //handler.obtainMessage(JSON,
                            //        length, -1, result).sendToTarget();
                            break;
                        default:
                            succ = -1;
                    }

                    if(succ == -1){
                        break;
                    }
                    //int length = convertByteToInt(buffer);

                    /*byte[]
                     result = new byte[length];
                    Log.d(TAG, "run: hey2" );
                    int totalNrOfPackages = length / BUFFER_SIZE;
                    Log.d(TAG, "run: hey3" );
                    int sizeOfLastPackage = length % BUFFER_SIZE;

                    Log.d(TAG, "run: "+length);
                    byte[] result;
                    //int consumed = 1;
                    if(totalNrOfPackages != 0) {
                        Log.d(TAG, "run total1: ");
                        byte[] newPackage = new byte[length-BUFFER_SIZE];
                        byte[] rest = Arrays.copyOfRange(buffer, 4, BUFFER_SIZE);
                        Log.d(TAG, "run total2: ");
                        bytes = iStream.read(newPackage, 0, length-BUFFER_SIZE);
                        if (bytes == -1) {
                            Log.d(TAG, "-1");
                            break;
                        }
                        Log.d(TAG, "run total3: ");
                        result = joinByteArray(rest, newPackage);
                    }else {
                        result = Arrays.copyOfRange(buffer, 4, sizeOfLastPackage+4);
                    }*/


                    // Send the obtained bytes to the UI Activity

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int receivePhoto(byte[] header) {
        return 0;
    }

    public int convertByteToInt(byte[] b)
    {
        ByteBuffer wrapped = ByteBuffer.wrap(b); // big-endian by default
        return wrapped.getInt(); // 1

    }

    private synchronized static void setInstance(SocketManager socketManager) {
        instance = socketManager;
    }

    public byte[] joinByteArray(byte[] a, byte[] b) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(a);
        out.write(b);
        return out.toByteArray();
    }


    public void sendPhoto(String albumName, String photoName, File photo) {
        Log.d(TAG, "sendPhoto: "+photoName);
        Log.d(TAG, "sendPhoto: "+photo.getName());
        try {
            byte[] photoBytes= Util.fileToByteArray(photo);
            String photoString = Base64.encodeToString(photoBytes, Base64.NO_WRAP);

            JSONObject content = new JSONObject();

            content.put("user_id", UserSessionDetails.user_id);
            content.put("photo_name", photoName);
            content.put("album_name", albumName);
            content.put("photo", photoString);
            System.out.println(photoString);
            Log.d(TAG, photoString);

            sendJson(content);



        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void sendPhoto2(String photoName, File photo) {
        try {


            Log.d(TAG, "sendPhoto: "+photoName);
            Log.d(TAG, "sendPhoto: "+photo.getName());
            //build content
            byte[] photoBytes = Util.fileToByteArray(photo);

            final byte[] content = joinByteArray(photoName.getBytes(), photoBytes);
            Log.d(TAG, "sendPhoto: "+content.length);
            //build header
            byte[] contentSize = ByteBuffer.allocate(4).putInt(content.length).array();
            byte[] typeOfContent = ByteBuffer.allocate(4).putInt(PHOTO).array();
            byte[] arg1 = ByteBuffer.allocate(8).putInt(photoName.getBytes().length).array();
            byte[] arg2 = ByteBuffer.allocate(8).putInt(UserSessionDetails.user_id).array();
            Log.d(TAG, "#######");
            Log.d(TAG, "sendPhoto: "+content.length);
            Log.d(TAG, "sendPhoto:" + convertByteToInt(contentSize));
            Log.d(TAG, "sendPhoto: "+PHOTO);
            Log.d(TAG, "sendPhoto: "+photoName.getBytes().length);
            Log.d(TAG, "sendPhoto: "+UserSessionDetails.user_id);


            ByteArrayOutputStream header = new ByteArrayOutputStream();
            header.write(contentSize);
            header.write(typeOfContent);
            header.write(arg1);
            header.write(arg2);
            final byte[] headerToSend = header.toByteArray();
            Log.d(TAG, "sendPhoto: "+headerToSend.length);
            Log.d(TAG, "#######");

            Thread thread = new Thread() {
                public void run() {
                    try {
                        oStream.write(headerToSend);
                        oStream.flush();
                        oStream.write(content);
                        oStream.flush();
                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write", e);
                    }
                }
            };
            //thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendJson(JSONObject json) throws IOException {
        final byte[] content = json.toString().getBytes();

        //build header
        byte[] contentSize = ByteBuffer.allocate(4).putInt(content.length).array();
        byte[] typeOfContent = ByteBuffer.allocate(4).putInt(JSON).array();
        byte[] arg1 = ByteBuffer.allocate(8).putInt(0).array();
        byte[] arg2 = ByteBuffer.allocate(8).putInt(0).array();
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        header.write(contentSize);
        header.write(typeOfContent);
        header.write(arg1);
        header.write(arg2);
        final byte[] headerToSend = header.toByteArray();

        Log.d(TAG, "##### SENT BYTES #######");
        Log.d(TAG,json.toString());
        Log.d(TAG,Base64.encodeToString(content, Base64.NO_WRAP));
        Log.d(TAG,String.valueOf(json.toString().getBytes().length));
        Log.d(TAG, "############");


        Thread thread = new Thread() {
            public void run() {
                try {
                    if(oStream == null)
                        oStream = socket.getOutputStream();
                    oStream.write(headerToSend);
                    int count;
                    byte[] buffer = new byte[8*1024];
                    ByteArrayInputStream bis = new ByteArrayInputStream(content);
                    while((count = bis.read(buffer)) > 0) {
                        oStream.write(buffer, 0 , count);
                    }
                    oStream.close();
                    bis.close();


                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                }
            }
        };
        thread.start();

    }

    public void write(String msg) throws IOException {

        final byte[] buffer = msg.getBytes();
        Log.d(TAG, "write1: "+buffer.length);
        //final byte[] buff = {(byte)buffer.length};
        final byte[] buff = ByteBuffer.allocate(4).putInt(buffer.length).array();
        Log.d(TAG, "write2: "+convertByteToInt(buff));
        final byte[] result = joinByteArray(buff, buffer);
        Log.d(TAG, "write3: "+buff.length);
        Thread thread = new Thread() {
            public void run() {
                try {
                    oStream.write(result);
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                }
            }
        };
        thread.start();
    }

}
