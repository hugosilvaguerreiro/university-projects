package pt.ulisboa.tecnico.cnv.balancer;

import com.amazonaws.services.ec2.model.Instance;
import pt.ulisboa.tecnico.cnv.autoScaler.AWSMetricsAutoScaler;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScalerFactory;
import pt.ulisboa.tecnico.cnv.cloudManager.CloudManager;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScalerFactory.ASTYPE;
import pt.ulisboa.tecnico.cnv.policies.AreaCalc;
import pt.ulisboa.tecnico.cnv.policies.ComplexityEstimator;
import pt.ulisboa.tecnico.cnv.policies.DistanceCalc;
import pt.ulisboa.tecnico.cnv.policies.LoadComplexity;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;


public class SmarterBalancer extends LoadBalancer{
    private static SmarterBalancer balancerInstance;
    private CloudManager manager;
    private AutoScaler autoScaler;
    private AreaCalc areaComplCalc;
    private DistanceCalc distanceCalc;
    private Random rand;

    private SmarterBalancer() {
        this.manager = CloudManager.getInstance();
        this.autoScaler = AutoScalerFactory.getInstance(ASTYPE.CPU);
        this.rand = new Random();
        this.areaComplCalc = AreaCalc.getInstance();
        this.distanceCalc = DistanceCalc.getInstance();
        //instances = manager.getAvailableInstances();
    }

    public synchronized static SmarterBalancer getLoadBalancer() {
        if(balancerInstance == null) {
            balancerInstance = new SmarterBalancer();
        }
        return balancerInstance;
    }

    @Override
    public Instance getNextInstance(LoadComplexity compl) {
        //ArrayList<Instance> getAvailableInstances = manager.getAvailableInstances();
        List<Instance> availableInstances = autoScaler.getAvailableInstances();
        AutoScaler autoScaler = AWSMetricsAutoScaler.getAutoScaler();

        while (availableInstances.size() == 0) {
            autoScaler.scaleUp();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            availableInstances = autoScaler.getAvailableInstances();
        }
        System.out.println("////////");
        System.out.println("N instances: " + availableInstances.size());

        int min = 0;

        boolean shouldScaleDown = autoScaler.ShouldScaleDown();

        for(int i = 0; i < availableInstances.size(); i++){
            if(shouldScaleDown) {
                if((autoScaler.getInstanceLoad(availableInstances.get(i)) < autoScaler.getInstanceLoad(availableInstances.get(min))) &&
                autoScaler.getInstanceRequests(availableInstances.get(i)) != 0) min = i;
            } else {
                if(autoScaler.getInstanceLoad(availableInstances.get(i)) < autoScaler.getInstanceLoad(availableInstances.get(min))) min = i;

            }
        }

        System.out.println("sending to:" + availableInstances.get(min).getInstanceId());
        System.out.println("////////");
        return availableInstances.get(min);
    }

    public LoadComplexity computeComplexity(HashMap<String, String> params) {
        System.out.println(params);
        LoadComplexity c = ComplexityEstimator.getComplexity(params);
        if(c == null) {
            c = new LoadComplexity((long) (areaComplCalc.getComplexity(params).getComplexity() *0.5 + distanceCalc.getComplexity(params).getComplexity() * 0.5));
            if( c.getComplexity() == 0 ){
                System.out.println("Complexity was 0 using a minimum value");
                c = AWSMetricsAutoScaler.MIN_COMPLEXITY;
            }
            System.out.println("Estimated Complexity by area and starting point: " + c.getComplexity());
        } else {
            System.out.println("Estimated Complexity by past values: " + c.getComplexity());
        }
        return c;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}






