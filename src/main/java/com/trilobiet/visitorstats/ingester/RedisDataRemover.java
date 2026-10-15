package com.trilobiet.visitorstats.ingester;

import java.util.List;

import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class RedisDataRemover {
	
	private RedisDataRemover() {}

	/**
	 * Remove all keys that match pattern
	 * 
	 * @param redisClient
	 * @param pattern
	 */
	public static void deleteKeysByPattern(RedisClient redisClient, String pattern) {
		
		String cursor = ScanParams.SCAN_POINTER_START;
		ScanParams params = new ScanParams().match(pattern).count(1000);

		do {
			ScanResult<String> scanResult = redisClient.scan(cursor, params);
			List<String> keys = scanResult.getResult();
			cursor = scanResult.getCursor();

			if (keys != null && !keys.isEmpty()) {
				redisClient.unlink(keys.toArray(new String[0]));
			}

		} while (!cursor.equals(ScanParams.SCAN_POINTER_START));
	}

}
