package com.trilobiet.visitorstats.ingester;

import redis.clients.jedis.RedisClient;

public class LuaScripts {
	
	private LuaScripts() {
	}

	// ZRANGE mijntest:ipranges 3845672890 0 BYSCORE REV LIMIT 0 1
    public static String insertVisitsScript = ""
        + "local lookUpKey = '[prefix]:ipranges' " 
        + "local ipAsNumber = ARGV[1] "
        + "local date = ARGV[2] "
        + "local request = ARGV[3] "
        /*
           In ipranges lookup what location ip must be mapped to
           ZRANGE prefix:ipranges 17171400 0 BYSCORE REV LIMIT 0 1 -> sww:ipranges 17171400 0 BYSCORE REV LIMIT 0 1
           lookup location:ipstart by ip aton ("India:Karnataka:Bengaluru=17171200-17171455")
        */   
		+ "local locations = redis.call('ZRANGE', lookUpKey, ipAsNumber, 0, 'BYSCORE', 'REV', 'LIMIT', 0, 1) "
        // ZRANGE returns a list, so pick the first and only element
		+ "local location = locations[1] "
        + "local allVisitsKey = '[prefix]:visits:all' .. ':' .. date "
        + "local allVisitsKeyAllDates = '[prefix]:visits:all:totals' "
        + "redis.call('zincrby', allVisitsKey, 1, location) "
        + "redis.call('zincrby', allVisitsKeyAllDates, 1, location) "
        // Visits per request (page in website)
        + "local requestVisitsKey = '[prefix]:visits:' .. request .. ':' .. date "
        + "redis.call('zincrby', requestVisitsKey, 1, location) "
		;
	

    public static String loadInsertVisitsScript(RedisClient redisClient, String prefix) {
    	
    	return redisClient.scriptLoad(insertVisitsScript.replace("[prefix]", prefix));
    }
	

}
