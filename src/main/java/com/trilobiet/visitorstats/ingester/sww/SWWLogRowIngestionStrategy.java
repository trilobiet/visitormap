package com.trilobiet.visitorstats.ingester.sww;

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

public class SWWLogRowIngestionStrategy implements RowIngestionStrategy {
	
	// Maybe wrap some reusable behavior (checkUserAgent, checkHttpStatus, checkRequestString)
	// in (configurable through extra params) decorators

	private final String scriptID;
	private final DateFormatter df = new DateFormatter("dd/MMM/yyyy:HH:mm:ss");
	private final List<String> columns = Arrays.asList(
		"ip","ignore1","ignore2","datetime","ignore3","request","status","ignore4","referer","user_agent");
	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	public SWWLogRowIngestionStrategy(String scriptID) {

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
	    	String request = Utils.pathFromRequest(row[idx("request")]).toLowerCase();
	    	String status = row[idx("status")];
	    	String userAgent = row[idx("user_agent")];
	    	
	    	//System.out.println(String.valueOf(Utils.ipAton(ip)) + " " + date + " " + request);
	
	    	if ( doIngest(request,status,userAgent) )  
	    		p.evalsha(scriptID, 0, String.valueOf(Utils.ipAton(ip)), date, request);
		}
		catch (Exception e) {

			logger.warn("Could not ingest {}", Arrays.toString(row));
		}
		
	}
	
	
	private boolean doIngest(String request, String status, String userAgent) {
		
		/* Request string is on column 5
		 * Status code string is on column 6  
		 * User Agent is on column 9
		 */
		return checkRequestString(request) 
			&& checkHttpStatus(status)
			&& checkUserAgent(userAgent);	
	}
	
	/*
	 * Filter out some user agents
	 */
	private boolean checkUserAgent(String ua) {
		
	    List<String> ignoreList = new ArrayList<>(); //Arrays.asList("uptimerobot","amazonbot");
	    ignoreList.add("uptimerobot");
	    ignoreList.add("amazonbot");
	    ignoreList.add("youbot");
		
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
			"/home",
			"/news",
			"/continent/africa",	
			"/continent/america",	
			"/continent/asia",	
			"/continent/europe",	
			"/continent/oceania"	
		);		
		
		// .. or request must start with one of this list:
		List<String> startOfString = Arrays.asList(
			"/city",
			"/page"
		);
		
		return (wholeString.stream().anyMatch(request::equals)
			||
			startOfString.stream().anyMatch(request::startsWith)
		);
	}	

}
