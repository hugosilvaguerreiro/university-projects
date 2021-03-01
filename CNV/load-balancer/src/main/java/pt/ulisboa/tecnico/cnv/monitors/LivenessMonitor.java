package pt.ulisboa.tecnico.cnv.monitors;

import com.amazonaws.services.ec2.model.Instance;
import pt.ulisboa.tecnico.cnv.autoScaler.AWSMetricsAutoScaler;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScalerFactory;
import pt.ulisboa.tecnico.cnv.cloudManager.CloudManager;

import java.util.ArrayList;

public class LivenessMonitor implements Runnable {
    private int probeTime;
    private int count = 0;
    private AWSMetricsAutoScaler autoScaler;
    private CloudManager manager;

    public LivenessMonitor(int probeTime) {
        this.probeTime = probeTime;
        this.autoScaler = (AWSMetricsAutoScaler) AutoScalerFactory.getInstance(AutoScalerFactory.ASTYPE.CPU);
        this.manager = CloudManager.getInstance();
    }

    @Override
    public void run() {
        System.out.println("LIVENESS MONITOR");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                try{
                    count += 1;
                    final ArrayList<Instance> instances = manager.getAvailableInstances();
                    System.out.println("## " + String.valueOf(count) + " ##");
                    System.out.println(instances.size());
                    autoScaler.printav();
                    System.out.println("#####");
                    this.autoScaler.updateAvailableInstances(instances);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Thread.sleep(this.probeTime);

            } catch (InterruptedException e) {
                // good practice
                Thread.currentThread().interrupt();
                return;
            }
        }

        System.out.println("LIVENESS MONITOR BYE");
    }
}
