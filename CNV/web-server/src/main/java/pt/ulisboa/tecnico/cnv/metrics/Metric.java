package pt.ulisboa.tecnico.cnv.metrics;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

public class Metric {

    public static final String[] LIST_PARAMETERS = {"x0","y0", "x1", "y1", "start_x", "start_y", "input_image", "solver_strategy"};

    public HashMap<String, String> parameters;
    public long mCount;
    public String uuid;


    public Metric() {
        this.parameters = new HashMap<>();
        this.mCount = 0;

    }

    public Metric(SolverArgumentParser params, String uuid) {
        this.parameters = new HashMap<>();
        this.parameters.put("x0", Integer.toString(params.getX0()));
        this.parameters.put("y0", Integer.toString(params.getY0()));
        this.parameters.put("x1", Integer.toString(params.getX1()));
        this.parameters.put("y1", Integer.toString(params.getY1()));
        this.parameters.put("start_x", Integer.toString(params.getStartX()));
        this.parameters.put("start_y", Integer.toString(params.getStartY()));
        this.parameters.put("input_image", params.getInputImage());
        this.parameters.put("solver_strategy", params.getSolverStrategy().toString());

        this.mCount = 0;
        this.uuid = uuid;

    }

    public Metric(HashMap<String, String> params, long mCount, long bbCount, long iCount) {
        this.parameters = params;
        this.mCount = mCount;
        this.uuid = uuid;

    }

    public static Metric parseMap(Map<String, AttributeValue> item) {
        long mCount = Long.parseLong(item.get("mCount").getN());
        long bbCount = Long.parseLong(item.get("bbCount").getN());
        long iCount = Long.parseLong(item.get("iCount").getN());

        HashMap<String, String> params = new HashMap<>();
        for(int i = 0; i < LIST_PARAMETERS.length; i++) {
            params.put(LIST_PARAMETERS[i], item.get(LIST_PARAMETERS[i]).getS());
        }
        return new Metric(params, mCount, bbCount, iCount);
    }

    @Override
    public String toString() {
        String str = "";
        str += "Metric with params: " + this.parameters + "\n";
        str += "    *Method Calls: " + this.mCount + "\n";

        return str;
    }


}