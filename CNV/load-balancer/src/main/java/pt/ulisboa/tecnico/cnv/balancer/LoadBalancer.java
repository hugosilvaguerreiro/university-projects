package pt.ulisboa.tecnico.cnv.balancer;

import com.amazonaws.services.ec2.model.Instance;
import pt.ulisboa.tecnico.cnv.policies.LoadComplexity;

public abstract class LoadBalancer {
    public abstract void start();
    public abstract void stop();

    public abstract Instance getNextInstance(LoadComplexity compl);
}
