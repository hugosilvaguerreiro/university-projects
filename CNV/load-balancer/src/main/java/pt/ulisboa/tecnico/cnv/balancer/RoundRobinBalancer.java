package pt.ulisboa.tecnico.cnv.balancer;

import com.amazonaws.services.ec2.model.Instance;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScaler;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScalerFactory;
import pt.ulisboa.tecnico.cnv.cloudManager.CloudManager;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScalerFactory.ASTYPE;
import pt.ulisboa.tecnico.cnv.policies.LoadComplexity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;



public class RoundRobinBalancer extends LoadBalancer{
    private static RoundRobinBalancer balancerInstance;
    private CloudManager manager;
    private AutoScaler autoScaler;
    private ArrayList<Instance> instances;
    private AtomicInteger index = new AtomicInteger(0);


    private RoundRobinBalancer() {
        manager = CloudManager.getInstance();
        autoScaler = AutoScalerFactory.getInstance(ASTYPE.CPU);
        //instances = manager.getAvailableInstances();
    }

    public synchronized static RoundRobinBalancer getLoadBalancer() {
        if(balancerInstance == null) {
            balancerInstance = new RoundRobinBalancer();
        }
        return balancerInstance;
    }

    @Override
    public synchronized Instance getNextInstance(LoadComplexity compl) {
        //ArrayList<Instance> getAvailableInstances = manager.getAvailableInstances();
        ArrayList<Instance> availableInstances = (ArrayList<Instance>) autoScaler.getAvailableInstances();
        while (availableInstances.size() == 0) {
            autoScaler.scaleUp();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(instances == null) {
            instances = availableInstances;
        }
        System.out.println("///////");
        System.out.println(instances.size());

        Random r = new Random();
        int ind = r.nextInt(instances.size());
        System.out.println(ind);
        System.out.println("///////");
        index.set(ind);
        instances = availableInstances;
        return instances.get(ind);
    }

    public LoadComplexity computeComplexity(HashMap<String, String> params) {
        return new LoadComplexity(10);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}






