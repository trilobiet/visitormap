package com.trilobiet.visitorstats.ingester.ip2loc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilobiet.visitorstats.ingester.RowIngestionStrategy;

import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Pipeline;

public class IPRowIngestionStrategy implements RowIngestionStrategy {
	
	private final String prefix;
	
	public IPRowIngestionStrategy(String prefix) {

		this.prefix = prefix;
	}


	private final List<String> columns = Arrays.asList(
		"lower","upper","country_code","country","region","city","lat","lon");
	
	private int idx(String s) {
		return columns.indexOf(s); 
	}
	
	
	@Override
	public void ingestRow(String[] row, Pipeline p) {
		
        StringBuilder landRegionCity = new StringBuilder();
        landRegionCity.append(row[idx("country")])
        	.append(":").append(row[idx("region")])
        	.append(":").append(row[idx("city")]);
		
		// ZADD mijntest:ranges 755292160 argentina:santa_cruz:los_antigos=767011840-767012863
		Map<String,Double> rangeMember = new HashMap<>();
		rangeMember.put(landRegionCity.toString(), Double.valueOf(row[idx("lower")]));
        p.zadd(prefix + ":ipranges", rangeMember);
        
        Map<String,GeoCoordinate> geoMember = new HashMap<>();
        GeoCoordinate gc = new GeoCoordinate( // longitude,latitude
        	Double.valueOf(row[idx("lon")]),
        	Double.valueOf(row[idx("lat")])
        ); 
        geoMember.put(landRegionCity.toString(), gc);
        p.geoadd(prefix + ":geo",geoMember);
	}
}
