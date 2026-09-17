package earth.cube.tools.file_backup.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import earth.cube.tools.file_backup.commons.Query;

public class RecordSequence {
	
	protected Logger _log = LogManager.getLogger(getClass());
	
	private Connection _conn;
	private String _sName;
	private int _nCounter;
	
	
	public RecordSequence(Connection conn, String sName) throws IOException, SQLException {
		_conn = conn;
		_sName = sName;
	}
	
	public int acquireNext() throws SQLException, IOException {
		Query.perform(_conn, q -> q.query("BEGIN EXCLUSIVE TRANSACTION").execute());
		Query.perform(_conn, q -> q.query("INSERT OR REPLACE INTO sequence (NAME, COUNTER) VALUES ('%s', COALESCE((SELECT counter FROM sequence WHERE name = '%1$s'), 0) + 1)", _sName).execute());
		_nCounter = Query.perform(_conn, q -> q.query("SELECT counter from sequence where name='" + _sName + "'").execute().asInt("counter"));
		Query.perform(_conn, q -> q.query("COMMIT").execute());
		return _nCounter;
	}

	public static void ensureTable(Connection conn) throws IOException, SQLException {
	    if(!Query.existsTable(conn, "SEQUENCE")) {
	    	String sSql = "CREATE TABLE SEQUENCE " +
	                   "(NAME         TEXT    PRIMARY KEY    NOT NULL,"
	                 + " COUNTER      INT                    NOT NULL)";
		    Query.perform(conn, q -> q.query(sSql).execute());
	   }	
	}

}
