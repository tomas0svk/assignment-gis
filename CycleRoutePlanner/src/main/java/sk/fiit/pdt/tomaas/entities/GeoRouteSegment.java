package sk.fiit.pdt.tomaas.entities;

/**
 * Entity for a line segment - using two points
 * @author Tomas
 *
 */
public class GeoRouteSegment {

	GeoPoint start;
	GeoPoint end;
	
	public GeoRouteSegment(GeoPoint start, GeoPoint end){
		this.start = start;
		this.end = end;
	}

	public double getLength() {
		return GeoPoint.getPointDistance(start, end);
	}
	
	public double getDEle(){
		return start.getEle() - end.getEle();
	}
	
	public double getIncline(){
		return Math.abs(getDEle()/getLength());
	}

	public GeoPoint getStart() {
		return start;
	}

	public void setStart(GeoPoint start) {
		this.start = start;
	}

	public GeoPoint getEnd() {
		return end;
	}

	public void setEnd(GeoPoint end) {
		this.end = end;
	}
	
	
}
