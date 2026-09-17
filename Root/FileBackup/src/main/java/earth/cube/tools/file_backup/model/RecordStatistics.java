package earth.cube.tools.file_backup.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.commons.Sha256Util;

public class RecordStatistics {
	
	protected Logger _log = LogManager.getLogger(getClass());

	private Scope _scope;

	private Date _startTime = new Date();

	private int _nCheckedFiles;
	private int _nNewFiles;
	private int _nUpdatedFiles;
	private int _nUnchangedFiles;

	private int _nCheckedInodes;
	private int _nNewInodes;
	private int _nUpdatedInodes;
	private int _nUnchangedInodes;
	

	
	public RecordStatistics(Scope scope) throws IOException, SQLException {
		_scope = scope;
		Sha256Util._nCalculatedChecksumBytes = 0;
	}

	
	public void incrementCheckedFiles() {
		_nCheckedFiles++;
	}

	public void incrementNewFiles() {
		_nNewFiles++;
	}

	public void incrementUpdatedFiles() {
		_nUpdatedFiles++;
	}

	public void incrementUnchangedFiles() {
		_nUnchangedFiles++;
	}

	public void incrementCheckedInodes() {
		_nCheckedInodes++;
	}

	public void incrementNewInodes() {
		_nNewInodes++;
	}

	public void incrementUpdatedInodes() {
		_nUpdatedInodes++;
	}

	public void incrementUnchangedInodes() {
		_nUnchangedInodes++;
	}
	

	
	public void save() {
		_log.debug("save: entered");
	
		try {
			Query.perform(_scope.getConnection(), q -> q.query("INSERT INTO STATISTICS (BATCH_ID, FILES_CHECKED, FILES_NEW, FILES_UPDATED, FILES_UNCHANGED, INODES_CHECKED, INODES_NEW, INODES_UPDATED, INODES_UNCHANGED, BYTES_CALCULATED, STARTED, COMPLETED) VALUES(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)", 
				_scope.getBatchId(),
				_nCheckedFiles, _nNewFiles, _nUpdatedFiles, _nUnchangedFiles,
				_nCheckedInodes, _nNewInodes, _nUpdatedInodes, _nUnchangedInodes,
				Sha256Util._nCalculatedChecksumBytes,
				Query.getJulianDate(_startTime), Query.getJulianDate(new Date())).execute());
		} catch (IOException | SQLException e) {
			_log.error("save: Exception occurred -->", e);
			throw new RuntimeException(e);
		}	
	}
	

	public static void ensureTable(Connection conn) throws IOException, SQLException {
	    if(!Query.existsTable(conn, "STATISTICS")) {
	    	String sSql = "CREATE TABLE STATISTICS " +
	                 "(BATCH_ID          INT     PRIMARY KEY    NOT NULL," +
	                 " FILES_CHECKED     INT                    NOT NULL," +
	                 " FILES_NEW         INT                    NOT NULL," +
	                 " FILES_UPDATED     INT                    NOT NULL," +
	                 " FILES_UNCHANGED   INT                    NOT NULL," +
	                 " INODES_CHECKED    INT                    NOT NULL," +
	                 " INODES_NEW        INT                    NOT NULL," +
	                 " INODES_UPDATED    INT                    NOT NULL," +
	                 " INODES_UNCHANGED  INT                    NOT NULL," +
	                 " BYTES_CALCULATED  LONG                   NOT NULL," +
	                 " STARTED           REAL                   NOT NULL," +
	                 " COMPLETED         REAL                   NOT NULL)";
		    Query.perform(conn, q -> q.query(sSql).execute());
	    }	
	}



}
