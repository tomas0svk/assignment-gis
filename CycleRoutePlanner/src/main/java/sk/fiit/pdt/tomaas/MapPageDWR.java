package sk.fiit.pdt.tomaas;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.wicket.ajax.json.JSONObject;

import sk.fiit.pdt.tomaas.entities.GeoPoint;
import sk.fiit.pdt.tomaas.entities.GeoRoute;
import sk.fiit.pdt.tomaas.entities.GeoRouteSegment;
import sk.fiit.pdt.tomaas.services.OsmService;

public class MapPageDWR {

	//TODO reimplement service using wicket stateful beans
	
	private static Map<String, List<GeoRoute>> allRoutes = new HashMap<>();
	private static MapPageDWR instance = null;

	private MapPageDWR() {
		// TODO remove from map after time elapses
	}

	public static MapPageDWR getInstance() {
		if (instance == null) {
			instance = new MapPageDWR();
		}
		return instance;
	}

	public float getPointDistance(Long id1, Long id2) {
		OsmService osm = OsmService.getInstance();
		return osm.getPointDistance(id1, id2);
	}

	public String getNearestPoint(String p) {
		OsmService osm = OsmService.getInstance();

		JSONObject params = new JSONObject(p);
		float lat = (float) params.getDouble("lat");
		float lng = (float) params.getDouble("lng");

		GeoPoint nearest = osm.getNearestPoint(lat, lng);

		return nearest.toString();
	}

	public String getRoute(String p) {
		OsmService osm = OsmService.getInstance();

		JSONObject params = new JSONObject(p);
		long ID1 = params.getLong("ID1");
		long ID2 = params.getLong("ID2");
		int index = params.getInt("inx");
		List<GeoRoute> rt = allRoutes.get(params.getString("sessionID"));
		if (rt == null) {
			rt = new ArrayList<>();
			allRoutes.put(params.getString("sessionID"), rt);
		}

		GeoRoute result = osm.getRoute(ID1, ID2);
		if (index == rt.size()) {
			rt.add(result);
		} else {
			rt.set(index, result);
		}
		return result.toString();
	}
	
	public void removeRoutes(String p){
		System.out.println(p);
		JSONObject params = new JSONObject(p);
		int n = params.getInt("n");	
		List<GeoRoute> rt = allRoutes.get(params.getString("sessionID"));
		for(int i=0;i<n;i++){
			rt.remove(rt.size() - 1);
		}
	}

	public String getStatistics(String p) {
		System.out.println(p);
		JSONObject params = new JSONObject(p);

		double totLen = 0;
		double totAsc = 0;
		double totDesc = 0;
		double eleDiff;
		double avgIncl;

		List<GeoRoute> rts = allRoutes.get(params.getString("sessionID"));
		if (rts == null) {
			return "{}";
		}

		for (GeoRoute route : rts) {
			totLen += route.getTotalLength();
			totAsc += route.getTotalAscends();
			totDesc += route.getTotalDescends();
		}
		eleDiff = totAsc - totDesc;
		avgIncl = (totAsc + totDesc) / totLen * 100;

		JSONObject result = new JSONObject();
		result.put("totLen", totLen);
		result.put("totAsc", totAsc);
		result.put("totDesc", totDesc);
		result.put("eleDiff", eleDiff);
		result.put("avgIncl", avgIncl);
		String b64Img = "";
		
		try {
			b64Img = foo(params.getString("sessionID"), rts);
		} catch (IOException e) {
			e.printStackTrace();
		}
		result.put("img", b64Img);

		
		return result.toString();
	}
	
	private String foo(String id, List<GeoRoute> rts) throws IOException{
		BufferedImage statImage = new BufferedImage(600, 300, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = statImage.createGraphics();

		double totLen = 0;
		double totAsc = 0;
		double totDesc = 0;
		double eleDiff;
		double avgIncl;

		for (GeoRoute route : rts) {
			totLen += route.getTotalLength();
			totAsc += route.getTotalAscends();
			totDesc += route.getTotalDescends();
		}
		eleDiff = totAsc - totDesc;
		avgIncl = (totAsc + totDesc) / totLen * 100;
		
		g.setFont(new Font("Tahoma", Font.PLAIN, 26));

		int totDiffc = (int) (totLen + totAsc*100);
		if(totDiffc < 50000){
			g.setColor(new Color(0, 128, 0));
			g.drawString("EASY", 10, 60);

		}
		else if(totDiffc < 100000){
			g.setColor(new Color(206, 128, 0));
			g.drawString("MEDIUM", 10, 60);

		}else{
			g.setColor(new Color(128, 0, 0));
			g.drawString("HARD", 10, 60);
		}
		
		//characteristics
		g.setColor(Color.BLACK);
		g.setFont(new Font("Tahoma", Font.PLAIN, 12));
		g.drawString("Overall difficulty:", 10, 20);

		g.drawString("Total Length:", 300, 20);
		g.drawString("Total Ascends:", 300, 40);
		g.drawString("Total Descends:", 300, 60);
		g.drawString("Elevation Difference:", 300, 80);
		g.drawString("Average Incline:", 300, 100);
		
		NumberFormat formatter = new DecimalFormat("#0.00");     
		g.setFont(new Font("Tahoma", Font.BOLD, 12));
		g.drawString(formatter.format(totLen)+"m", 430, 20);
		g.drawString(formatter.format(totAsc)+"m", 430, 40);
		g.drawString(formatter.format(totDesc)+"m", 430, 60);
		g.drawString(formatter.format(eleDiff)+"m", 430, 80);
		g.drawString(formatter.format(avgIncl)+"%", 430, 100);
		
		double xScale = 500/totLen;
		double eleMax = 0;
		double eleMin = Double.POSITIVE_INFINITY;
		for (GeoRoute rt : rts) {
			if(rt.getTop()>eleMax){
				eleMax = rt.getTop();
			}
			if(rt.getBottom()<eleMin){
				eleMin = rt.getBottom();
			}
		}
		double yScale = 100/(eleMax-eleMin);
		
		double curM = 0;
		
		for (GeoRoute rt : rts) {
			for (GeoRouteSegment seg : rt.getSegments()) {
				int x1=(int) (curM * xScale);
				curM += seg.getLength();
				int y1 = (int) ((seg.getStart().getEle() - eleMin) * yScale);
				
				int x2=(int) (curM * xScale);
				int y2 = (int) ((seg.getEnd().getEle() - eleMin) * yScale);
				
				g.drawLine(50+x1, 250 - y1, 50 + x2, 250 - y2);
			}
		}
		
		//chart bars
		g.drawString("0km", 60, 270);
		g.drawString(formatter.format(totLen/1000)+"km", 490, 270);		
		g.drawString(formatter.format(eleMax)+"m", 10, 120);
		g.drawString(formatter.format(eleMin)+"m", 10, 290);
		
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(40, 250, 560, 250);
		g.drawLine(50, 140, 50, 260);
		g.drawLine(550, 140, 550, 260);

	
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(statImage, "png", Base64.getEncoder().wrap(os));
		String result = os.toString("UTF-8");
		
		System.out.println(result);
		return result;
		
	}

}
