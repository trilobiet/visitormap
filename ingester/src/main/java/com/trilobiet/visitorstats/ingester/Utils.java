package com.trilobiet.visitorstats.ingester;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
	
	private Utils() {}  // No public constructor

    /*
     *  Return a Long representation of an IP Address
     */
    public static Long ipAton(String ip) {
    	
    	List<String> ipParts = Arrays.asList(ip.split("\\."));
    	
    	return ipParts.stream()
    		.mapToLong(Long::parseLong)
    		.reduce(0L, (a, b) -> (a << 8) | b);
    }	
    
    
	public static String pathFromRequest(String input) {
		
		/*  "GET /continent/AMERICA%3Bjsessionid=4A5AFE3012EA1CAF932117B19B1E1FD1 HTTP/1.1"
		 *  "GET /continent/AMERICA?jsessionid=4A5AFE3012EA1CAF932117B19B1E1FD1 HTTP/1.1"
		 *  ->
		 *  /continent/AMERICA
		 */
		
		// Remove query strings etc.
		Pattern p1 = Pattern.compile("/[^%;?\s]*");
		Matcher m1 = p1.matcher(input);
		if (m1.find()) input = m1.group(0);
		
		// Remove trailing slash(es) except for single slash (home)
		while (input.endsWith("/") && input.length() > 1) {
		    input = input.substring(0, input.length() - 1);
		}
		
		return input;
	}	
	
}

