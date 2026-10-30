package com.trilobiet.visitorstats.ingester;

import redis.clients.jedis.RedisClient;

public class LuaScripts {
	
	private LuaScripts() {}

	// ZRANGE prefix:ipranges 3845672890 0 BYSCORE REV LIMIT 0 1
    public static String insertVisitsScript = ""
    		
        + "local lookUpKey = '[prefix]:ipranges' " 
        + "local ipAsNumber = ARGV[1] "
        + "local date = ARGV[2] "
        + "local request = ARGV[3] "
        /*
           In ipranges lookup what location ip must be mapped to
           ZRANGE prefix:ipranges 17171400 0 BYSCORE REV LIMIT 0 1 -> prefix:ipranges 17171400 0 BYSCORE REV LIMIT 0 1
           lookup location:ipstart by ip aton ("India:Karnataka:Bengaluru")
        */   
		+ "local locations = redis.call('ZRANGE', lookUpKey, ipAsNumber, 0, 'BYSCORE', 'REV', 'LIMIT', 0, 1) "
        
		// ZRANGE returns a list, so pick the first (and only) element
		+ "local location = locations[1] "
		
		// Distill the country from location (we also count totals by country)
		+ "local country = string.match(location, '([^:]+)') "
		
		// visits for all pages in website
        + "local allVisitsKey = '[prefix]:visits:all' .. ':' .. date "
        + "local allVisitsKeyAllDates = '[prefix]:visits:all:totals' "
        + "redis.call('zincrby', allVisitsKey, 1, location) "
        + "redis.call('zincrby', allVisitsKeyAllDates, 1, location) "
        + "redis.call('zincrby', allVisitsKey, 1, country) "
        + "redis.call('zincrby', allVisitsKeyAllDates, 1, country) "
        
        // Visits per page in website
        + "local pageVisitsKey = '[prefix]:visits:' .. request .. ':' .. date "
        + "local pageVisitsKeyAllDates = '[prefix]:visits:' .. request .. ':totals' "
        + "redis.call('zincrby', pageVisitsKey, 1, location) "
        + "redis.call('zincrby', pageVisitsKeyAllDates, 1, location) "
        + "redis.call('zincrby', pageVisitsKey, 1, country) "
        + "redis.call('zincrby', pageVisitsKeyAllDates, 1, country) "
		;

    public static String loadInsertVisitsScript(RedisClient redisClient, String prefix) {
    	
    	return redisClient.scriptLoad(insertVisitsScript.replace("[prefix]", prefix));
    }

}
