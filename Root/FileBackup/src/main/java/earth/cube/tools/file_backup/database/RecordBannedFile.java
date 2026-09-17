package earth.cube.tools.file_backup.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.commons.EscapeUtil;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.commons.Sha256Util;
import earth.cube.tools.file_backup.files.common.IContentKey;

public class RecordBannedFile {
	
	protected Logger _log = LogManager.getLogger(getClass());
	
	private Scope _scope;
	private File _file;
	private String _sRemark;


	public RecordBannedFile(Scope scope, File file, String sRemark) {
		_scope = scope;
		_file = file;
		_sRemark = sRemark;
	}
	
	
	public void save() throws IOException, SQLException {
		String sSha256 = Sha256Util.getChecksum(_file);
		_scope.getStatistics().incrementCheckedFiles();
		
		String sSql = "INSERT INTO BANNED_FILE (PATH, MODIFIED, SIZE, DELETED, UPDATED, EXTENSION, SHA256, REMARK, BATCH_ID) VALUES('%s', %s, %s, %s, %s, '%s', '%s', '%s', %s)";
		Query.perform(_scope.getConnection(), q -> q.query(sSql, 
				EscapeUtil.escapeSqlString(_file.getAbsolutePath()), 
				Query.getJulianDate(_file.lastModified()),
				_file.length(),
				Query.getJulianDate(new Date()),
				Query.getJulianDate(new Date()),
				EscapeUtil.escapeSqlString(FileUtil.getExtension(_file)),
				sSha256,
				EscapeUtil.escapeSqlString(_sRemark == null ? "" : _sRemark),
				_scope.getBatchId()).execute());
	}
	
	
	public static boolean isBanned(Connection conn, IContentKey cnt) throws IOException, SQLException {
		return Query.perform(conn, query -> query.query("select count(*) as cnt from banned_file where sha256='%s' and size=%s and extension='%s'", 
				EscapeUtil.escapeSqlString(cnt.getSha256()),
				cnt.getSize(),
				EscapeUtil.escapeSqlString(cnt.getExtension())).execute().asInt("cnt")) == 1;
	}
	
	public static void ensureTable(Connection conn) throws IOException, SQLException {
	    if(!Query.existsTable(conn, "BANNED_FILE")) {
	    	String sSql = "CREATE TABLE BANNED_FILE " +
	                 "(PATH         TEXT                   NOT NULL," +
	                 " MODIFIED     REAL                   NOT NULL," +
	                 " SIZE         LONG                   NOT NULL," +
	                 " DELETED      REAL                   NOT NULL," +
	    			 " UPDATED      REAL                   NOT NULL," +
	                 " EXTENSION    TEXT                   NOT NULL," +
	                 " SHA256       VARCHAR(64)            NOT NULL," +
	                 " REMARK       TEXT                   NOT NULL," +
	                 " BATCH_ID     INT                    NOT NULL)";
		    Query.perform(conn, q -> q.query(sSql).execute());
	    }
	    
	    final String sSql = "CREATE INDEX IF NOT EXISTS IDX_BANNED_FILE_KEY ON BANNED_FILE (SHA256, SIZE, EXTENSION)";
	    Query.perform(conn, q -> q.query(sSql).execute());

	}
	
}
