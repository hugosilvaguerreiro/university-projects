package pt.ulisboa.tecnico.cnv.monitors;

import pt.ulisboa.tecnico.cnv.autoScaler.AWSMetricsAutoScaler;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScalerFactory;

public class AutoscalingMonitor implements Runnable {
    private int probeTime;
    private AutoScaler autoScaler;

    public AutoscalingMonitor(int probeTime) {
        this.probeTime = probeTime;
        this.autoScaler = AutoScalerFactory.getInstance(AutoScalerFactory.ASTYPE.CPU);
    }

    @Override
    public void run() {
        System.out.println("AUTOSCALING MONITOR HEY");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                try{
                    if(this.autoScaler.ShouldScaleUp()) {
                        this.autoScaler.scaleUp();
                    }
                    if(this.autoScaler.ShouldScaleDown()) {
                        this.autoScaler.scaleDown();
                    }
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

        System.out.println("AUTOSCALING MONITOR BYE");
    }
}
