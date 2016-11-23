package sk.fiit.pdt.tomaas.entities;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.json.JSONArray;
import org.apache.wicket.ajax.json.JSONObject;

/**
 * Entity for describing a route - list of routeSegment-s
 * @author Tomas
 *
 */
public class GeoRoute {

	List<GeoRouteSegment> segments = new ArrayList<>();
	
	public GeoRoute(){
		super();
	}	
	
	public GeoRoute(List<GeoRouteSegment> segments){
		super();
		this.segments = segments;
	}
	
	public void addSegment(GeoRouteSegment s){
		segments.add(s);
	}
	
	public List<GeoRouteSegment> getSegments(){
		return this.segments;
	}
	
	public double getTotalLength(){
		double total = 0;
		for (GeoRouteSegment segment : segments) {
			total+=segment.getLength();
		}
		return total;
	}
	
	public double getTotalAscends(){
		double total = 0;
		for (GeoRouteSegment segment : segments) {
			double dEle = segment.getDEle();
			if(dEle<0){
				total-=dEle;
			}
		}
		return total;
	}
	
	public double getTotalDescends(){
		double total = 0;
		for (GeoRouteSegment segment : segments) {
			double dEle = segment.getDEle();
			if(dEle>0){
				total+=dEle;
			}
		}
		return total;
	}
	
	public double getTop(){
		double max = 0;
		for (GeoRouteSegment grs : segments) {
			if(grs.end.ele>max){
				max = grs.end.ele;
			}
			if(grs.start.ele>max){
				max = grs.start.ele;
			}
		}
		return max;
	}
	
	public double getBottom(){
		double min = Double.POSITIVE_INFINITY;
		for (GeoRouteSegment grs : segments) {
			if(grs.end.ele<min){
				min = grs.end.ele;
			}
			if(grs.start.ele<min){
				min = grs.start.ele;
			}
		}
		return min;
	}
	
	
	
	@Override
	public String toString(){
		JSONObject me = new JSONObject();		
		JSONArray lns_g = new JSONArray();
		JSONArray lns_b = new JSONArray();
		JSONArray lns_o = new JSONArray();
		JSONArray lns_r = new JSONArray();
		
		for (GeoRouteSegment segment : segments) {
			double incline = segment.getIncline();
			JSONArray line = new JSONArray();
			line.put(new double[]{segment.start.getLng(), segment.start.getLat()});
			line.put(new double[]{segment.end.getLng(), segment.end.getLat()});
			if(incline<0.02){
				lns_g.put(line);
			}else if(incline<0.07){
				lns_b.put(line);
			}else if(incline<0.17){
				lns_o.put(line);
			}else{
				lns_r.put(line);
			}
		}
		
		me.put("coordinates_g", lns_g);
		me.put("coordinates_b", lns_b);
		me.put("coordinates_o", lns_o);
		me.put("coordinates_r", lns_r);
		return me.toString();
	}	

}
