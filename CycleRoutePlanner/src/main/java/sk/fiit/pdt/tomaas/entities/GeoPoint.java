package sk.fiit.pdt.tomaas.entities;

import org.apache.wicket.ajax.json.JSONObject;

/**
 * Entity for describing a point in 3D coordinate system - (lat, lng, elevation)
 * @author Tomas
 *
 */
public class GeoPoint {
	double lat;
	double lng;
	long ID;
	double ele;

	public GeoPoint(double lat, double lng) {
		super();
		this.lat = lat;
		this.lng = lng;
	}
	
	public GeoPoint(double lat, double lng, long ID) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.ID = ID;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public long getID() {
		return ID;
	}

	public void setID(long iD) {
		ID = iD;
	}

	public double getEle() {
		return ele;
	}

	public void setEle(double ele) {
		this.ele = ele;
	}
		
	public static GeoPoint getPointFromStText(String pointAsText) {
		// "POINT(18.3929313 48.6276097)"
		float lat;
		float lng;
		lat = Float.parseFloat(pointAsText.split(" ")[1].substring(0, pointAsText.split(" ")[1].length()-1));
		lng = Float.parseFloat(pointAsText.split(" ")[0].substring(6));

		return new GeoPoint(lat, lng);
	}
	
	public static GeoPoint getPointFromStText_full(String pointAsText) {
		// "POINT(18.3929313 48.6276097) 123456"
		float lat;
		float lng;
		long id;
		lat = Float.parseFloat(pointAsText.split(" ")[1].substring(0, pointAsText.split(" ")[1].length()-1));
		lng = Float.parseFloat(pointAsText.split(" ")[0].substring(6));
		id = Long.parseLong(pointAsText.split(" ")[2]);

		return new GeoPoint(lat, lng, id);
	}
	
	public static double getPointDistance(GeoPoint p1, GeoPoint p2) {
		double R = 6371000; // metres
		double φ1 = Math.toRadians(p1.getLat()); //this code is copied from stackoverflow
		double φ2 = Math.toRadians(p2.getLat()); //dunno who dares to use such weird var names 
		double Δφ = Math.toRadians((p2.getLat() - p1.getLat()));
		double Δλ = Math.toRadians((p2.getLng() - p1.getLng()));

		double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2)
				+ Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		double d = R * c;
		return d;
	}
	
	@Override
	public String toString(){
		JSONObject me = new JSONObject();
		me.put("ID", getID());
		me.put("lat", getLat());
		me.put("lng", getLng());
		me.put("ele", getEle());
		
		return me.toString();
	}
}
