package com.trilobiet.visitorstats.ingester;

import java.util.List;
import java.util.ServiceLoader;

import redis.clients.jedis.Pipeline;

public interface RowIngestionStrategy {
	
	void ingestRow(String[] row, Pipeline p);
	String getName(); 

	/**
	 * Static factory method looks up strategy by name (if found on class path)
	 * 
	 * @param name
	 * @return corresponding RowIngestionStrategy
	 */
	public static RowIngestionStrategy getFor(String name) {
		
		/* Scan for implementations of RowIngestionStrategy.
		 * These can be dropped in /lib folder as a jar. 
		 */
		ServiceLoader<RowIngestionStrategy> strategies =
			ServiceLoader.load(RowIngestionStrategy.class);
		
		for (RowIngestionStrategy strategy: strategies) {
			if (name.equals(strategy.getName())) return strategy;	
		}
		
		throw new IllegalArgumentException(String.format("Strategy %s not found", name));
	}
	
	/**
	 * List all strategies
	 * @return a list of available strategy names
	 */
	public static List<String> getStrategyNames() {
		
		ServiceLoader<RowIngestionStrategy> strategies =
			ServiceLoader.load(RowIngestionStrategy.class);
		
		return strategies.stream()
			.map(ServiceLoader.Provider::get)
			.map(RowIngestionStrategy::getName)
			.toList();
	}
	
}
