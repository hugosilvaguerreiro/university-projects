package pt.ulisboa.tecnico.cnv.policies;

import pt.ulisboa.tecnico.cnv.dataObject.Metric;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.Map;

public class AreaCalc {
    private static AreaCalc instance = null;
    private SimpleRegression regression = new SimpleRegression();

    private AreaCalc() {
    }

    public static synchronized AreaCalc getInstance() {
        if (instance == null) {
            instance = new AreaCalc();
        }
        return instance;
    }

    public synchronized LoadComplexity getComplexity(Map<String, String> params) {
        int x0 = Integer.parseInt(params.get("x0"));
        int y0 = Integer.parseInt(params.get("y0"));
        int x1 = Integer.parseInt(params.get("x1"));
        int y1 = Integer.parseInt(params.get("y1"));

        long area = (x1 - x0) * (y1 - y0);

        double complexity = regression.predict((double) area);

        if(Double.isNaN(complexity))
            complexity = 0;

        System.out.println(complexity);

        return new LoadComplexity((long) complexity);
    }

    public synchronized void updateCalculator(Metric m) {
        int x0 = Integer.parseInt(m.parameters.get("x0"));
        int y0 = Integer.parseInt(m.parameters.get("y0"));
        int x1 = Integer.parseInt(m.parameters.get("x1"));
        int y1 = Integer.parseInt(m.parameters.get("y1"));
        long meth = m.mCount;

        long area = (x1 - x0) * (y1 - y0);

        regression.addData((double) area, (double)meth);

        //System.out.println("update: " + m.toString());
    }
}
