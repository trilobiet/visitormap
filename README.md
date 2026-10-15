
I wrote a Java web visits map visualizer using Redis and Leaflet

There's an article at hashnode describing its workings

Use it at your own peril 


How the REDIS database is built and ingested
============================================

Later


How the REDIS database is used 
============================================

1. Pick a region (a geo square or a point with radius, maybe the whole world) - can be gotten from a leaflet map (its center)
2. Feed it to REDIS:
     
      GEORADIUS sww:geo 4.4 51.9 +inf KM WITHCOORD
      
  Or
  
      GEOSEARCH sww:geo FROMLONLAT 4 51 BYBOX 400 400 KM WITHCOORD

  Returning:
  
	  1) 1) "Belgium:Hainaut:Antoing"
	     2) 1) "3.4492000937461853"
	        2) "50.56765037535527085"
	  2) 1) "Belgium:Hainaut:Tournai"
	     2) 1) "3.38932245969772339"
	        2) "50.60714893518124313"
	  3) 1) "Belgium:Hainaut:Pecq"
	     2) 1) "3.33788841962814331"
	        2) "50.68618914509291074"
	  ...
	  
3. Pick a visit metric. 
   For instance visits to all URLs, totals: sww:visits:all:totals
   Or visits to all URLs, on a specific day: sww:visits:all:2026-09-20
   Or visits to a specific URL, on a specific day: sww:visits:/news:2026-09-16
  
      ZRANGE sww:visits:/news:2026-09-15 +inf -inf BYSCORE REV WITHSCORES [LIMIT 0 20]
      
  Returning (every even row is the number of visits in the requested period):      

	   1) "Philippines:Iloilo:Leganes"
	   2) "377"
	   3) "United States of America:Nebraska:Kimball"
	   4) "355"
	   5) "Germany:Hessen:Bensheim"
	   6) "57"
	   7) "Hong Kong:Hong Kong:Sha Ha"
	   8) "48"
	   9) "United States of America:Tennessee:Shiloh"
	  10) "29"
	  11) "Turkiye:Hakkari:Seckin"
	  12) "12"
	  13) "United States of America:Iowa:Waukee"
	  14) "10"
	  15) "United States of America:Pennsylvania:Kempton"
	  16) "8"
	  17) "Sweden:Norrbottens lan:Klusan"
	  18) "5"
	  19) "Pakistan:Khyber Pakhtunkhwa:Dir"
	  20) "5"

4. In Java stream through the list retrieved at 2 and pick the associated metrics from the list retrieved at 3. 
   This must then be combined in a list of metric objects (name, visits, geocoords).
   
      "Germany:Hessen:Bensheim", 57, (8.595,49.698)	  
	  
	        
    

