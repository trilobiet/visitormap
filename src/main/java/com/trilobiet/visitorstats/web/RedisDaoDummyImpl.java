package com.trilobiet.visitorstats.web;

import java.util.ArrayList;
import java.util.List;

public class RedisDaoDummyImpl implements RedisDao {

	@Override
	public List<Location> getForArea(double lat, double lon, double widthKm, double heightKm) {
		
		List<Location> lst = new ArrayList<>();
		
		lst.add(new Location("Rotterdam", 51.922, 4.481, 723));
		lst.add(new Location("Delfshaven", 51.908, 4.449, 5234));
		lst.add(new Location("Overschie", 51.936, 4.427, 1345));

		lst.add(new Location("loc1", 51.912, 4.46, 723));
		lst.add(new Location("loc2", 51.948, 4.45, 5234));
		lst.add(new Location("loc3", 51.9330, 4.42, 1345));
		
		return lst;
	}

}
