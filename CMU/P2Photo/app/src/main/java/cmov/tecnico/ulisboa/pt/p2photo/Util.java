package cmov.tecnico.ulisboa.pt.p2photo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    public static boolean isUsernameInvalid(String username) {
        Pattern p = Pattern.compile("[a-zA-Z0-9_]{5,}[a-zA-Z0-9_]*");
        Matcher m = p.matcher(username);
        return !m.matches();
    }

    public static boolean isPasswordInvalid(String password) {
        Pattern p = Pattern.compile("[a-zA-Z0-9_]{5,}[a-zA-Z0-9_]*");
        Matcher m = p.matcher(password);
        return !m.matches();
    }

    public static boolean copyFile(File origin, File target) {
        try {
            if (!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
            }
            if (!target.exists()) {
                    target.createNewFile();
            }

            FileChannel source = new FileInputStream(origin).getChannel();
            FileChannel destination = new FileOutputStream(target).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            return true;
        }

    }
    public static byte[] fileToByteArray(File file) throws IOException {
        byte [] mybytearray  = new byte [(int)file.length()];
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(mybytearray,0,mybytearray.length);
        return mybytearray;
    }

    public static String fileToString(File file) throws IOException {
        //JSONParser
        //JSONArray a = (JSONArray) JSONParser.parse(new FileReader("c:\\exer4-courses.json"));
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        String ls = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            //stringBuilder.append(ls);
        }
        // delete the last new line separator
        //stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        reader.close();

        return stringBuilder.toString();
    }

    public static void writeStringToFile(File file, String content) throws IOException {
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();
    }

    public static JSONArray parseJsonArray(String json) {
        try {
            JSONArray obj = new JSONArray(json);
            return obj;
        } catch (Throwable t) {
            Log.e("P2Photo", "Could not parse malformed JSON: \"" + json + "\"");
        }
        return null;
    }
    public static JSONObject parseJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            return obj;
        } catch (Throwable t) {
            Log.e("P2Photo", "Could not parse malformed JSON: \"" + json + "\"");
        }
        return null;
    }

    public static void showProgress(final View v, final View pv, final boolean show) {
        int shortAnimTime = 200;

        v.setVisibility(show ? View.GONE : View.VISIBLE);
        v.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        v.setVisibility(show ? View.VISIBLE : View.GONE);
        pv.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                pv.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
