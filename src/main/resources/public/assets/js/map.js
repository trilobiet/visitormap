
var map = L.map('map').setView([51.904, 4.463], 14);

L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(map);

const markerLayer = L.layerGroup().addTo(map);

//fetch(`/api/markers?minLat=${minLat}&minLng=${minLng}&maxLat=${maxLat}&maxLng=${maxLng}`)
/*fetch(`/api/markers?minLat=4.4&minLng=51.5&maxLat=5.4&maxLng=52.5`)
	.then(response => response.json())
	.then(geoJsonData => {
	
	alert(geoJsonData)
	
    // Clear old pins out of view
    markerLayer.clearLayers();
    
    // Plot new markers and hook up popups
    L.geoJSON(geoJsonData, {
        onEachFeature: function (feature, layer) {
            // Check if the feature has properties defined
            if (feature.properties?.name) {
                layer.bindPopup(`<b>${feature.properties.name}</b><br>${feature.properties.description}`);
            }
        }
    }).addTo(markerLayer);
})
.catch(err => console.error("Error loading map data:", err));	
*/

map.on('moveend', viewportChanged);


function viewportChanged(e) {
	
	const map = e.target;
	
	console.log("Viewport changed ", map.getBounds())
	
	const center = map.getCenter();
	const lon = center.lng;
	const lat = center.lat;

	// Get the bounding box corners to calculate real-world distance
	const bounds = map.getBounds();
	const northEast = bounds.getNorthEast();
	const southWest = bounds.getSouthWest();

	// Calculate width at current latitude
	const leftPoint = L.latLng(lat, southWest.lng);
	const rightPoint = L.latLng(lat, northEast.lng);
	const widthInMeters = map.distance(leftPoint, rightPoint);

	// Calculate height
	const bottomPoint = L.latLng(southWest.lat, lon);
	const topPoint = L.latLng(northEast.lat, lon);
	const heightInMeters = map.distance(bottomPoint, topPoint);

	// Convert to kilometers
	const widthInKm = widthInMeters / 1000;
	const heightInKm = heightInMeters / 1000;	
	
	const params = new URLSearchParams();
	params.append("lat", lat);
	params.append("lon", lon);
	params.append("width", widthInKm);
	params.append("height", heightInKm);
	
	console.log('/api/markers?',params)
	// http://localhost:7070/api/markers?lat=51.860167605042925&lon=4.556579589843751&width=90.53323848030078&height=51.25580116315021
	
	fetch(`/api/markers?${params}`)
		.then(response => response.json())
		.then(geoJsonData => {
		
		console.log(geoJsonData)
		
	    // Clear old pins out of view
	    markerLayer.clearLayers();
	    
	    // Plot new markers and hook up popups
	    L.geoJSON(geoJsonData, {
	        onEachFeature: function (feature, layer) {
	            // Check if the feature has properties defined
	            if (feature.properties?.name) {
	                layer.bindPopup(`<b>${feature.properties.name}</b><br>${feature.properties.description}`);
	            }
	        }
	    }).addTo(markerLayer);
	})
	.catch(err => console.error("Error loading map data:", err));
	
}



