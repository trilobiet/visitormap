package com.trilobiet.visitorstats.ingester;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trilobiet.visitorstats.ingester.ip2loc.IPRowIngestionStrategy;
import com.trilobiet.visitorstats.ingester.sww.SWWLogRowIngestionStrategy;

import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.RedisClient;

public class App {
	
	private static Logger logger = LoggerFactory.getLogger(App.class);
	
    public static void main(String[] args) throws Exception {
    	
    	String prefix = "sww";
    	
        final ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(64);
        poolConfig.setMaxIdle(64);
        
        try ( RedisClient redisClient = RedisClient.builder().poolConfig(poolConfig).hostAndPort("localhost", 6379).build() ) {
        	
        	logger.info("Cleaning up data...");
        	RedisDataRemover.deleteKeysByPattern(redisClient, prefix + ":*");
        	
        	logger.info("Starting IP ingestion...");
            CSV2RedisIngester ipIngester = new CSV2RedisIngester(redisClient, new IPRowIngestionStrategy(prefix));
            ipIngester.ingestCSV(Path.of("data/IP2LOCATION-LITE-DB5.CSV"));
            logger.info("Ingestion ready!");
            
            logger.info("Starting Nginx Logs ingestion...");
            String scriptSha = LuaScripts.loadInsertVisitsScript(redisClient,prefix);
            CSV2RedisIngester logIngester =  new CSV2RedisIngester(redisClient, new SWWLogRowIngestionStrategy(scriptSha));
            CSVIngestSettings settings = new CSVIngestSettings(CSVIngestSettings.Separator.SPACE);
            logIngester.ingestDirectory(Path.of("/home/acdhirr/Downloads/logs/"), settings);
            logger.info("Ingestion ready!");
        }    
    }
    
}
