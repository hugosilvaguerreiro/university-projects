package pt.ulisboa.tecnico.cnv.policies;

import pt.ulisboa.tecnico.cnv.dataObject.Metric;

import java.util.*;

import static java.lang.Math.abs;

public class DistanceCalc {
    private static DistanceCalc instance = null;
    private static HashMap<String, ArrayList<Coord>> complexityMap = new HashMap<>();

    private DistanceCalc() {
    }

    private class Coord {
        public int[] coord;
        public long mCount;

        Coord(int x, int y, long bbCount ){
            this.coord = new int[]{x, y};
            this.mCount = bbCount;
        }
    }

    public static synchronized DistanceCalc getInstance() {
        if (instance == null) {
            instance = new DistanceCalc();
        }
        return instance;
    }

    public synchronized LoadComplexity getComplexity(Map<String, String> params) {
        String map = params.get("i");
        int xs = Integer.parseInt(params.get("xS"));
        int ys = Integer.parseInt(params.get("yS"));

        if(!complexityMap.containsKey(map))
            return new LoadComplexity((long) 0);

        ArrayList<Coord> compl = complexityMap.get(map);

        Coord closest = compl.get(0);

        for(Coord c : compl) {
            if (abs(c.coord[0] - xs) + abs(c.coord[1] - xs) < abs(closest.coord[0] - xs) + abs(closest.coord[1]  - ys)) {
                closest = c;
            }
        }

        return new LoadComplexity(closest.mCount);
    }

    public synchronized void updateCalculator(Metric m) {
        String map = m.parameters.get("input_image");
        if(!complexityMap.containsKey(map)) {
            complexityMap.put(map, new ArrayList<Coord>());
        }

        int xs = Integer.parseInt(m.parameters.get("start_x"));
        int ys = Integer.parseInt(m.parameters.get("start_y"));
        long bb = m.mCount;

        complexityMap.get(map).add(new Coord(xs, ys, bb));

        //System.out.println("update: " + m.toString());
    }

}
