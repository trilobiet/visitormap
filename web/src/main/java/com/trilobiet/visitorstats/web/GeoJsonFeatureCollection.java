package com.trilobiet.visitorstats.web;

import java.util.List;

public record GeoJsonFeatureCollection(
		
	String type,
	List<GeoJsonFeature> features
) {

	public static GeoJsonFeatureCollection from(List<Location> locations) {
		
		List<GeoJsonFeature> lst = locations.stream().map( loc ->
			new GeoJsonFeature(
				"Feature", 
				new Geometry("Point", List.of(loc.lon(), loc.lat())),
				new Properties(loc.name(), loc.visits())
			)	
		).toList();
		
		return new GeoJsonFeatureCollection("FeatureCollection", lst);
	}
}

record GeoJsonFeature( String type, Geometry geometry, Properties properties ) {}
record Geometry( String type, List<Double> coordinates ) {}
record Properties( String name, Double visits ) {}

