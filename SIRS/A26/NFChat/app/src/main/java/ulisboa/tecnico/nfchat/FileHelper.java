package ulisboa.tecnico.nfchat;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileHelper {

    public static String readFile(String filename, Context ctx) {
        File file = new File(ctx.getFilesDir(), filename);
        String line = "";
        try {

            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);


            StringBuilder stringBuilder = new StringBuilder();
            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;

    }

    public static  boolean saveToFile(String filename, Context ctx, String data) {
        return saveToFile(filename, ctx, data, false);
    }

    public static boolean saveToFile(String filename, Context ctx, String data, boolean append){
        try {
            File file = new File(ctx.getFilesDir(), filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file,append);

            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return  false;
        }

    }
}
