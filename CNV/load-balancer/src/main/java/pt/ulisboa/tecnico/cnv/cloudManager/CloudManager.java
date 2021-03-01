package pt.ulisboa.tecnico.cnv.cloudManager;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import pt.ulisboa.tecnico.cnv.dataObject.Metric;

import java.util.*;

public class CloudManager {

    public static final String IMAGES[] = new String[] {
            "datasets/RANDOM_HILL_1024x1024_2019-03-08_16-57-28.dat",
            "datasets/RANDOM_HILL_512x512_2019-03-01_10-28-46.dat",
            "datasets/RANDOM_HILL_1024x1024_2019-03-08_17-00-23.dat",
            "datasets/RANDOM_HILL_512x512_2019-03-01_10-28-31.dat",
            "datasets/RANDOM_HILL_512x512_2019-02-27_09-56-18.dat",
            "datasets/RANDOM_HILL_512x512_2019-03-01_10-28-59.dat",
            "datasets/RANDOM_HILL_512x512_2019-03-01_10-29-31.dat",
            "datasets/RANDOM_HILL_512x512_2019-02-27_09-46-42.dat",
            "datasets/RANDOM_HILL_1024x1024_2019-03-08_17-04-10.dat",
            "datasets/RANDOM_HILL_1024x1024_2019-03-08_16-57-37.dat",
            "datasets/RANDOM_HILL_512x512_2019-03-01_10-28-39.dat",
            "datasets/RANDOM_HILL_1024x1024_2019-03-08_16-59-31.dat",
            "datasets/RANDOM_HILL_1024x1024_2019-03-08_16-58-44.dat"
    };
    public static final String STRATS[] = new String[] {"BFS", "DFS", "ASTAR"};
    private static HashMap<String, Long> lastAttrValue = new HashMap<>();

    private static int currentImage = 0;
    private static int currentStrat = 0;


    private final String ZONE = "eu-central-1";

    //public static final String TABLE_NAME = "HillClimberMetricsStorage";
    public static final String ID_NAME = "time";

    private static final String IMAGE_ID = "ami-0a7cf8f85285e01fc";
    private static final String INSTANCE_TYPE = "t2.micro";
    private static final String KEY_NAME = "drn-mjro";
    private static final String SECURITY_GROUP_NAME = "cnv";

    private static CloudManager instance;
    private ArrayList<Instance> availableInstances;
    private AmazonCloudWatch cloudWatch;
    private AmazonEC2 ec2;
    private AmazonDynamoDB dynamoDB;

    private CloudManager() {

        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            System.out.println("Cannot load the credentials from the credential profiles file.");
            System.out.println("Trying to load from environment variables.");
            try {
                credentials = new EnvironmentVariableCredentialsProvider().getCredentials();
            } catch (Exception e2) {
                throw new AmazonClientException(
                        "Cannot load the credentials from the credential profiles file or from the environment variables. " +
                                "Please make sure that your credentials file is at the correct " +
                                "location (~/.aws/credentials), and is in valid format." +
                                "Optionally make sure that the environment variables AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY are set",
                        e2);
            }

        }
        this.ec2 = AmazonEC2ClientBuilder.standard().withRegion(ZONE).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        this.cloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion(ZONE).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();

        dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(ZONE)
                .build();

        for(String image: IMAGES) {
            for(String strat: STRATS) {
                createTable(getTableName(image, strat));
            }
        }

        this.dynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(ZONE).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    public AmazonDynamoDB getDatabase() {
        return dynamoDB;
    }

