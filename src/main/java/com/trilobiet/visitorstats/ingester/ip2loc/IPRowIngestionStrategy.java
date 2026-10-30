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
	
	private static final String NAME = "ip2location";
	
	public IPRowIngestionStrategy(String prefix) {

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
		
        StringBuilder landRegionCity = new StringBuilder();
        landRegionCity.append(row[idx("country")])
        	.append(":").append(row[idx("region")])
        	.append(":").append(row[idx("city")]);
		
		// ZADD mijntest:ranges 755292160 argentina:santa_cruz:los_antigos
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
        
        /* We also add the country location. Since this is a set only the last read entry will
         * be retained. Because this entry corresponds to a city *in* that country it will not necessarily
         * have the countries center coordinates. In a later pass we will overwrite with country specific
         * coordinates, but these are not available in this dataset for all countries, so with the next
         * lines we ensure we have *some* coordinate for the country.
         */
        
        geoMember.clear();
        geoMember.put(country, gc);
        p.geoadd(prefix + ":geocountries",geoMember);
	}
}
