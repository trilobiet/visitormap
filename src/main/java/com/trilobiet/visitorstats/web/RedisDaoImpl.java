package com.trilobiet.visitorstats.web;

import java.util.Collections;
import java.util.List;

import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.params.ZRangeParams;
import redis.clients.jedis.resps.GeoRadiusResponse;
import redis.clients.jedis.resps.Tuple;

public class RedisDaoImpl implements RedisDao {
	
    final ConnectionPoolConfig poolConfig;
    
	public RedisDaoImpl() {
		
		poolConfig = new ConnectionPoolConfig();
	    poolConfig.setMaxTotal(64);
	    poolConfig.setMaxIdle(64);
	}
	
	@Override
	public List<Location> getForArea(double lat, double lon, double widthKm, double heightKm) {
		
		try ( RedisClient redisClient = RedisClient.builder().poolConfig(poolConfig).hostAndPort("localhost", 6379).build() ) {
		
			// STEP 1. GEOSEARCH sww:geo FROMLONLAT 4 51 BYBOX 400 400 KM WITHCOORD
			GeoSearchParam geoParams = new GeoSearchParam()
				.fromLonLat(lon,lat)
				.byBox(widthKm, heightKm, GeoUnit.KM)
				.withCoord();
			
			List<GeoRadiusResponse> lstGeo = redisClient.geosearch("sww:geo", geoParams);
			
			lstGeo.forEach(geo -> System.out.println(geo.getMemberByString() + " : " + geo.getCoordinate()));
			
			// STEP 2. ZRANGE sww:visits:/news:2026-09-28 +inf 1 BYSCORE REV WITHSCORES [LIMIT 0 20]
			// second number in (+inf 1) denotes minimum score when we run BYSCORE 
			ZRangeParams zParams = ZRangeParams
				.zrangeByScoreParams(Double.POSITIVE_INFINITY, 1)
				.rev();
			
			List<Tuple> lstGeoScores = redisClient.zrangeWithScores("sww:visits:/news:2026-09-28", zParams);
			
			lstGeoScores.forEach(System.out::println);
			
		}	
		return Collections.emptyList();
	}

}