    public void createTable(String name) {
        try {
            // Create a table with a primary hash key named 'name', which holds a string
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(name)
                    .withKeySchema(new KeySchemaElement().withAttributeName(ID_NAME).withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName(ID_NAME).withAttributeType(ScalarAttributeType.N))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

            // Create table if it does not exist yet
            TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
            // wait for the table to move into ACTIVE state
            TableUtils.waitUntilActive(dynamoDB, name);

            // Describe our new table
            DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(name);
            TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
            System.out.println("Table Description: " + tableDescription);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (InterruptedException e){
            System.out.println("Caught an InterruptedExpeception.");
            System.out.println("Error Message: " + e.getMessage());
        }
    }

    public synchronized static CloudManager getInstance() {
        if(instance == null) {
            instance = new CloudManager();
        }
        return instance;
    }

    public boolean isAvailable(Instance instance) {

        return instance.getState().getName().equals("running");
    }

    public List<Instance> launchNewInstance(int nrOfInstances) {
        RunInstancesRequest runInstancesRequest =
                new RunInstancesRequest();

        runInstancesRequest.withImageId(IMAGE_ID)
                .withInstanceType(INSTANCE_TYPE)
                .withMinCount(nrOfInstances)
                .withMaxCount(nrOfInstances)
                .withKeyName(KEY_NAME)
                .withSecurityGroups(SECURITY_GROUP_NAME);
        RunInstancesResult runInstancesResult =
                ec2.runInstances(runInstancesRequest);
        String newInstanceId = runInstancesResult.getReservation().getInstances()
                .get(0).getInstanceId();

        System.out.println("Created instance "+ newInstanceId);
        return runInstancesResult.getReservation().getInstances();
    }


    public void destroyInstance(String instanceId) {
        TerminateInstancesRequest termInstanceReq = new TerminateInstancesRequest();
        termInstanceReq.withInstanceIds(instanceId);
        ec2.terminateInstances(termInstanceReq);
        System.out.println("Destroyed Instance "+instanceId);
    }
    public void notifyDeadInstance(Instance instance) {
        synchronized (availableInstances) {
            availableInstances = null;
        }
    }

    public synchronized ArrayList<Instance> getAvailableInstances() {
            DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
            List<Reservation> reservations = describeInstancesRequest.getReservations();
            ArrayList<Instance> instances = new ArrayList<Instance>();

            for (Reservation reservation : reservations) {
                for(Instance i : reservation.getInstances()) {
                    if(i.getState().getName().equals("running") && i.getImageId().equals(IMAGE_ID)) {
                        instances.add(i);
                    }
                }

            }
        return instances;
    }

    public List<Datapoint> getCloudWatchCPUUtilization(Instance instance) {
        Dimension instanceDimension = new Dimension();
        instanceDimension.setName("InstanceId");
        List<Dimension> dims = new ArrayList<Dimension>();
        dims.add(instanceDimension);

        String name = instance.getInstanceId();
        String state = instance.getState().getName();
        List<Datapoint> datapoints = null;
        if(state.equals("running")) {
            instanceDimension.setValue(name);
            GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
                    .withStartTime(new Date(new Date().getTime() - 1000 * 60 * 10))
                    .withNamespace("AWS/EC2")
                    .withPeriod(30)
                    .withMetricName("CPUUtilization")
                    .withStatistics("Average")
                    .withDimensions(instanceDimension)
                    .withEndTime(new Date());
            GetMetricStatisticsResult getMetricStatisticsResult =
                    cloudWatch.getMetricStatistics(request);
            datapoints = getMetricStatisticsResult.getDatapoints();
        }else {
            datapoints = new ArrayList<Datapoint>();
        }

        return datapoints;
    }

    public List<Metric> getMetrics() {
        final String TABLE_NAME = "HillClimberMetricsStorage";
        Map<String, String> expAttrName = new HashMap<String, String>();
        expAttrName.put("#time", "time");
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        String table_name = getTableName(IMAGES[currentImage], STRATS[currentStrat]);
        long last = lastAttrValue.getOrDefault(table_name, 0L);

        eav.put(":last_time", new AttributeValue().withN(Long.toString(last)));

        List<Metric> listResult = new ArrayList<Metric>();
        Map<String, AttributeValue> lastKeyEvaluated = null;
        do {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(table_name)
                    .withFilterExpression("#time > :last_time")
                    .withExpressionAttributeNames(expAttrName)
                    .withExpressionAttributeValues(eav)
                    .withExclusiveStartKey(lastKeyEvaluated);

            ScanResult result = dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> item : result.getItems()){
                Metric m = Metric.parseMap(item);
                listResult.add(m);
                if(lastAttrValue.containsKey(table_name)) {
                    long v = lastAttrValue.get(table_name);
                    lastAttrValue.put(table_name, m.time > v ? m.time : v);
                } else {
                    lastAttrValue.put(table_name, m.time);
                }

            }
            lastKeyEvaluated = result.getLastEvaluatedKey();
        } while (lastKeyEvaluated != null);

        currentImage = (currentImage + 1) % IMAGES.length;
        if(currentImage == 0)
            currentStrat = (currentStrat +1 ) % STRATS.length;


        return listResult;
    }


    public static String getTableName(String image, String strat) {
        return image.replace("/", "-") + "_" + strat;
    }

}
