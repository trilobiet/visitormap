
const theMap = L.map('map',{
		minZoom: 3
	})
	.setView([51.904, 4.463], 6);
	
// selected path & date
const selPath = document.querySelector("#controls [name='path']");
const selDate = document.querySelector("#controls [name='date']");
	

L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(theMap);

// Set up the clustering, and the behaviour inside the clusters
const theMarkersGroup = L.markerClusterGroup({

	iconCreateFunction: function(cluster) {
		
		const markers = cluster.getAllChildMarkers();
		let clusterVisits = 0;
		
		markers.forEach(marker => {
			if (marker.feature?.properties.visits) {
				clusterVisits += marker.feature.properties.visits;
			}
		});
		
		return getIcon("",clusterVisits);
	}	
	
});


// return an icon
function getIcon(label, count) {
	
	// TODO do we need a label?
	// TODO circle size must increase with visits
	
	let	clusterClass = 'vmap-icon-1' 
	
	if (count > 500) {
		clusterClass = 'vmap-icon-3';
	} else if (count > 100) {
		clusterClass = 'vmap-icon-2';
	} 
	
	return L.divIcon({

	  html: this.getCircleAsHtml(count),
	  className: "vmap-marker-icon",
	  iconSize: L.Point(0, 0)
	});		
}


theMap.addLayer(theMarkersGroup);


document.body.addEventListener('htmx:load', function(evt) {
	
    const divControls = document.getElementById('controls');
    
    //if (evt.target === divControls || evt.target.closest('#controls')) {
	if (divControls && !divControls.dataset.mapInitialized && (evt.target === divControls || evt.target.closest('#controls'))) {		
		
		divControls.dataset.mapInitialized = "true";
		
		[selPath, selDate].forEach(selectElement => {
		  selectElement.addEventListener('change', () => renderMarkers(theMap, theMarkersGroup));
		});
        
        theMap.whenReady(() => {
            
			// initial rendering after page load
            renderMarkers(theMap, theMarkersGroup); 
            
            // setup pan & move handler
            theMap.off('moveend'); 
            theMap.on('moveend', () => renderMarkers(theMap, theMarkersGroup));
        });
        
    }
});



function renderMarkers(map, markersGroup) {
	
	//console.log("Options: ", selPath.options.length)
	//console.log("Path & date: ", selPath.value, " - ", selDate.value)

	const dims = getDims(map);
	//console.log("Map dimensions: ", dims);
	
	const params = new URLSearchParams();
	params.append("lat", dims.lat);
	params.append("lon", dims.lon);
	params.append("width", dims.widthKm);
	params.append("height", dims.heightKm);
	params.append("path", selPath.value);
	params.append("date", selDate.value);
	
	//console.log(`/api/markers?${new URLSearchParams(params).toString()}`)
	//console.log('/api/markers?',params)
	// http://localhost:7070/api/markers?lat=51.860167605042925&lon=4.556579589843751&width=90.53323848030078&height=51.25580116315021
	
	fetch(`/api/markers?${params}`)
		.then(response => response.json())
		.then(geoJsonData => {
		
		//console.log(geoJsonData)
		
	    markersGroup.clearLayers();
	    
	    // Render new markers
	    const newLayer = L.geoJSON(geoJsonData, {
			
			pointToLayer: function (feature, latlng) {

			    visits = feature.properties?.visits || 1; // default =1
			    const customIcon = getIcon("", visits);
			    return L.marker(latlng, { icon: customIcon });
			},			
			
	        onEachFeature: function (feature, layer) {

	            if (feature.properties?.name) {
	                layer.bindPopup(`<b>${feature.properties.name}</b><br>${feature.properties.visits}`);
	            }
	        }
	    });
		
		markersGroup.addLayer(newLayer);
	})
	.catch(err => console.error("Error loading map data:", err));
}


function getCircleAsHtml(weight) {
  
	const scalingFactor = 1;
	const circleSizePixels = 150;
	
    const mWeight = Math.sqrt(weight) * scalingFactor // TODO do not calculate every time
    let diameter, clazz

    // set circle properties
    if (mWeight >= circleSizePixels * .9) {
        diameter = mWeight
        clazz = "mb-large"
    }
    else if (mWeight > circleSizePixels * .09) {
        diameter = Math.max(30, mWeight)
        clazz = "mb-medium"
    }
    else if (mWeight > circleSizePixels * .009) {
        diameter = Math.max(30, mWeight)
        clazz = "mb-small"
    }
    else {
        diameter = 20
        clazz = "mb-tiny"
    }

    const radius = diameter / 2;

    const markerbox =
        `<div class="vmap-marker-box ${clazz}" mweight="${mWeight}" style="`
        + `width:${diameter}px;`
        + `height:${diameter}px;`
        + `margin:-${radius}px 0 0 -${radius}px;`
        + `">`
        + `<div class="vmap-marker-circle" style="`
        + `border-radius:${radius}px;`
        + `"></div>`
        + `<div class="vmap-marker-caption" style="`
        + `line-height:${diameter}px;`
        + `">${weight}</div>`
        + `</div>`

    return markerbox;
}


function getDims(map) {
	
    // 1. Get map center
    const center = map.getCenter().wrap();
    const lon = center.lng;
    const lat = center.lat;

	// get map zoom	
	const curZoom = map.getZoom();
	const zoomThreshold = 3;
	
	const maxWidthKm = 40075;
	const maxheightKm = 20004;
	let widthKm = maxWidthKm;
	let heightKm = maxheightKm;
	
    
	// below threshold height and width will be inaccurate so just take the entire world
	if (curZoom > zoomThreshold) { 
	
	    // 2. Get width in Km
	    const circumferenceEquator = 40075000;
	    const zoom = map.getZoom();
	    const metersPerPixelEquator = circumferenceEquator / (256 * Math.pow(2, zoom));
	    const metersPerPixelCenter = metersPerPixelEquator * Math.cos(lat * Math.PI / 180);
	    widthKm = (metersPerPixelCenter * map.getSize().x) / 1000;
	    
	    // 3. Get height in Km
	    const bounds = map.getBounds();
	    const radius = 6371; // earth radius in km
	    const deltaPhi = Math.abs(bounds.getNorth() - bounds.getSouth()) * Math.PI / 180;
	    heightKm = radius * deltaPhi;
	}
    
    // 4. Return dims
    return {
        lon: lon.toFixed(6),
        lat: lat.toFixed(6),
        widthKm: widthKm.toFixed(2),
        heightKm: heightKm.toFixed(2)
    };
}


