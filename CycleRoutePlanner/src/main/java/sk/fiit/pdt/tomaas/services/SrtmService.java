package sk.fiit.pdt.tomaas.services;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;

import org.apache.wicket.ajax.json.JSONObject;

import sk.fiit.pdt.tomaas.entities.GeoPoint;

/**
 * Service for integration wiith osm & srtm schema regarding to point altitude
 * @author Tomas
 *
 */
public class SrtmService {

	private static SrtmService instance = null;
	private Connection connectionSrtm = null;
	private Connection connectionOsm = null;

	
	private SrtmService(){
		try{
		Class.forName("org.postgresql.Driver");
		connectionSrtm = DriverManager.getConnection("jdbc:postgresql://localhost:5432/gis?currentSchema=srtm",
				"postgres", "postgres");
		}catch(Throwable t){
			t.printStackTrace();
		}
		try {
			Class.forName("org.postgresql.Driver");
			connectionOsm = DriverManager.getConnection("jdbc:postgresql://localhost:5432/gis?currentSchema=public",
					"postgres", "postgres");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public static SrtmService getInstance(){
		if(instance == null){
			instance = new SrtmService();
		}
		return instance;
	}
	
	//-----------------------------------------------------------
	
	public float getAltitude(String lngLat) {
		float output = 0;
		JSONObject obj = new JSONObject(lngLat);
		float lat = (float) obj.getDouble("lat");
		float lng = (float) obj.getDouble("lng");

		try {
			CallableStatement stmt = connectionSrtm.prepareCall("{? = call srtm.getaltitude(?, ?)}");

			stmt.registerOutParameter(1, Types.REAL);
			stmt.setFloat(2, lat);
			stmt.setFloat(3, lng);
			stmt.execute();
			output = stmt.getFloat(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	public float getAltitude(GeoPoint p) {
		float output = 0;

		try {
			CallableStatement stmt = connectionSrtm.prepareCall("{? = call srtm.getaltitude(?, ?)}");

			stmt.registerOutParameter(1, Types.REAL);
			stmt.setFloat(2, (float) p.getLat());
			stmt.setFloat(3, (float) p.getLng());
			stmt.execute();
			output = stmt.getFloat(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
	
	/*public Float[] getAltitudes(Object[] p) {
		Float output[] = null;

		try {
			CallableStatement stmt = connectionOsm.prepareCall("{? = call getaltitudes(?)}");

			stmt.registerOutParameter(1, Types.ARRAY);
			stmt.setArray(2, connectionOsm.createArrayOf("BIGINT", p));
			stmt.execute();
			output = (Float[]) stmt.getArray(1).getArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}*/
	
	public Float[] getAltitudes(Object[] p) {
		ResultSet output = null;
		Float[] alts = null;
		
		try {
			Statement stmt = connectionOsm.createStatement();
			String query = "select * from getaltitudes(Array"+Arrays.toString(p)+");";
			output = stmt.executeQuery(query);

			while (output.next()) {
				Array a = (output.getArray(1));
				if (a != null) {
					alts = (Float[]) a.getArray();					
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return alts;
	}
}
