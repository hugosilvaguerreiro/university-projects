package pt.ulisboa.tecnico.cnv.metrics;

import java.util.List;
import java.util.ArrayList;

public class MetricGatherer {
    private static MetricGatherer instance;
    private List<Metric> metrics;

    private MetricGatherer() {
        this.metrics = new ArrayList<Metric>();
    }

    public static synchronized MetricGatherer getInstance() {
        if(instance == null)
            instance = new MetricGatherer();
        return instance;
    }

    public synchronized void addMetric(Metric m) {
        this.metrics.add(m);
    }


    public String listMetrics() {
        String res = "";
        for(Metric m: metrics) {
            res += m.toString();
            res += "\n";
        }
        return res;
    }

    public List<Metric> getMetricList() {
        return metrics;
    }
}