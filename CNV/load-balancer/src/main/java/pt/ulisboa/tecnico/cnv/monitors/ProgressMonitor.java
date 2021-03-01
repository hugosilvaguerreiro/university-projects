package pt.ulisboa.tecnico.cnv.monitors;

import com.amazonaws.services.ec2.model.Instance;
import com.google.gson.Gson;
import pt.ulisboa.tecnico.cnv.autoScaler.AWSMetricsAutoScaler;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScalerFactory;
import pt.ulisboa.tecnico.cnv.dataObject.Metric;
import pt.ulisboa.tecnico.cnv.dataObject.Metric;
import pt.ulisboa.tecnico.cnv.policies.LoadComplexity;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressMonitor implements Runnable {
    private int probeTime;
    private AWSMetricsAutoScaler autoScaler;


    public ProgressMonitor(int probeTime) {
        this.probeTime = probeTime;
        this.autoScaler = (AWSMetricsAutoScaler)AutoScalerFactory.getInstance(AutoScalerFactory.ASTYPE.CPU);

    }

    public void updateRequestProgress(Instance instance, String uuid, AWSMetricsAutoScaler autoScaler) {
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
        URL url = null;
        try {
            url = new URL("http://"+instance.getPublicIpAddress()+":8000/metrics");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            int responseCode = con.getResponseCode();
            DataInputStream is = new DataInputStream((con.getInputStream()));
            byte[] buffer = new byte[con.getContentLength()];
            is.readFully(buffer);
            if(responseCode == 200) {
                String json = new String(buffer);
                Metric[] metrics = new Gson().fromJson(json, Metric[].class);

                Metric m = null;
                for(Metric metric : metrics) {
                    if(metric.uuid.equals(uuid))
                        m =  metric;
                }
                if(m != null){
                    autoScaler.updateRequestProgress(instance, uuid, m.mCount);
                }
            }

            //[{"parameters":{"solver_strategy":"BFS","y0":"0","y1":"512","start_y":"0","start_x":"0","x0":"0","input_image":"datasets/RANDOM_HILL_512x512_2019-02-27_09-46-42.dat","x1":"512"},"mCount":7328258,"uuid":"d1c9649d-f0ec-4bec-b8d0-214a1012b196"}]
            //JSONObject obj = new JSONObject();
            //Gson gson = new Gson();

            //JsonElement jsonElement = gson.toJsonTree(json);
            //JsonE
            //convertedObject

            System.out.println(new String(buffer));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//            }
//        });
//        t.start();

    }

    @Override
    public void run() {
        System.out.println("PROGRESS MONITOR HEY");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                try{
                    List<Instance> a = this.autoScaler.getAvailableInstances();
                    System.out.println("======= PROGRESS MONITOR =========");
                    for(Instance ins : this.autoScaler.getAvailableInstances()) {
                        HashMap<String, HashMap<String, LoadComplexity>> instancesLoad = this.autoScaler.getInstancesLoad();

                        HashMap<String, LoadComplexity> instances = instancesLoad.get(ins.getInstanceId());

                        for(Map.Entry<String, LoadComplexity> entry : instances.entrySet()) {
                            if(entry.getValue().gt(new LoadComplexity(0))) {
                                updateRequestProgress(ins, entry.getKey(), this.autoScaler);
                                System.out.println("Updating "+ins.getInstanceId() +" "+entry.getKey()+" "+this.autoScaler);
                            }
                        }
                    }
                    System.out.println("==================================");

                } catch (Exception e) {
                    //e.printStackTrace();
                }
                Thread.sleep(this.probeTime);

            } catch (InterruptedException e) {
                //good practice
                Thread.currentThread().interrupt();
                return;
            }
        }

        System.out.println("AUTOSCALING MONITOR BYE");
    }
}
