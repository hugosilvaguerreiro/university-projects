package pt.ulisboa.tecnico.cnv.monitors;

import pt.ulisboa.tecnico.cnv.cloudManager.CloudManager;
import pt.ulisboa.tecnico.cnv.dataObject.Metric;
import pt.ulisboa.tecnico.cnv.policies.AreaCalc;
import pt.ulisboa.tecnico.cnv.policies.DistanceCalc;

import java.util.List;

public class MetricsMonitor implements Runnable{
    private CloudManager manager;
    private int probeTime;
    private AreaCalc areaCalc;
    private DistanceCalc distanceCalc;

    public MetricsMonitor(int probeTime) {
        this.probeTime = probeTime;
        this.manager = CloudManager.getInstance();
        this.areaCalc = AreaCalc.getInstance();
        this.distanceCalc = DistanceCalc.getInstance();
    }

    @Override
    public void run() {
        System.out.println("METRICS MONITOR");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                try{
                    List<Metric> metrics = manager.getMetrics();
                    System.out.println("## NEW METRICS =  " + Integer.toString(metrics.size()) + " ##");
                    for(Metric m : metrics) {
                        //System.out.println(m.toString());
                        this.areaCalc.updateCalculator(m);
                        this.distanceCalc.updateCalculator(m);
                    }
                    System.out.println("#####");

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
