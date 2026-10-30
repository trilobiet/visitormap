package com.trilobiet.visitorstats.ingester;

import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trilobiet.visitorstats.ingester.ip2loc.IPRowIngestionCountryStrategy;
import com.trilobiet.visitorstats.ingester.ip2loc.IPRowIngestionStrategy;
import com.trilobiet.visitorstats.ingester.sww.SWWLogRowIngestionStrategy;

import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.RedisClient;

public class App {

	private static Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws Exception {

		/*
		 * Arguments:
		 * 
		 * prefix "sww" 
		 * cleanup true 
		 * strategyname 
		 * file
		 * dir
		 * 
		 */

		String prefix = "sww";

		CommandLineParser parser = new DefaultParser();
		CommandLine line;

		final ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
		poolConfig.setMaxTotal(64);
		poolConfig.setMaxIdle(64);

		try (RedisClient redisClient = RedisClient.builder()
				.poolConfig(poolConfig)
				.hostAndPort("localhost", 6379)
				.build()) {

			logger.info("Cleaning up data...");
			RedisDataRemover.deleteKeysByPattern(redisClient, prefix + ":*");

			logger.info("Starting IP ingestion...");
			CSV2RedisIngester ipIngester = new CSV2RedisIngester(redisClient, new IPRowIngestionStrategy(prefix));
			ipIngester.ingestCSV(Path.of("/home/acdhirr/Downloads/logs/ip2location/IP2LOCATION-LITE-DB5.CSV"));
			logger.info("Ingestion ready!");

			/*
			 * IP2LOCATION also contains *some* clean country coordinates that we will
			 * harvest in this pass. These will overwrite the crude country harvesting that
			 * was done at the Ip ingestion.
			 */
			logger.info("Starting Country ingestion...");
			CSV2RedisIngester countryIngester = new CSV2RedisIngester(redisClient,
					new IPRowIngestionCountryStrategy(prefix));
			countryIngester.ingestCSV(Path.of("/home/acdhirr/Downloads/logs/ip2location/IP2LOCATION-LITE-DB5.CSV"));
			logger.info("Country ingestion ready!");

			logger.info("Starting Nginx Logs ingestion...");
			String scriptSha = LuaScripts.loadInsertVisitsScript(redisClient, prefix);
			CSV2RedisIngester logIngester = new CSV2RedisIngester(redisClient,
					new SWWLogRowIngestionStrategy(scriptSha));
			CSVIngestSettings settings = new CSVIngestSettings(CSVIngestSettings.Separator.SPACE);
			logIngester.ingestDirectory(Path.of("/home/acdhirr/Downloads/logs/sww/"), settings);
			logger.info("Ingestion ready!");
		}
	}
	
	private Options cmdOptions() {
	
		Option prefix = Option.builder("p").longOpt("prefix").hasArg().type(String.class).required().build();
		Option cleanUp = Option.builder("c").longOpt("cleanup").type(Boolean.class).build();
		Option strategyName = Option.builder("s").longOpt("strategy").hasArg().type(String.class).required().build();
		Option file = Option.builder("f").longOpt("file").hasArg().type(String.class).build();
		Option dir = Option.builder("d").longOpt("dir").hasArg().type(String.class).build();
		
		OptionGroup fileOrDir = new OptionGroup();
		fileOrDir.addOption(file).addOption(dir);
		fileOrDir.setRequired(true);
		
		return new Options()
			.addOption(prefix)
			.addOption(cleanUp)
			.addOption(strategyName)
			.addOptionGroup(fileOrDir);
	}
	
	

}

