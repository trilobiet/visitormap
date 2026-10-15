package com.trilobiet.visitorstats.ingester;

import redis.clients.jedis.Pipeline;

public interface RowIngestionStrategy {
	
	void ingestRow(String[] row, Pipeline p);
}
