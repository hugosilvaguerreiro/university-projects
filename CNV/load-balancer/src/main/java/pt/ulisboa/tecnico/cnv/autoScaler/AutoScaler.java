package pt.ulisboa.tecnico.cnv.autoScaler;

import com.amazonaws.services.ec2.model.Instance;
import pt.ulisboa.tecnico.cnv.policies.LoadComplexity;

import java.util.List;

public abstract class AutoScaler {
    public abstract List<Instance> getAvailableInstances();

    public abstract void start();
    public abstract void stop();

    public abstract void notifyRequestStart(Instance instance, LoadComplexity complexity, String uuid);
    public abstract void notifyRequestEnd(Instance instance, String uuid);
    public abstract void addInstance(Instance instance);
    public abstract void removeInstance(Instance instance);


    public abstract long getInstanceLoad(Instance i);
    public abstract int getInstanceRequests(Instance i);
    public abstract LoadComplexity computeOverallLoad();
    public abstract boolean ShouldScaleUp();
    public abstract boolean ShouldScaleDown();
    public abstract void scaleUp();
    public abstract void scaleDown();

}

