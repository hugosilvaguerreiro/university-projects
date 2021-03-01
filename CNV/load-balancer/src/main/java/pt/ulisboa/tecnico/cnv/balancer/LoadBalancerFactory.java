package pt.ulisboa.tecnico.cnv.balancer;

public class LoadBalancerFactory {
    public enum LBTYPE { ROUND_ROBIN, SMARTER, CUSTOM };

    public static LoadBalancer getInstance(LBTYPE type) {
        switch (type) {
            case SMARTER:
                return SmarterBalancer.getLoadBalancer();
            case ROUND_ROBIN:
                return RoundRobinBalancer.getLoadBalancer();
            default:
                return null;
        }
    }
}
