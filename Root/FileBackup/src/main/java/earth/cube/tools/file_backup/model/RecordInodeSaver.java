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
public class RecordInodeSaver {
	
	public static RecordInodeSaver create(Connection conn) {
		return new RecordInodeSaver().connection(conn);
	}

	@Setter
	private Connection _connection;

	@Setter
	private long _nId;

	@Setter
	private Date _dChangedTime;

	@Setter
	private long _nSize;

	@Setter
	private String _sExtension;

	@Setter
	private String _sSha256;

	@Setter
	private Date _sha256Updated;

	@Setter
	private Date _dUpdatedTime;

	@Setter
	private int _nBatchId;

	
	public void save() throws IOException, SQLException {
		Query.perform(_connection, q -> q.query("INSERT OR REPLACE INTO INODE (ID, CTIME, SIZE, EXTENSION, SHA256, UPDATED, BATCH_ID) VALUES(%s, %s, %s, '%s', '%s', %s, %s)", _nId, Query.getJulianDate(_dChangedTime), _nSize, EscapeUtil.escapeSqlString(_sExtension), _sSha256, Query.getJulianDate(_dUpdatedTime), _nBatchId).execute());
	}
	
}
