package pt.ulisboa.tecnico.cnv.server;

import com.amazonaws.services.ec2.model.Instance;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.policies.LoadComplexity;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReverseProxyRequester implements Runnable {

    private Instance instance;
    private String urlParameters;
    private AutoScaler autoScaler;
    private LoadComplexity compl;
    private HttpExchange t;
    private Exception e;

    public ReverseProxyRequester(Instance instance, String urlParameters, AutoScaler autoScaler, LoadComplexity compl, HttpExchange t){
        this.instance = instance;
        this.urlParameters = urlParameters;
        this.autoScaler = autoScaler;
        this.compl = compl;
        this.t = t;
    }

    @Override
    public void run(){
        try {
            String ip = instance.getPublicIpAddress();
            URL url = new URL("http",ip, 8000, "/climb?"+urlParameters);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            int responseCode = con.getResponseCode();
            StringBuffer response = new StringBuffer();

            //Get Response
            DataInputStream is = new DataInputStream((con.getInputStream()));
            byte[] buffer = new byte[con.getContentLength()];
            is.readFully(buffer);

            try {final Headers hdrs = t.getResponseHeaders();

                hdrs.add("Content-Type", "image/png");
                hdrs.add("Access-Control-Allow-Origin", "*");
                hdrs.add("Access-Control-Allow-Credentials", "true");
                hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
                hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

                String strResponse = response.toString();
                t.sendResponseHeaders(responseCode, strResponse.getBytes().length);
                final OutputStream os = t.getResponseBody();
                os.write(buffer);
                os.close();
                System.out.println("> Sent response to " + t.getRemoteAddress().toString());
            } catch ( Exception e) {
                System.out.println("ERROR SENDING BACK TO REQUESTER");
                return;
            }

        }
        catch (Exception e){
            e.printStackTrace();
            this.e = e;

        }
    }

    public synchronized Exception getException() {
        return e;
    }
}
