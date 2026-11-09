package com.trilobiet.visitorstats.ingester.sww;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class NginxLogRecordIngestionStrategyTest {
	


	@Test
	void test_passableRequestsMustPass() throws Exception {
		
		assertTrue(getDoIngest("/","200","Mozilla"));
		assertTrue(getDoIngest("/city/rotterdam","200","Blablabla"));
	}
	
	@Test
	void testUnPassableRequestsMustNotPass() throws Exception {
		
		assertFalse(getDoIngest("/res/js/jquery-2.2.4.min.js","200","Mozilla"));
		assertFalse(getDoIngest("/ga.gif","200","Mozilla"));
		assertFalse(getDoIngest("/city/rotterdam","404","Mozilla"));
		assertFalse(getDoIngest("/city/rotterdam","404","UptimeRobot"));
		assertFalse(getDoIngest("/city/rotterdam","404","youbot"));
		assertFalse(getDoIngest("/city/rotterdam","404","amazonbot"));
	}	
	
	private boolean getDoIngest(String request, String status, String userAgent) throws Exception {

		SWWLogRowIngestionStrategy str = new SWWLogRowIngestionStrategy(null);
		
		// Using a bit of reflection here to test a private method
		Method method = SWWLogRowIngestionStrategy.class.getDeclaredMethod("doIngest", String.class, String.class, String.class);
		method.setAccessible(true);
		
		return (boolean) method.invoke(str, request, status, userAgent);
	}
}
