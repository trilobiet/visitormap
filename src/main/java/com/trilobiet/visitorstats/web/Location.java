package com.trilobiet.visitorstats.web;

public record Location (

	String name,
	double lat,
	double lon,
	int visits
) {
}
