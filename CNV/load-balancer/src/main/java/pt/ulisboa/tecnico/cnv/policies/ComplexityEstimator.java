package pt.ulisboa.tecnico.cnv.policies;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import pt.ulisboa.tecnico.cnv.cloudManager.CloudManager;
import pt.ulisboa.tecnico.cnv.dataObject.Metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComplexityEstimator {
    public static final int APPROXIMATION_TOLERANCE = 150;
    public static final int XS_TOLERANCE = 100;

    public static long estimateComplexity(Map<String, String> params) {
        int x0 = Integer.parseInt(params.get("x0"));
        int y0 = Integer.parseInt(params.get("y0"));
        int x1 = Integer.parseInt(params.get("x1"));
        int y1 = Integer.parseInt(params.get("y1"));
        return 0;
    }
    public static String sumStrings(String i1, int i2) {
        System.out.println(Integer.toString(Integer.valueOf(i1) + i2));
        return Integer.toString(Integer.valueOf(i1) + i2);
    }

    public static LoadComplexity getComplexity(Map<String, String> params) {
        CloudManager manager = CloudManager.getInstance();
        AmazonDynamoDB db = manager.getDatabase();

        Map<String, String> expAttrName = new HashMap<String, String>();
        //expAttrName.put("#bbCount", "bbCount");
        //expAttrName.put("#input_image", "input_image");
        //expAttrName.put("#solver_strategy", "solver_strategy");
        expAttrName.put("#x0", "x0");
        expAttrName.put("#y0", "y0");
        expAttrName.put("#x1", "x1");
        expAttrName.put("#y1", "y1");
        expAttrName.put("#xS", "start_x");
        expAttrName.put("#yS", "start_y");

        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        //eav.put(":myImage", new AttributeValue().withS(params.get("i")));
        //eav.put(":myStrategy", new AttributeValue().withS(params.get("s")));

        eav.put(":myX0Low", new AttributeValue().withN(sumStrings(params.get("x0"), -1*APPROXIMATION_TOLERANCE)));
        eav.put(":myX0High", new AttributeValue().withN(sumStrings(params.get("x0"), APPROXIMATION_TOLERANCE)));

        eav.put(":myX1Low", new AttributeValue().withN(sumStrings(params.get("x1"), -1*APPROXIMATION_TOLERANCE)));
        eav.put(":myX1High", new AttributeValue().withN(sumStrings(params.get("x1"), APPROXIMATION_TOLERANCE)));

        eav.put(":myY0Low", new AttributeValue().withN(sumStrings(params.get("y0"), -1*APPROXIMATION_TOLERANCE)));
        eav.put(":myY0High", new AttributeValue().withN(sumStrings(params.get("y0"), APPROXIMATION_TOLERANCE)));

        eav.put(":myY1Low", new AttributeValue().withN(sumStrings(params.get("y1"), -1*APPROXIMATION_TOLERANCE)));
        eav.put(":myY1High", new AttributeValue().withN(sumStrings(params.get("y1"), APPROXIMATION_TOLERANCE)));

        eav.put(":myYSLow", new AttributeValue().withN(sumStrings(params.get("yS"), -1*XS_TOLERANCE)));
        eav.put(":myYSHigh", new AttributeValue().withN(sumStrings(params.get("yS"), XS_TOLERANCE)));

        eav.put(":myXSLow", new AttributeValue().withN(sumStrings(params.get("xS"), -1*XS_TOLERANCE)));
        eav.put(":myXSHigh", new AttributeValue().withN(sumStrings(params.get("xS"), XS_TOLERANCE)));

        //eav.put(":myStrategy", new AttributeValue().withN(Long.toString(97468380)));

        System.out.println(CloudManager.getTableName(params.get("i"), params.get("s")));

        List<Metric> listResult = new ArrayList<Metric>();
        Map<String, AttributeValue> lastKeyEvaluated = null;
        do {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(CloudManager.getTableName(params.get("i"), params.get("s")))
                    .withFilterExpression(
                            "#x0 between :myX0Low and :myX0High and"+
                            "#x1 between :myX1Low and :myX1High and"+
                            "#y0 between :myY0Low and :myY0High and"+
                            "#y1 between :myY1Low and :myY1High and"+
                            "#xS between :myXSLow and :myXSHigh and"+
                            "#yS between :myYSLow and :myYSHigh"
                    )
                    .withExpressionAttributeNames(expAttrName)
                    .withExpressionAttributeValues(eav)
                    .withExclusiveStartKey(lastKeyEvaluated);

            ScanResult result = db.scan(scanRequest);
            for (Map<String, AttributeValue> item : result.getItems()){
                listResult.add(Metric.parseMap(item));
            }
            lastKeyEvaluated = result.getLastEvaluatedKey();
        } while (lastKeyEvaluated != null);


        long total=0;
        int nrMatches=0;
        for(Metric res : listResult) {
            nrMatches++;
            total += res.mCount;
            System.out.println(res.toString());
        }
        if(nrMatches != 0) {
            return new LoadComplexity(total/nrMatches);
        } else return null;


    }

    public static void main(String[] args) {
        HashMap<String, String> params = new HashMap<>();
        params.put("x0", "0");
        params.put("y0", "0");
        params.put("x1", "512");
        params.put("y1", "512");
        params.put("xS", "100");
        params.put("yS", "200");
        params.put("s", "BFS");
        params.put("i", "datasets/RANDOM_HILL_512x512_2019-02-27_09-46-42.dat");

        LoadComplexity complexity = getComplexity(params);

    }

}
