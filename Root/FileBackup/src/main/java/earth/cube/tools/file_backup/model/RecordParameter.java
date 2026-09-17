package earth.cube.tools.file_backup.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.commons.EscapeUtil;
import earth.cube.tools.file_backup.commons.Query;

public class RecordParameter {
	
	protected Logger _log = LogManager.getLogger(getClass());
	
	private Scope _scope;
	
	
	public RecordParameter(Scope scope) throws IOException, SQLException {
		_scope = scope;
	}
	
	public void save(String sName, String sValue) throws SQLException, IOException {
		Query.perform(_scope.getConnection(), q -> q.query("INSERT OR REPLACE INTO PARAMETER (NAME, VALUE) VALUES ('%s', '%s')", EscapeUtil.escapeSqlString(sName), EscapeUtil.escapeSqlString(sValue)).execute());
	}

	public String load(String sName) throws SQLException, IOException {
		return Query.perform(_scope.getConnection(), q -> q.query("SELECT VALUE FROM PARAMETER WHERE NAME='%s'", EscapeUtil.escapeSqlString(sName)).execute().asString("VALUE"));
	}

	public static void ensureTable(Connection conn) throws IOException, SQLException {
	    if(!Query.existsTable(conn, "PARAMETER")) {
	    	String sSql = "CREATE TABLE PARAMETER " +
	                   "(NAME         TEXT    PRIMARY KEY    NOT NULL,"
	                 + " VALUE        TEXT                   NOT NULL)";
		    Query.perform(conn, q -> q.query(sSql).execute());
	   }	
	}

}
