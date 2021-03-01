package pt.ulisboa.tecnico.cnv.dataObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class Metric {

    public static final String[] LIST_PARAMETERS = {"x0","y0", "x1", "y1", "start_x", "start_y"};

    public long time;
    public HashMap<String, String> parameters;
    public long mCount;
    public String uuid;

    public Metric() {
        this.parameters = new HashMap<String, String>();
        this.time = 0;
        this.mCount = 0;
        this.uuid = UUID.randomUUID().toString();
    }

    public Metric(long time, HashMap<String, String> params, long mCount, String uuid) {
        this.time = time;
        this.parameters = params;
        this.mCount = mCount;
        this.uuid = uuid;
    }

    public static Metric parseMap(Map<String, AttributeValue> item) {
        long time = Long.parseLong(item.get("time").getN());
        long mCount = Long.parseLong(item.get("mCount").getN());

        HashMap<String, String> params = new HashMap<String, String>();
        for(int i = 0; i < LIST_PARAMETERS.length; i++) {
            params.put(LIST_PARAMETERS[i], item.get(LIST_PARAMETERS[i]).getN());
        }
        return new Metric(time, params, mCount, "");
    }

    @Override
    public String toString() {
        String str = "";
        str += "Metric(time " + this.time + ") with params: " + this.parameters + "\n";
        str += "    *Method Calls: " + this.mCount + "\n";
        return str;
    }

}