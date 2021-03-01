package pt.ulisboa.tecnico.cnv.metrics;

import java.util.concurrent.ConcurrentHashMap;

public class LocalStorage {
	
	private static ConcurrentHashMap<Long,Metric> memory = new ConcurrentHashMap<Long,Metric>();


	public static ConcurrentHashMap<Long,Metric> getMemory(){
		return memory;
	}
}