
//////////////////app functions

function getStats() {
	var params = {
		"sessionID" : sessionID
	};
	MapPageDWR.getStatistics(JSON.stringify(params), {
		callback : function(str) {
			var res = JSON.parse(str);
			var popup = new mapboxgl.Popup().setLngLat(
					geojson_Pt.features[0].geometry.coordinates).setHTML(
					'<img src="data:image/png;base64,' + res.img + '" />')
					.addTo(map);
		}
	});
}

function deleteLastN(n) {
	// delete last n points from data arays
	console.log(n);
	if (geojson_Pt.features.length > n) {
		geojson_Pt.features.splice(-1, n);
		geojson_Ln_g.features.splice(-1, n);
		geojson_Ln_b.features.splice(-1, n);
		geojson_Ln_o.features.splice(-1, n);
		geojson_Ln_r.features.splice(-1, n);
		var params = {
			"sessionID" : sessionID,
			"n" : n
		};
		MapPageDWR.removeRoutes(JSON.stringify(params), {
			callback : function(str) {
			}
		});
	}
	if (geojson_Pt.features.length === n) {
		geojson_Pt.features.splice(0, n);
		geojson_Ln_g.features.splice(0, n - 1);
		geojson_Ln_b.features.splice(0, n - 1);
		geojson_Ln_o.features.splice(0, n - 1);
		geojson_Ln_r.features.splice(0, n - 1);
		var params = {
			"sessionID" : sessionID,
			"n" : n - 1
		};
		MapPageDWR.removeRoutes(JSON.stringify(params), {
			callback : function(str) {
			}
		});
	}
	// redraw
	map.getSource('point').setData(geojson_Pt);
	map.getSource('route_g').setData(geojson_Ln_g);
	map.getSource('route_b').setData(geojson_Ln_b);
	map.getSource('route_o').setData(geojson_Ln_o);
	map.getSource('route_r').setData(geojson_Ln_r);
}

function findNearestMarker(array, lat, lng) {
	// finds nearest point within geojson array
	var nearestL = 999999999;
	var nearestI = -1;

	for (i = 0; i < geojson_Pt.features.length; i++) {
		var pLat = array.features[i].geometry.coordinates[0];
		var pLng = array.features[i].geometry.coordinates[1];

		if (Math.sqrt(Math.abs(lat - pLat) * Math.abs(lat - pLat)
				+ Math.abs(lng - pLng) * Math.abs(lng - pLng)) < nearestL) {
			nearestI = i;
			nearestL = Math.sqrt(Math.abs(lat - pLat) * Math.abs(lat - pLat)
					+ Math.abs(lng - pLng) * Math.abs(lng - pLng));
		}
	}
	return nearestI;
}


function addPoint(array, data) {
	// pushes a point to an array specified
	array.features.push({
		"type" : "Feature",
		"ID" : data.ID,
		"geometry" : {
			"type" : "Point",
			"coordinates" : [ data.lng, data.lat ]
		}
	});
}

function addLine(array, data) {
	// pushes a line to an array specified
	array.features.push({
		"type" : "Feature",
		"geometry" : {
			"type" : "MultiLineString",
			"coordinates" : data
		}
	});
}

function initLayers(map, data) {
	// Add a points layer to the map
	map.addSource('point', {
		"type" : "geojson",
		"data" : data.geojson_Pt
	});

	// Add a lines layer to the map - green = no incline
	map.addSource('route_g', {
		"type" : "geojson",
		"data" : data.geojson_Ln_g
	});
	// ...brown = slight incline
	map.addSource('route_b', {
		"type" : "geojson",
		"data" : data.geojson_Ln_b
	});
	// ...orange = mid incline
	map.addSource('route_o', {
		"type" : "geojson",
		"data" : data.geojson_Ln_o
	});
	// ...red = steep incline
	map.addSource('route_r', {
		"type" : "geojson",
		"data" : data.geojson_Ln_r
	});

	// add layers
	map.addLayer({
		"id" : "point",
		"type" : "circle",
		"source" : "point",
		"paint" : {
			"circle-radius" : 8,
			"circle-color" : "#b0b0b0"
		}
	});

	map.addLayer({
		"id" : "route_g",
		"type" : "line",
		"source" : "route_g",
		"layout" : {
			"line-join" : "round",
			"line-cap" : "round"
		},
		"paint" : {
			"line-color" : "#00a000",
			"line-width" : 4
		}
	});

	map.addLayer({
		"id" : "route_b",
		"type" : "line",
		"source" : "route_b",
		"layout" : {
			"line-join" : "round",
			"line-cap" : "round"
		},
		"paint" : {
			"line-color" : "#83A800",
			"line-width" : 4
		}
	});

	map.addLayer({
		"id" : "route_o",
		"type" : "line",
		"source" : "route_o",
		"layout" : {
			"line-join" : "round",
			"line-cap" : "round"
		},
		"paint" : {
			"line-color" : "#CC9008",
			"line-width" : 4
		}
	});

	map.addLayer({
		"id" : "route_r",
		"type" : "line",
		"source" : "route_r",
		"layout" : {
			"line-join" : "round",
			"line-cap" : "round"
		},
		"paint" : {
			"line-color" : "#AD0000",
			"line-width" : 4
		}
	});
}

