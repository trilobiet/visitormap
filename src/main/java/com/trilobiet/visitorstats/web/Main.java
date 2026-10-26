package com.trilobiet.visitorstats.web;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trilobiet.visitorstats.ingester.App;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.RedisClient;

public class Main {
	
	private static Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String... args) {
		
		RedisClient redisClient = getRedisClient();
		
		RedisDao dao = new RedisDaoImpl(redisClient);
		//RedisDao dao = new RedisDaoDummyImpl();
	    
		Javalin app = Javalin.create(config -> {

			//config.routes.get("/", ctx -> ctx.result("Hello World"));
			
			// css, js, images
	    	config.staticFiles.add("public", Location.CLASSPATH);
	    	
		    // close redisClient on shutdown
	        config.events.serverStopping(() -> {
	            logger.info("Javalin stopped. Closing redisClient... ");
	            redisClient.close();
	        });
	    	
	    	config.routes.get("/api/markers", ctx -> {
	    		
	    		logger.info(ctx.fullUrl());
	    		
	    		// Todo we need some input validation here on the params being available and well formed  
                double lat = Double.parseDouble(ctx.queryParam("lat"));
                double lon = Double.parseDouble(ctx.queryParam("lon"));
                double width = Double.parseDouble(ctx.queryParam("width"));
                double height = Double.parseDouble(ctx.queryParam("height"));
                String path = ctx.queryParam("path");
                String date = ctx.queryParam("date");
                
                List<com.trilobiet.visitorstats.web.Location> locations = dao.getForArea(lat, lon , width, height, path, date);
                
                GeoJsonFeatureCollection geoCollection = GeoJsonFeatureCollection.from(locations);

                ctx.json(geoCollection);
                
            });	    
	    	
	    	// Get keys as a list of options
	    	config.routes.get("/keys", ctx -> {
	    		
	    		Set<String> keys = dao.getKeys();
	    		
	    		List<String> options = keys.stream()
	    			.map(key -> "<option>" + key + "</option>")
	    			.toList();
	    		
	    		ctx.result(String.join("",options));
	    	});

	    	// Get dates as a list of options
	    	config.routes.get("/dates", ctx -> {
	    		
	    		Set<String> dates = dao.getDates();
	    		
	    		List<String> options = dates.stream()
	    			.map(date -> "<option>" + date + "</option>")
	    			.toList();
	    		
	    		ctx.result(String.join("",options));
	    	});
	    	
	    });
		
	    app.start(7070);
	}
	
	
	public static RedisClient getRedisClient() {
		
		ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
		poolConfig.setMaxTotal(64);
		poolConfig.setMaxIdle(64);

		return RedisClient.builder()
			.poolConfig(poolConfig)
			.hostAndPort("localhost", 6379)
			.build();
    }
	
}

