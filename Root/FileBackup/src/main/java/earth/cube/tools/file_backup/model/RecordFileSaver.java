package earth.cube.tools.file_backup.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import earth.cube.tools.file_backup.commons.EscapeUtil;
import earth.cube.tools.file_backup.commons.Query;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class RecordFileSaver {
	
	public static RecordFileSaver create(Connection conn) {
		return new RecordFileSaver().connection(conn);
	}
	
	@Setter
	private Connection _connection;

	@Setter
	private String _sPath;

	@Setter
	private long _nInodeId;

	@Setter
	private Date _dLastModified;

	@Setter
	private boolean _bValid = true;
	
	@Setter
	private Date _dUpdatedTime;

	@Setter
	private int _nBatchId;

	public void save() throws IOException, SQLException {
		Query.perform(_connection, q -> q.query("INSERT OR REPLACE INTO FILE (PATH, INODE, MODIFIED, VALID, UPDATED, BATCH_ID) VALUES('%s', %s, %s, %s, %s, %s)", EscapeUtil.escapeSqlString(_sPath), _nInodeId, Query.getJulianDate(_dLastModified), _bValid ? 1 : 0, Query.getJulianDate(_dUpdatedTime), _nBatchId).execute());
	}

}
