package com.trilobiet.visitorstats.ingester;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Format a date in a (date,time) pair
 */
public class DateFormatter {
	
	private final DateTimeFormatter inputFormatter;
	
	public DateFormatter(String inPattern) {
		inputFormatter = DateTimeFormatter.ofPattern(inPattern);
	}

	public Pair<String,String> format(String date) {
		
        LocalDateTime dateTime = LocalDateTime.parse(date, inputFormatter);
        return Pair.of(dateTime.toLocalDate().toString(),dateTime.toLocalTime().toString());
	}

}
