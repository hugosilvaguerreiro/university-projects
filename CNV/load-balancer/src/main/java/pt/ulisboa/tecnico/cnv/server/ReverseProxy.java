package pt.ulisboa.tecnico.cnv.server;

import com.amazonaws.services.ec2.model.Instance;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.autoScaler.AWSMetricsAutoScaler;
import pt.ulisboa.tecnico.cnv.autoScaler.AutoScalerFactory;
import pt.ulisboa.tecnico.cnv.balancer.LoadBalancerFactory;
import pt.ulisboa.tecnico.cnv.balancer.LoadBalancerFactory.LBTYPE;
import pt.ulisboa.tecnico.cnv.balancer.SmarterBalancer;
import pt.ulisboa.tecnico.cnv.monitors.AutoscalingMonitor;
import pt.ulisboa.tecnico.cnv.monitors.LivenessMonitor;
import pt.ulisboa.tecnico.cnv.monitors.MetricsMonitor;
import pt.ulisboa.tecnico.cnv.monitors.ProgressMonitor;
import pt.ulisboa.tecnico.cnv.policies.LoadComplexity;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;


public class ReverseProxy {

    public static void main(final String[] args) throws Exception {

        //initialize the load balancer and auto scalar
        LoadBalancerFactory.getInstance(LBTYPE.SMARTER);
        AutoScalerFactory.getInstance(AutoScalerFactory.ASTYPE.CPU);


        final HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);

        server.createContext("/climb", new MyHandler());
        //server.createContext("/metrics", new MetricsHandler());

        // be aware! infinite pool of threads!
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();


        //Start monitors
        Thread t = new Thread(new AutoscalingMonitor(2000));
        t.start();

        Thread t1 = new Thread(new LivenessMonitor(4000));
        t1.start();

        Thread t2 = new Thread(new MetricsMonitor(5000));
        t2.start();

        Thread t3 = new Thread(new ProgressMonitor(4000));
        t3.start();

        System.out.println(server.getAddress().toString());
    }

    static class MyHandler implements HttpHandler {

        private static final int MAX_TRIES = 5;

        @Override
        public void handle(final HttpExchange t) {

            String urlParameters = t.getRequestURI().getQuery();
            HashMap<String, String> paramMap = new HashMap<>();
            for(String param : urlParameters.split("&")) {
                String[] tParam = param.split("=");
                paramMap.put(tParam[0], tParam[1]);
            }

            String uuid = UUID.randomUUID().toString();
            urlParameters += "&uuid=" + uuid;

            System.out.println(urlParameters);

            AWSMetricsAutoScaler autoScaler = (AWSMetricsAutoScaler) AutoScalerFactory.getInstance(AutoScalerFactory.ASTYPE.CPU);
            SmarterBalancer loadBalancer = (SmarterBalancer) LoadBalancerFactory.getInstance(LBTYPE.SMARTER);
            LoadComplexity compl = loadBalancer.computeComplexity(paramMap);

            boolean responded = false;
            while( !responded ) {

                Instance instance = loadBalancer.getNextInstance(compl);
                autoScaler.notifyRequestStart(instance, compl, uuid);
                ReverseProxyRequester requestObject = new ReverseProxyRequester(instance, urlParameters, autoScaler, compl, t);
                Thread request = new Thread(requestObject);
                request.start();

                while (true) {
                    boolean alive = request.isAlive();
                    if( !alive && requestObject.getException() == null ){
                        responded = true;
                        break;
                    }
                    else if ( !alive ){
                        break;
                    }
                    List<Instance> instances = autoScaler.getAvailableInstances();
                    boolean found = false;
                    for(Instance i : instances){
                        if( i.getInstanceId().equals(instance.getInstanceId()) ){
                            found = true;
                        }
                    }
                    if( !found ){
                        request.interrupt();
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(responded){
                    autoScaler.notifyRequestEnd(instance, uuid);
                }
                else {
                    System.out.println(instance.getInstanceId() + " unresponsive trying again");
                }
            }
        }
    }
}
