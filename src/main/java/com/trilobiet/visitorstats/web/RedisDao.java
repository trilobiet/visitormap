package com.trilobiet.visitorstats.web;

import java.util.List;
import java.util.Set;

public interface RedisDao {
	
	List<Location> getForArea(double lat, double lon, double widthKm, double heightKm); 
	Set<String> getKeys();
	Set<String> getDates();

}
