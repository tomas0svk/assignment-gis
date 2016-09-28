# General course assignment

Build a map-based application, which lets the user see geo-based data on a map and filter/search through it in a meaningfull way. Specify the details and build it in your language of choice. The application should have 3 components:

1. Custom-styled background map, ideally built with [mapbox](http://mapbox.com). Hard-core mode: you can also serve the map tiles yourself using [mapnik](http://mapnik.org/) or similar tool.
2. Local server with [PostGIS](http://postgis.net/) and an API layer that exposes data in a [geojson format](http://geojson.org/).
3. The user-facing application (web, android, ios, your choice..) which calls the API and lets the user see and navigate in the map and shows the geodata. You can (and should) use existing components, such as the Mapbox SDK, or [Leaflet](http://leafletjs.com/).

## Example projects

- Showing nearby landmarks as colored circles, each type of landmark has different circle color and the more interesting the landmark is, the bigger the circle. Landmarks are sorted in a sidebar by distance to the user. It is possible to filter only certain landmark types (e.g., castles).

- Showing bicykle roads on a map. The roads are color-coded based on the road difficulty. The user can see various lists which help her choose an appropriate road, e.g. roads that cross a river, roads that are nearby lakes, roads that pass through multiple countries, etc.

## Data sources
- [Open Street Maps](https://www.openstreetmap.org/)

## My project

**Application description**: 
For planning a trip, e.g. cycling or hiking tour, there are two main parameters considered: distance and altitude changes.
However there are problems with evaluating both of them:
- Distance can be quite easily fetched using routing tools, e.g. those in google maps. However, these maps do not include pathways, whose are mostly interesting for hiking or cycling.
- Altitude cannot be automatically gained in any tool known to me, one of the reasons may be, that altidude data are not even preseent in maps, e.g. openstreetmap.

My plan is to make a tool in which the user could select points of the route being planned, the pgrouting algorithm automatically routes the pathways and they are shown on the map.
Points will be movable, to make it possible to correct, or fine-tune the route being planned.
In the next step, the algorithm will fetch altitude across the points being computed by the routing algorithm. As a result, trip statistics (e.g. altitude chart as known from cycling competitions) are provided.
The path segments can also be colored by their incline/difficulty.

**Data source**: 
- [Open Street Maps](https://www.openstreetmap.org/)
- [Shuttle Radar Topography Mission - JPL - NASA](http://www2.jpl.nasa.gov/srtm/)

**Technologies used**: 
Web programming: Mapbox, JS, Java + connectors(Wicket, DirectWebRemoting)
DB and its access: Postgres, PL/PGSQL, JDBC
GIS functions: PostGIS, pgrouting
