package pt.ulisboa.tecnico.cnv.autoScaler;

import com.amazonaws.services.ec2.model.Instance;
import pt.ulisboa.tecnico.cnv.cloudManager.CloudManager;
import pt.ulisboa.tecnico.cnv.policies.LoadComplexity;

import java.util.*;

public class AWSMetricsAutoScaler extends AutoScaler {

    private static final int MIN_AVAILABLE_INSTANCES = 3;
    private static final int MAX_AVAILABLE_INSTANCES = 20;

    private static final double SCALE_DOWN_REQUESTS = 1d;
    private static final double SCALE_UP_REQUESTS = 3d;
    private static final int SCALE_DOWN_COOLDOWN = 2;


    public static final LoadComplexity MAX_COMPLEXITY = new LoadComplexity(15000000);
    public static final LoadComplexity AVG_COMPLEXITY = new LoadComplexity(6000000);
    public static final LoadComplexity MIN_COMPLEXITY = new LoadComplexity(4000000);
    private static int scaleDownCoolDown = 0;
    private static AWSMetricsAutoScaler instance;
    private CloudManager manager;

    public HashMap<String, HashMap<String, LoadComplexity>> getInstancesLoad() {
        return instancesLoad;
    }

    private HashMap<String, HashMap<String, LoadComplexity>> instancesLoad;
    private ArrayList<Instance> availableInstances;
    private int scalingUp;
    private int scalingDown;

    public AWSMetricsAutoScaler() {
        manager = CloudManager.getInstance();
        availableInstances = manager.getAvailableInstances();
        scalingUp = 0;
        scalingDown = 0;
        instancesLoad = new HashMap<>();
        for(Instance i : availableInstances) {
            instancesLoad.put(i.getInstanceId(), new HashMap<>());
        }
    }

    public synchronized static AutoScaler getAutoScaler() {
        if(instance == null) {
            instance = new AWSMetricsAutoScaler();
        }
        return instance;
    }

    public synchronized void removeInstance(Instance i) {
            manager.destroyInstance(i.getInstanceId());
            instancesLoad.remove(i.getInstanceId());
            availableInstances.remove(i);
    }

    @Override
    public synchronized LoadComplexity computeOverallLoad() {
            if(instancesLoad.values().size() == 0) {

                return new LoadComplexity(0);
            }
            int total = 0;
            for(HashMap<String, LoadComplexity> requests : instancesLoad.values()) {
                for(LoadComplexity complexity: requests.values())
                    total += Long.min(MAX_COMPLEXITY.getComplexity(), complexity.getComplexity());
            }
            return new LoadComplexity(total / (instancesLoad.values().size() + scalingUp - scalingDown));
    }

    public synchronized double computeOverallRequests() {
        if(instancesLoad.values().size() == 0) {
            return 0;
        }
        int total = 0;
        for(HashMap<String, LoadComplexity> requests : instancesLoad.values()) {
            total += requests.size();
        }
        return total / (instancesLoad.values().size() + scalingUp - scalingDown);
    }

    @Override
    public boolean ShouldScaleUp() {
        if(availableInstances.size() + scalingUp > MAX_AVAILABLE_INSTANCES )
            return false;
        LoadComplexity currentLoad = computeOverallLoad();
        double avg = computeOverallRequests();
        if(availableInstances.size()+scalingUp < MIN_AVAILABLE_INSTANCES || AVG_COMPLEXITY.lt(currentLoad) || avg > SCALE_UP_REQUESTS) {
            return true;
        }else {
            return false;
        }
    }

    public synchronized void updateAvailableInstances(ArrayList<Instance> allInstances) {
        if( scalingUp > 0 && allInstances.size() > availableInstances.size() ){
            scalingUp -= allInstances.size() - availableInstances.size();
            scalingUp = Math.max(0, scalingUp);
        }
        if( scalingDown > 0  && allInstances.size() < availableInstances.size() ){
            scalingDown += allInstances.size() - availableInstances.size();
            scalingDown = Math.max(0, scalingDown);
        }
        for(Instance i : availableInstances){
            boolean found = false;
            for(Instance j : allInstances){
                if( i.getInstanceId().equals(j.getInstanceId())){
                    found = true;
                    break;
                }
            }
            if( !found ){
                instancesLoad.remove(i);
            }
        }
        availableInstances = new ArrayList<>();

        for(Instance i : allInstances){
            availableInstances.add(i);
            if( !instancesLoad.containsKey(i.getInstanceId()) ){
                instancesLoad.put(i.getInstanceId(), new HashMap<>()); // TODO: put 0 here when it changes to long
            }
        }
    }

    @Override
    public boolean ShouldScaleDown() {
        LoadComplexity currentLoad = computeOverallLoad();
        double avg = computeOverallRequests();
        if((currentLoad.lt(MIN_COMPLEXITY) || avg < SCALE_DOWN_REQUESTS ) && availableInstances.size() > MIN_AVAILABLE_INSTANCES ) {
            return true;
        }else {
            return false;
        }
    }

