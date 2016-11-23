package sk.fiit.pdt.tomaas.services;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sk.fiit.pdt.tomaas.entities.GeoPoint;
import sk.fiit.pdt.tomaas.entities.GeoRoute;
import sk.fiit.pdt.tomaas.entities.GeoRouteSegment;

/**
 * Service for integration with osm schema - open street map data
 * @author Tomas
 *
 */
public class OsmService {

	private static OsmService instance = null;
	private Connection connectionOsm = null;

	private OsmService() {
		try {
			Class.forName("org.postgresql.Driver");
			connectionOsm = DriverManager.getConnection("jdbc:postgresql://localhost:5432/gis?currentSchema=public",
					"postgres", "postgres");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static OsmService getInstance() {
		if (instance == null) {
			instance = new OsmService();
		}
		return instance;
	}

	// ---------------------------------------------------------

	public float getPointDistance(Long id1, Long id2) {
		float output = 0;

		try {
			CallableStatement stmt = connectionOsm.prepareCall("{? = call public.getpointdistance(?, ?)}");

			stmt.registerOutParameter(1, Types.REAL);
			stmt.setLong(2, id1);
			stmt.setLong(3, id2);
			stmt.execute();
			output = stmt.getFloat(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	public GeoPoint getNearestPoint(float lat, float lng) {
		ResultSet output;
		try {
			Statement stmt = connectionOsm.createStatement();
			output = stmt
					.executeQuery("select * from getnearestpoint(" + lat + ", " + lng + ") f(col1 bigint, col2 text);");

			while (output.next()) {
				GeoPoint point = GeoPoint.getPointFromStText(output.getString(2));
				point.setID(output.getLong(1));

				return point;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public GeoRoute getRoute(Long ID1, Long ID2) {
		ResultSet output;
		SrtmService srtm = SrtmService.getInstance();

		GeoRoute result = new GeoRoute();

		try {
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			System.out.println("Started routing: "+sdf.format(new Date()));
						
			Statement stmt = connectionOsm.createStatement();
			output = stmt.executeQuery("select * from getroute(" + ID1 + ", " + ID2 + ");");
			
			String[] pointsAsString;
			List<GeoPoint> points = new ArrayList<>();
			while (output.next()) {
				Array a = (output.getArray(1));
				if (a != null) {
					pointsAsString = (String[]) a.getArray();
					for (String point : pointsAsString) {
						points.add(GeoPoint.getPointFromStText_full(point));
					}
				}
			}
			System.out.println("Finished routing, started post-proc: "+sdf.format(new Date()));

			List<Long> ids = new ArrayList<>();
			for (int i = 0; i < points.size(); i++) {
				ids.add(points.get(i).getID());
			}
			Float[] alts = srtm.getAltitudes(ids.toArray());
			
			
			for (int i = 0; i < points.size() - 1; i++) {
				points.get(i).setEle(alts[i]);
				points.get(i + 1).setEle(alts[i+1]);
				result.addSegment(new GeoRouteSegment(points.get(i), points.get(i + 1)));
			}
			System.out.println("Finished post-proc, started visualization: "+sdf.format(new Date()));
			
			return result;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
