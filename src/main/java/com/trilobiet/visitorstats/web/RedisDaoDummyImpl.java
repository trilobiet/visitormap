package com.trilobiet.visitorstats.web;

import java.util.ArrayList;
import java.util.List;

public class RedisDaoDummyImpl implements RedisDao {

	@Override
	public List<Location> getForArea(double lat, double lon, double widthKm, double heightKm) {
		
		List<Location> lst = new ArrayList<>();
		
		lst.add(new Location("Rotterdam", 51.922, 4.481, 123));
		lst.add(new Location("Delfshaven", 51.908, 4.449, 234));
		lst.add(new Location("Overschie", 51.936, 4.427, 345));
		
		return lst;
	}

}
