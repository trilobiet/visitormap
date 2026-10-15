package com.trilobiet.visitorstats.ingester;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.opencsv.processor.RowProcessor;

import redis.clients.jedis.Pipeline;
import redis.clients.jedis.RedisClient;

public class CSV2RedisIngester {

	private final RedisClient redisClient;
	private final RowIngestionStrategy ingestionStrategy;
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	private RowProcessor rowProcessor;

	/**
	 * RowProcessor can be used to prepare and alter a CSV row before any other processing
	 * @param rowProcessor
	 */
	public void setRowProcessor(RowProcessor rowProcessor) {
		this.rowProcessor = rowProcessor;
	}

	public CSV2RedisIngester(RedisClient redisClient, RowIngestionStrategy ingestionStrategy) {
    	
		this.redisClient = redisClient;
		this.ingestionStrategy = ingestionStrategy;
	}

    public void ingestDirectory(Path path) throws IOException, CsvValidationException {
    	ingestDirectory(path, new CSVIngestSettings());
    }
    
    
    public void ingestDirectory(Path path, CSVIngestSettings settings) throws IOException, CsvValidationException {
    	
    	// maxdepth = 1 (no going into subdirs)
    	try ( Stream<Path> paths = Files.walk(path,1) ) {
    		
    	    Iterator<Path> pi = paths.filter(Files::isRegularFile).iterator();
    	    while (pi.hasNext()) ingestCSV(pi.next(), settings);
    	}
    }
    
    
    public void ingestCSV(Path path) throws IOException, CsvValidationException {
    	ingestCSV(path, new CSVIngestSettings());
    }
    
    
	public void ingestCSV(Path path, CSVIngestSettings settings) throws IOException, CsvValidationException {
    	
    	CSVParser parser = new CSVParserBuilder()
    	    .withSeparator(settings.separator().character) 
    	    .build();
    	
    	CSVReaderBuilder csvReaderBuilder = new CSVReaderBuilder(Files.newBufferedReader(path))
   			.withSkipLines(settings.skiplines())
   			.withCSVParser(parser);

    	if (rowProcessor != null)
    		csvReaderBuilder.withRowProcessor(rowProcessor);
    	
    	try ( CSVReader csvReader = csvReaderBuilder.build() ) {
    		
    		Pipeline p = redisClient.pipelined();
    	    String[] row;
    	    int count =0; 
    	    
			while ((row = csvReader.readNext()) != null) {
				
				ingestionStrategy.ingestRow(row, p);
				count++;
    	        
    	        // batches of [batchSize] records
    	        if (count % settings.batchSize() == 0) {
    	        	p.sync();
    	        	logger.info("Ingested {} records from {}", count, path);
    	        }
    	    }
    	    
    	    p.sync();
    	}
    }
	
}
