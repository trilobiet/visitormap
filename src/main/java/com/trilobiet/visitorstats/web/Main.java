package com.trilobiet.visitorstats.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trilobiet.visitorstats.ingester.App;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Main {
	
	private static Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String... args) {
		
		RedisDao dao = new RedisDaoDummyImpl();
	    
		Javalin app = Javalin.create(config -> {

			//config.routes.get("/", ctx -> ctx.result("Hello World"));
			
			// css, js, images
	    	config.staticFiles.add("public", Location.CLASSPATH);
	    	
	    	config.routes.get("/api/markers", ctx -> {
	    		
	    		logger.info(ctx.fullUrl());
	    		
	    		// Todo we need some input validation here on the params being available and well formed  
                double lat = Double.parseDouble(ctx.queryParam("lat"));
                double lon = Double.parseDouble(ctx.queryParam("lon"));
                double width = Double.parseDouble(ctx.queryParam("width"));
                double height = Double.parseDouble(ctx.queryParam("height"));
                
                List<com.trilobiet.visitorstats.web.Location> locations = dao.getForArea(0, 0 , width, height);
                
                GeoJsonFeatureCollection geoCollection = GeoJsonFeatureCollection.from(locations);

                ctx.json(geoCollection);
                
            });	    	
	    	
	    });
		
	    app.start(7070);
	}
	
	//private static getConfig
}

