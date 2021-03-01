import BIT.highBIT.*;
import java.io.*;
import java.util.*;

import pt.ulisboa.tecnico.cnv.metrics.LocalStorage;
import pt.ulisboa.tecnico.cnv.metrics.Metric;;


public class HillBalancerInstrument {
    public static void main(String argv[]) {
        File file_in = new File(argv[0]);
        String filename = file_in.getPath();
        // create class info object
        ClassInfo ci = new ClassInfo(filename);
        
        // loop through all the routines
        // see java.util.Enumeration for more information on Enumeration class
        for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
            Routine routine = (Routine) e.nextElement();
            routine.addBefore("HillBalancerInstrument", "mcount", new Integer(1));
            
            /*for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                BasicBlock bb = (BasicBlock) b.nextElement();
                bb.addBefore("HillBalancerInstrument", "count", new Integer(bb.size()));
            }*/

        }
        ci.write(filename);
    }
/*
    public static void count(int incr) {
        Metric m = LocalStorage.getMemory().get(Thread.currentThread().getId());
        m.iCount += incr;
        m.bbCount++;
    }
*/
    public static void mcount(int incr) {
        Metric m = LocalStorage.getMemory().get(Thread.currentThread().getId());
        m.mCount++;
    }
}

