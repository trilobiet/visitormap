package com.trilobiet.redistest.ingester;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.trilobiet.visitorstats.ingester.Utils;

public class UtilsTest {
	
	@Test
	void testConversion() {
		
		String ip1 = "192.168.1.10";
		assertEquals(3232235786L,Utils.ipAton(ip1));

		String ip2 = "45.80.169.103";
		assertEquals(760260967L,Utils.ipAton(ip2));

		String ip3 = "255.255.255.255";
		assertEquals(4294967295L,Utils.ipAton(ip3));
	}	
	
	@Test
	void testPathFromRequest() {
		
		String request1 = "GET /continent/europe%3Bjsessionid=4A5AFE3012EA1CAF932117B19B1E1FD1 HTTP/1.1";
		assertEquals("/continent/europe",Utils.pathFromRequest(request1));

		String request2 = "GET /continent/AMERICA?this=that&such&so HTTP/1.1";
		assertEquals("/continent/AMERICA",Utils.pathFromRequest(request2));
	}	
	
}