////////////////////event handler functions

function mouseDown(e) {
	// initialize drag&drop
	if (!isCursorOverPoint) {
		lastMousePos = e.point;
		map.on('mouseup', onClickUp);
	} else {
		isDragging = true;
		var coords = e.lngLat;
		draggingInx = findNearestMarker(geojson_Pt, coords.lng, coords.lat);
		console.log(draggingInx);
		canvas.style.cursor = 'grab';
		map.on('mousemove', onMove);
		map.on('mouseup', onDragUp);
	}
}

function onMove(e) {
	if (!isDragging)
		return;
	var coords = e.lngLat;
	canvas.style.cursor = 'grabbing';
	if (draggingInx > -1) {
		geojson_Pt.features[draggingInx].geometry.coordinates = [ coords.lng,
				coords.lat ];
		map.getSource('point').setData(geojson_Pt);
	}
}

function onDragUp(e) {
	if (!isDragging) {
		return;
	}

	// retrace routes to a moved point
	MapPageDWR
			.getNearestPoint(
					JSON.stringify(e.lngLat),
					{
						callback : function(str) {
							var data = JSON.parse(str);
							geojson_Pt.features[draggingInx].geometry.coordinates = [
									data.lng, data.lat ];
							geojson_Pt.features[draggingInx].ID = data.ID;
							map.getSource('point').setData(geojson_Pt);
							// reroute if last one
							if (draggingInx == geojson_Pt.features.length - 1) {
								var nrOfPts = geojson_Pt.features.length;
								if (nrOfPts > 1) {
									var IDs = {
										"ID1" : geojson_Pt.features[nrOfPts - 1].ID,
										"ID2" : geojson_Pt.features[nrOfPts - 2].ID,
										"inx" : draggingInx - 1,
										"sessionID" : sessionID
									};
									MapPageDWR
											.getRoute(
													JSON.stringify(IDs),
													{
														callback : function(str) {
															var data = JSON
																	.parse(str);
															geojson_Ln_g.features[draggingInx - 1].geometry.coordinates = data.coordinates_g;
															geojson_Ln_b.features[draggingInx - 1].geometry.coordinates = data.coordinates_b;
															geojson_Ln_o.features[draggingInx - 1].geometry.coordinates = data.coordinates_o;
															geojson_Ln_r.features[draggingInx - 1].geometry.coordinates = data.coordinates_r;
															map
																	.getSource(
																			'route_g')
																	.setData(
																			geojson_Ln_g);
															map
																	.getSource(
																			'route_b')
																	.setData(
																			geojson_Ln_b);
															map
																	.getSource(
																			'route_o')
																	.setData(
																			geojson_Ln_o);
															map
																	.getSource(
																			'route_r')
																	.setData(
																			geojson_Ln_r);
														}
													});
								}
							}
							// reroute if middle one
							else if (draggingInx > 0) {
								var IDs = {
									"ID1" : geojson_Pt.features[draggingInx].ID,
									"ID2" : geojson_Pt.features[draggingInx + 1].ID,
									"inx" : draggingInx,
									"sessionID" : sessionID
								};
								MapPageDWR
										.getRoute(
												JSON.stringify(IDs),
												{
													callback : function(str) {
														var data = JSON
																.parse(str);
														geojson_Ln_g.features[draggingInx].geometry.coordinates = data.coordinates_g;
														geojson_Ln_b.features[draggingInx].geometry.coordinates = data.coordinates_b;
														geojson_Ln_o.features[draggingInx].geometry.coordinates = data.coordinates_o;
														geojson_Ln_r.features[draggingInx].geometry.coordinates = data.coordinates_r;
														map
																.getSource(
																		'route_g')
																.setData(
																		geojson_Ln_g);
														map
																.getSource(
																		'route_b')
																.setData(
																		geojson_Ln_b);
														map
																.getSource(
																		'route_o')
																.setData(
																		geojson_Ln_o);
														map
																.getSource(
																		'route_r')
																.setData(
																		geojson_Ln_r);
													}
												});
								IDs = {
									"ID1" : geojson_Pt.features[draggingInx - 1].ID,
									"ID2" : geojson_Pt.features[draggingInx].ID,
									"inx" : draggingInx - 1,
									"sessionID" : sessionID
								};
								MapPageDWR
										.getRoute(
												JSON.stringify(IDs),
												{
													callback : function(str) {
														var data = JSON
																.parse(str);
														geojson_Ln_g.features[draggingInx - 1].geometry.coordinates = data.coordinates_g;
														geojson_Ln_b.features[draggingInx - 1].geometry.coordinates = data.coordinates_b;
														geojson_Ln_o.features[draggingInx - 1].geometry.coordinates = data.coordinates_o;
														geojson_Ln_r.features[draggingInx - 1].geometry.coordinates = data.coordinates_r;
														map
																.getSource(
																		'route_g')
																.setData(
																		geojson_Ln_g);
														map
																.getSource(
																		'route_b')
																.setData(
																		geojson_Ln_b);
														map
																.getSource(
																		'route_o')
																.setData(
																		geojson_Ln_o);
														map
																.getSource(
																		'route_r')
																.setData(
																		geojson_Ln_r);
													}
												});
							}
							// reroute if first one
							else {
								var IDs = {
									"ID1" : geojson_Pt.features[0].ID,
									"ID2" : geojson_Pt.features[1].ID,
									"inx" : 0,
									"sessionID" : sessionID
								};
								MapPageDWR
										.getRoute(
												JSON.stringify(IDs),
												{
													callback : function(str) {
														var data = JSON
																.parse(str);
														geojson_Ln_g.features[0].geometry.coordinates = data.coordinates_g;
														geojson_Ln_b.features[0].geometry.coordinates = data.coordinates_b;
														geojson_Ln_o.features[0].geometry.coordinates = data.coordinates_o;
														geojson_Ln_r.features[0].geometry.coordinates = data.coordinates_r;
														map
																.getSource(
																		'route_g')
																.setData(
																		geojson_Ln_g);
														map
																.getSource(
																		'route_b')
																.setData(
																		geojson_Ln_b);
														map
																.getSource(
																		'route_o')
																.setData(
																		geojson_Ln_o);
														map
																.getSource(
																		'route_r')
																.setData(
																		geojson_Ln_r);
													}
												});
							}
						}
					});

	isDragging = false;

	map.off('mousemove', onMove);
	map.off('mouseup', onDragUp);

	canvas.style.cursor = '';
}

