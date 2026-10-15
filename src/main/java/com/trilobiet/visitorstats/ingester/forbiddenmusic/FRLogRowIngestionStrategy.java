package com.trilobiet.visitorstats.ingester.forbiddenmusic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trilobiet.visitorstats.ingester.DateFormatter;
import com.trilobiet.visitorstats.ingester.Utils;
import com.trilobiet.visitorstats.ingester.RowIngestionStrategy;

import redis.clients.jedis.Pipeline;

public class FRLogRowIngestionStrategy implements RowIngestionStrategy {

	private final String scriptID;
	private final DateFormatter df = new DateFormatter("dd/MMM/yyyy:HH:mm:ss");
	private final List<String> columns = Arrays.asList(
		"ip","datetime","request","status","referer","user_agent");
	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	public FRLogRowIngestionStrategy(String scriptID) {

		this.scriptID = scriptID;
	}
	
	
	private int idx(String s) {
		return columns.indexOf(s); 
	}
	

	@Override
	public void ingestRow(String[] row, Pipeline p) {
		
		columns.indexOf("request");

		try {
	    	String ip = row[idx("ip")];
	    	Pair<String, String> dateTime = df.format(row[idx("datetime")].substring(1)); // remove square brackets
	    	String date = dateTime.getLeft();
	    	String time = dateTime.getRight();
	    	String request = Utils.pathFromRequest(row[idx("request")]);
	
	    	if ( doIngest(row) )  
	    		p.evalsha(scriptID, 0, ip, String.valueOf(Utils.ipAton(ip)), date, time, request);
		}
		catch (Exception e) {

			logger.warn("Could not ingest {}", Arrays.toString(row));
		}
		
	}
	
	
	private boolean doIngest(String[] row) {
		
		/* Request string is on column 5
		 * Status code string is on column 6  
		 * User Agent is on column 9
		 */
		return checkRequestString(row[idx("request")]) 
			&& checkHttpStatus(row[idx("status")])
			&& checkUserAgent(row[idx("user_agent")]);	
	}
	
	/*
	 * Filter out some user agents
	 */
	private boolean checkUserAgent(String ua) {
		
	    List<String> ignoreList = new ArrayList<>(); //Arrays.asList("uptimerobot","amazonbot");
	    ignoreList.add("uptimerobot");
	    ignoreList.add("amazonbot");
	    ignoreList.add("youbot");
	    ignoreList.add("reflection.ai");
		
	    ignoreList.removeIf(i -> !ua.toLowerCase().contains(i.toLowerCase()));
		
		return ignoreList.isEmpty();
	}	
	
	
	/*
	 * Only allow these http status codes.
	 */
	private boolean checkHttpStatus(String status) {
		
		return status.equals("200");
	}
	
	/*
	 * Filter on certain url patterns. We are not interested in css files,
	 * images, js files, google pixels etc.
	 */
	private boolean checkRequestString(String request) {
		
		// Either request must exactly be one of this list:  
		List<String> wholeString = Arrays.asList(
			"/",
			"/search/composers",
			"/search/compositions",
			"/search/recordings"
		);		
		
		// .. or request must start with one of this list:
		List<String> startOfString = Arrays.asList(
			"/content/",
			"/search/composer/",
			"/search/composition/",
			"/search/recording/"
		);
		
		return (wholeString.stream().anyMatch(request::equals)
			||
			startOfString.stream().anyMatch(request::startsWith)
		);
	}	
}
