package com.trilobiet.visitorstats.web;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.params.ZRangeParams;
import redis.clients.jedis.resps.GeoRadiusResponse;
import redis.clients.jedis.resps.Tuple;

public class RedisDaoImpl implements RedisDao {
	
    final RedisClient redisClient;
    
	public RedisDaoImpl(RedisClient redisClient) {
		
		this.redisClient = redisClient;
	}
	
	@Override
	public List<Location> getForArea(double lat, double lon, double widthKm, double heightKm, String path, String date) {
		
		// STEP 1. GEOSEARCH sww:geo FROMLONLAT 4 51 BYBOX 400 400 KM WITHCOORD
		GeoSearchParam geoParams = new GeoSearchParam()
			.fromLonLat(lon,lat)
			.byBox(widthKm, heightKm, GeoUnit.KM)
			.withCoord();
		
		List<GeoRadiusResponse> lstGeo;
		
		System.out.println("WIDTH KM: " + widthKm);
		
		if (widthKm > 30000) lstGeo = redisClient.geosearch("sww:geocountries", geoParams);
		else lstGeo = redisClient.geosearch("sww:geo", geoParams);
		
		lstGeo.forEach(geo -> { 
			if (!geo.getMemberByString().contains(":")) System.out.println(geo.getMemberByString() + " : " + geo.getCoordinate());
		});
		
		// STEP 2. ZRANGE sww:visits:/news:2026-09-28 +inf 1 BYSCORE REV WITHSCORES [LIMIT 0 20]
		// second number in (+inf 1) denotes minimum score when we run BYSCORE 
		ZRangeParams zParams = ZRangeParams
			.zrangeByScoreParams(Double.POSITIVE_INFINITY, 1)
			.rev();
		
		String metric = String.join(":", path, date); 
		
		//List<Tuple> lstGeoScores = redisClient.zrangeWithScores("sww:visits:all:totals", zParams);
		List<Tuple> lstGeoScores = redisClient.zrangeWithScores(metric, zParams);
		
		//lstGeoScores.forEach(System.out::println);
		
		System.out.println("lstGeo: " + lstGeo.size() + " lstGeoScores: " + lstGeoScores.size());
		
		
		// STEP 3. Load the shorter list into a hashMap 
		// (lstGeo, on average, will be the shorter one unless we have very few site visitors...)
		Map<String, GeoRadiusResponse> mapGeo = lstGeo.stream()
			.collect(Collectors.toMap(GeoRadiusResponse::getMemberByString,Function.identity()));
		
		// STEP 4. merge the lists (join)
		List<Location> lstLocations = lstGeoScores.stream()
		     // only locations that match geo scores that are in our area (mapGeo)
			.filter(gs -> mapGeo.containsKey(gs.getElement()))
			.map(gs -> {
				GeoCoordinate geo = mapGeo.get(gs.getElement()).getCoordinate();
				return new Location(
					gs.getElement(),
					geo.getLatitude(),
					geo.getLongitude(),
					gs.getScore()
				);
			}).collect(Collectors.toList());
		
		System.out.println("Locations: " + lstLocations.size());
		
		lstLocations.forEach(loc -> { 
			if(!loc.name().contains(":")) System.out.println(loc);
		});
		
		
		return lstLocations;
	}	
	

	@Override
	public Set<String> getKeys() {
		
		Set<String> allKeys = redisClient.keys("*:visits:*");

		return allKeys.stream()
			.sorted(Comparator.reverseOrder())	
			.map(key -> key.substring(0, key.lastIndexOf(":"))) // remove date part
			//.collect(Collectors.toSet());
			.collect(Collectors.toCollection(TreeSet::new)); // explicitely create a TreeSet to maintain order  
	}

	
	@Override
	public Set<String> getDates() {

		Set<String> allKeys = redisClient.keys("*:visits:all:*");
		return allKeys.stream()
			.sorted(Comparator.reverseOrder())	
			.map(key -> key.substring(key.lastIndexOf(":") + 1)) // select only date part
			.collect(Collectors.toCollection(TreeSet::new));
	}

}