function onClickUp(e) {
	// add a point
	if (e.point.x == lastMousePos.x && e.point.y == lastMousePos.y) {
		MapPageDWR.getNearestPoint(JSON.stringify(e.lngLat), {
			callback : function(str) {
				var data = JSON.parse(str);
				addPoint(geojson_Pt, data);
				map.getSource('point').setData(geojson_Pt);

				var nrOfPts = geojson_Pt.features.length;
				if (nrOfPts > 1) {
					var IDs = {
						"ID1" : geojson_Pt.features[nrOfPts - 2].ID,
						"ID2" : geojson_Pt.features[nrOfPts - 1].ID,
						"inx" : geojson_Pt.features.length - 2,
						"sessionID" : sessionID
					};
					MapPageDWR.getRoute(JSON.stringify(IDs), {
						callback : function(str) {
							var data = JSON.parse(str);
							addLine(geojson_Ln_g, data.coordinates_g);
							addLine(geojson_Ln_b, data.coordinates_b);
							addLine(geojson_Ln_o, data.coordinates_o);
							addLine(geojson_Ln_r, data.coordinates_r);

							map.getSource('route_g').setData(geojson_Ln_g);
							map.getSource('route_b').setData(geojson_Ln_b);
							map.getSource('route_o').setData(geojson_Ln_o);
							map.getSource('route_r').setData(geojson_Ln_r);

						}
					});
				}
			}
		});

	}
	map.off('mouseup', onClickUp);
}




