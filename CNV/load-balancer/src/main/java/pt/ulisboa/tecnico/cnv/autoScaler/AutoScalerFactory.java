package pt.ulisboa.tecnico.cnv.autoScaler;

import pt.ulisboa.tecnico.cnv.balancer.LoadBalancer;
import pt.ulisboa.tecnico.cnv.balancer.LoadBalancerFactory;
import pt.ulisboa.tecnico.cnv.balancer.RoundRobinBalancer;

public class AutoScalerFactory {
    public enum ASTYPE { CPU, CUSTOM };

    public synchronized static AutoScaler getInstance(ASTYPE type) {
        switch (type) {
            case CPU:
                return AWSMetricsAutoScaler.getAutoScaler();
            default:
                return null;
        }
    }
}
