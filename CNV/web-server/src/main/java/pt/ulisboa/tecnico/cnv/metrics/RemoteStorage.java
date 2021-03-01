package pt.ulisboa.tecnico.cnv.metrics;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoteStorage {

    public static final String TABLE_NAME = "HillClimberMetricsStorage";
    public static final String ID_NAME = "time";
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

    // Singleton
    private static RemoteStorage instance;

    private RemoteStorage() {
        try {
            this.init();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public static synchronized RemoteStorage getInstance() {
        if(instance == null)
            instance = new RemoteStorage();
        return instance;
    }

    // Instance fields
    private AmazonDynamoDB dynamoDB;

    private void init() {
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        EnvironmentVariableCredentialsProvider  credentialsProvider = new EnvironmentVariableCredentialsProvider();
        AWSCredentials credentials = null;
        String region = System.getenv("AWS_REGION");
        if(region == null){
            throw new AmazonClientException("Cannot load the region from the enviromental variables."+
                    "Please make sure to set AWS_REGION to a valid aws region id");
        }

        try {
            credentials = credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();

        for(String image: IMAGES) {
            for(String strat: STRATS) {
                createTable(getTableName(image, strat));
            }
        }
    }

    public static String getTableName(String image, String strat) {
        return image.replace("/", "-") + "_" + strat;
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

    public PutItemResult addMetric(Metric metric) {
        try {
            PutItemRequest putItemRequest = new PutItemRequest(getTableName(
                    metric.parameters.get("input_image"),metric.parameters.get("solver_strategy")), newItem(metric));
            System.out.println(putItemRequest);
            PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Result: " + putItemResult);
            return putItemResult;

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
        }
        return null;
    }

    private static Map<String, AttributeValue> newItem(Metric metric) {
        Map<String, AttributeValue> item = new HashMap<>();
        //Item item = new Item().withPrimaryKey(System.currentTimeMillis() / 1000L);
        item.put(ID_NAME, new AttributeValue().withN(Long.toString(System.currentTimeMillis() / 1000L)));
        item.put("mCount", new AttributeValue().withN(Long.toString(metric.mCount)));
        //item.put("bbCount", new AttributeValue().withN(Long.toString(metric.bbCount)));
        //item.put("iCount", new AttributeValue().withN(Long.toString(metric.iCount)));
        for(Map.Entry<String,String> e : metric.parameters.entrySet()){
            if(!e.getKey().equals("input_image") && !e.getKey().equals("solver_strategy"))
                item.put(e.getKey(), new AttributeValue().withN(e.getValue()));
        }
        return item;
    }

    public List<Metric> getMetrics() {

        List<Metric> listResult = new ArrayList<>();
        Map<String, AttributeValue> lastKeyEvaluated = null;
        do {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(TABLE_NAME)
                    .withExclusiveStartKey(lastKeyEvaluated);

            ScanResult result = dynamoDB.scan(scanRequest);
            for (Map<String, AttributeValue> item : result.getItems()){
                listResult.add(Metric.parseMap(item));
            }
            lastKeyEvaluated = result.getLastEvaluatedKey();
        } while (lastKeyEvaluated != null);
        return listResult;
    }
}