    public boolean ShouldScaleDownMonitor() {
        if(scaleDownCoolDown-- != 0)
            return false;
        return ShouldScaleDown();
    }


    @Override
    public synchronized void scaleUp() {
        LoadComplexity currentLoad = computeOverallLoad();
        int nrOfInstancesToSpin = 1;
        //int nrOfInstancesToSpin = Math.max(LoadComplexity.COMPLEXITIES.MIN.value.complexity+MIN_AVAILABLE_INSTANCES,
        //                                currentLoad.sub(LoadComplexity.COMPLEXITIES.MAX.value).complexity + MIN_AVAILABLE_INSTANCES);

        //nrOfInstancesToSpin -= scalingUp;
        nrOfInstancesToSpin = Math.max(0, nrOfInstancesToSpin);

        System.out.println("Scaling up");

        List<Instance> instances = manager.launchNewInstance(nrOfInstancesToSpin);

        System.out.println("Scaled up "+ Integer.toString(nrOfInstancesToSpin));
        scalingUp += nrOfInstancesToSpin;
    }

    @Override
    public synchronized void scaleDown() {
        LoadComplexity currentLoad = computeOverallLoad();

        int nrOfInstancesToRemove = 1;

        //int nrOfInstancesToRemove = Math.max(0,LoadComplexity.COMPLEXITIES.MAX.value.complexity - (int)(currentLoad.complexity*0.6));

        //nrOfInstancesToRemove -= scalingDown;
        nrOfInstancesToRemove = Math.max(0, nrOfInstancesToRemove);

        if ((availableInstances.size() - nrOfInstancesToRemove) < MIN_AVAILABLE_INSTANCES) {
                nrOfInstancesToRemove = availableInstances.size() - MIN_AVAILABLE_INSTANCES;
        }

        if(instancesLoad.values().size() <= MIN_AVAILABLE_INSTANCES) {
            return;
        }

        ArrayList<Instance> toRemove = new ArrayList<>();
        for(Instance i : availableInstances) {

            if(instancesLoad.get(i.getInstanceId()).size() == 0 &&
            toRemove.size() < nrOfInstancesToRemove) {
                scaleDownCoolDown = SCALE_DOWN_COOLDOWN;
                toRemove.add(i);
            }
        }
        for(Instance i: toRemove) {
            removeInstance(i);
        }
        if(toRemove.size() > 0)
            System.out.println("Scaled down "+ toRemove.size());
        scalingDown = nrOfInstancesToRemove;
    }


    public synchronized void addInstance(Instance i) {
        instancesLoad.put(i.getInstanceId(), new HashMap<>());
        availableInstances.add(i);
    }

    @Override
    public synchronized List<Instance> getAvailableInstances() {
        ArrayList<Instance> available = new ArrayList<>();
        //System.out.println(availableInstances.size());
        for(Instance i : availableInstances) {
            if(manager.isAvailable(i))
                available.add(i);
        }
        available.sort(Comparator.comparing(Instance::getInstanceId));
        return available;

    }

    @Override
    public synchronized long getInstanceLoad(Instance i) {
        HashMap<String, LoadComplexity> loads = instancesLoad.get(i.getInstanceId());
        long sum = 0;
        for(LoadComplexity lc : loads.values())
            sum += lc.getComplexity();
        return sum;
    }

    @Override
    public synchronized int getInstanceRequests(Instance i) {
        HashMap<String, LoadComplexity> loads = instancesLoad.get(i.getInstanceId());
        return loads.size();

    }

    public synchronized void printav() {
        for(Instance i : availableInstances) {
            ArrayList<String> total_compl = new ArrayList<>();
            for(LoadComplexity compl: instancesLoad.get(i.getInstanceId()).values())
                total_compl.add(Long.toString(compl.getComplexity()));
            System.out.println(i.getInstanceId() + "(" + instancesLoad.get(i.getInstanceId()).size() + ")" + "=" + String.join("\t", total_compl));
        }
    }

    @Override
    public synchronized void notifyRequestStart(Instance instance, LoadComplexity complexity, String uuid) {
            HashMap<String, LoadComplexity> compl = this.instancesLoad.get(instance.getInstanceId());
            compl.put(uuid, complexity);


        /*if(ShouldScaleUp()) {
            scaleUp();
        }*/

    }

    @Override
    public synchronized void notifyRequestEnd(Instance instance, String uuid) {
            HashMap<String, LoadComplexity> compl = this.instancesLoad.get(instance.getInstanceId());
            compl.remove(uuid);

            /*if(ShouldScaleDown()) {
                scaleDown();
            }*/
    }

    public synchronized void updateRequestProgress(Instance instance, String uuid, long progress) {
        HashMap<String, LoadComplexity> compl = this.instancesLoad.get(instance.getInstanceId());
        compl.get(uuid).setProgress(progress);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
