package cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import cmov.tecnico.ulisboa.pt.p2photo.UserSessionDetails;

import static cmov.tecnico.ulisboa.pt.p2photo.wifidirect.activities.WiFiDirectActivity.MESSAGE_READ;
import static cmov.tecnico.ulisboa.pt.p2photo.wifidirect.handlers.SocketManager.TAG;

public class MessageHandler implements Handler.Callback {

    public static final int SET_SOCKET_MANAGER = 0x400 + 2;
    public static final int PHOTO = 1;
    public static final int JSON = 2;

    public SocketManager manager = null;

    public Context ctx;
    public MessageHandler(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case PHOTO:
                // message.obj: byte array containing [[photo name][photo bytes]]
                // message.arg1: user id
                // message.arg2: size of the photo name
                //photoName is in the format albumName@userid@photoname
                String photoName = new String(Arrays.copyOfRange((byte[])message.obj, 0, message.arg2));
                String[] parts = photoName.split("@"+message.arg1+"@");
                String albumName = parts[0];
                String name = parts[1];
                Log.d(TAG, "//////////////");
                Log.d(TAG, "sendPhoto: "+ ((byte[])message.obj).length);
                Log.d(TAG, "sendPhoto: "+ photoName);
                Log.d(TAG, "sendPhoto: "+ albumName);
                Log.d(TAG, "sendPhoto: "+ name);
                Log.d(TAG, "sendPhoto: "+ message.arg1);
                Log.d(TAG, "sendPhoto: "+ message.arg2);
                byte[] photo = Arrays.copyOfRange((byte[])message.obj, message.arg2, ((byte[])message.obj).length);
                Log.d(TAG, "->>>>>sendPhoto: "+ photo.length);
                Log.d(TAG, "->>>>>sendPhoto: "+ message.arg2);
                Log.d(TAG, "->>>>>sendPhoto: "+ ((byte[])message.obj).length);
                Log.d(TAG, "->>>>>sendPhoto: "+ photo.length + message.arg2);

                Log.d(TAG, "//////////////");
                try {
                    (new File(ctx.getFilesDir(),  "wifi-direct/" + albumName)).mkdirs();
                    (new File(ctx.getFilesDir(), "wifi-direct/" + albumName + "/" + message.arg1)).mkdirs();
                    (new File(ctx.getFilesDir(), "wifi-direct/" + albumName + "/" + message.arg1 + "/" + name)).createNewFile();
                    FileOutputStream os = new FileOutputStream(new File(ctx.getFilesDir(),
                            "wifi-direct/" + albumName + "/" + message.arg1 + "/" + name));

                    os.write(photo);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case JSON:
                //Log.d(TAG, "##### RECEIVED BYTES #######");
                //System.out.println(Base64.encodeToString((byte[])message.obj, Base64.NO_WRAP));
                //Log.d(TAG, "############");

                JSONObject cont = (JSONObject)message.obj;
                try {
                    byte[] recvPhoto = Base64.decode(cont.getString("photo"), Base64.NO_WRAP);
                    String album_name = cont.getString("album_name");
                    int user_id = cont.getInt("user_id");
                    String photo_name = cont.getString("photo_name");


                    (new File(ctx.getFilesDir(),  "wifi-direct/" + cont.getString("album_name"))).mkdirs();
                    (new File(ctx.getFilesDir(), "wifi-direct/" + album_name + "/" + user_id)).mkdirs();
                    (new File(ctx.getFilesDir(), "wifi-direct/" + album_name + "/" + user_id + "/" + photo_name)).createNewFile();
                    FileOutputStream os = new FileOutputStream(new File(ctx.getFilesDir(),
                            "wifi-direct/" + album_name + "/" + user_id + "/" + photo_name));
                    os.write(recvPhoto);

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Log.d(TAG, Util.parseJson(new String((byte[])message.obj)).toString());
                break;

            case MESSAGE_READ:
                byte[] readBuf = (byte[]) message.obj;
                Log.d("SocketManager", "handleMessage:" + new String(readBuf));
                // construct a string from the valid bytes in the buffer
                //String readMessage = new String(readBuf, 0, message.arg1);
                //int b = message.;
                //(chatFragment).pushMessage("Buddy: " + readMessage);
                break;
            case SET_SOCKET_MANAGER:
                Object obj = message.obj;
                this.manager = ((SocketManager) obj);
        }
        return true;
    }

}
