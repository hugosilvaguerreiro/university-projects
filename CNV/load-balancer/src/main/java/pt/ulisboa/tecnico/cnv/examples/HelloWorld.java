package pt.ulisboa.tecnico.cnv.examples;

import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.ec2.model.Instance;
import pt.ulisboa.tecnico.cnv.cloudManager.CloudManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HelloWorld {

        public static void main(String[] args) {
            // Prints "Hello, World" to the terminal window.
            CloudManager c = CloudManager.getInstance();

            //String instance_id = c.launchNewInstance();

            //ArrayList<Instance> instances = c.getAvailableInstances();


            /*for(Instance i : instances) {
                List<Datapoint> ut = c.getCloudWatchCPUUtilization(i);
                System.out.println(i.getPublicIpAddress());
                for(Datapoint d : ut) {
                    System.out.println(i.getInstanceId());
                    System.out.println(d.getAverage());
                }
//                break;

            }*/

            //c.destroyInstance(instance_id);

        }

}

