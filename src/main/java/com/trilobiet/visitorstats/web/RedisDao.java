package com.trilobiet.visitorstats.web;

import java.util.List;

public interface RedisDao {
	
	List<Location> getForArea(double lat, double lon, double widthKm, double heightKm); 

}
