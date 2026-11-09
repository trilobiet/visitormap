package com.trilobiet.visitorstats.ingester.ip2loc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilobiet.visitorstats.ingester.RowIngestionStrategy;

import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Pipeline;

public class IPRowIngestionCountryStrategy implements RowIngestionStrategy {
	
	private final String prefix;
	private static final String NAME = "ip2location_countries";
	
	public IPRowIngestionCountryStrategy(String prefix) {

		this.prefix = prefix;
	}

	@Override
	public String getName() {
		return NAME;
	}

	private final List<String> columns = Arrays.asList(
		"lower","upper","country_code","country","region","city","lat","lon");
	
	private int idx(String s) {
		return columns.indexOf(s); 
	}
	
	
	@Override
	public void ingestRow(String[] row, Pipeline p) {
		
		String country = row[idx("country")];
		String region = row[idx("region")];
		String city = row[idx("city")];
		
		/*
		 *  Collect only entries like "France:-:-"
		 *  These contain country coordinates. However they are used as a fallback
		 *  in IP2Location datasets and for some countries they are not available
		 *  because there was no need for a fallback. So these countries must be
		 *  read from another source.
		 */
		
		if (region.equals("-") && city.equals("-")) {
			
	        Map<String,GeoCoordinate> geoMember = new HashMap<>();
	        GeoCoordinate gc = new GeoCoordinate( // longitude,latitude
	        	Double.valueOf(row[idx("lon")]),
	        	Double.valueOf(row[idx("lat")])
	        ); 
	        geoMember.put(country, gc);
	        p.geoadd(prefix + ":geocountries",geoMember);
		}
	}

}

