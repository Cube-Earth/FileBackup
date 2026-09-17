package earth.cube.tools.file_backup.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.commons.ValidationException;
import earth.cube.tools.file_backup.files.IFacet;
import earth.cube.tools.file_backup.files.IFacetedFile;
import earth.cube.tools.file_backup.files.annotations.FacetInterface;
import earth.cube.tools.file_backup.files.common.ContentKey;
import earth.cube.tools.file_backup.files.common.InodeContent;
import earth.cube.tools.file_backup.files.common.InodeKey;
import earth.cube.tools.file_backup.files.facets.api.ILinuxFile;
import earth.cube.tools.file_backup.model.RecordInodeSaver;
import lombok.Getter;

@FacetInterface(dependsOn = { ILinuxFile.class })
public class RecordInode implements IFacet {
	
	protected Logger _log = LogManager.getLogger();
	
	private Connection _conn;
	private boolean _bNew;
	
	@Getter
	private IFacetedFile _host;

	@Getter
	private boolean _bUpdateNeeded;

	
	public RecordInode(IFacetedFile file) throws SQLException, IOException {
		_host = file;
		_conn = file.getPath().getFileSystem().getDatabase().getConnection();
	}
	
	protected void load() throws IOException, SQLException {
		ILinuxFile file = _host.getFacet(ILinuxFile.class);
		_bNew = true;
		_bUpdateNeeded = true;
		long nId = file.getInodeId();
		Query.perform(_conn, q -> q.query("SELECT ID, CTIME, SIZE, EXTENSION, SHA256, SHA256_UPDATED, UPDATED FROM INODE WHERE ID='%s'", nId).execute().process ( rs -> {
			_bUpdateNeeded = false;
			Date dChangedTime = rs.getTimestamp("CTIME");
			long nSize = rs.getLong("SIZE");
			String sExtension = rs.getString("EXTENSION");
			String sSha256 = rs.getString("SHA256");
			
			if(file.getInodeChangedTime().equals(dChangedTime)) {
				if(file.getSize() != nSize || !file.getExtension().equals(sExtension))
					throw new ValidationException("File '" + file.getFile().getAbsolutePath() + "' and database entry for inode " + nId + " do not match!");

				InodeContent cnt = new InodeContent(
						new InodeKey(nId, dChangedTime), 
						new ContentKey(sSha256, nSize, sExtension));
				file.setSha256(cnt);
			}
			else
				_bUpdateNeeded = true;
			
			_bNew = false;
			_log.debug("load: found inode " + nId);
		}));
	
	}
	
	public void save() throws IOException, SQLException {
		_log.debug("save: update needed = " + _bUpdateNeeded);
		if(!_bUpdateNeeded)
			return;
		
//		Query.perform(_conn, q -> q.query("INSERT OR REPLACE INTO INODE (ID, CTIME, SIZE, EXTENSION, SHA256, SHA256_UPDATED, UPDATED, BATCH_ID) VALUES(%s, %s, %s, '%s', '%s', %s, %s, %s)", _nId, Query.getJulianDate(_changedTime), _nSize, EscapeUtil.escapeSqlString(_sExtension), _sSha256, Query.getJulianDate(_sha256Updated), Query.getJulianDate(_updatedTime), _scope.getBatchId()).execute());

		ILinuxFile file = _host.getFacet(ILinuxFile.class);
		if(file.getSha256() == null || file.getSha256().length() == 0)
			throw new ValidationException("SHA256 not set!");
		
		int nBatchId = _host.getPath().getFileSystem().getDatabase().getBatchId();
		RecordInodeSaver.create(_conn).id(file.getInodeId()).size(file.getSize()).extension(file.getExtension()).changedTime(file.getInodeChangedTime()).updatedTime(new Date()).sha256(file.getSha256()).batchId(nBatchId).save();
		_bUpdateNeeded = false;
	}	

	public static void ensureTable(Connection conn) throws IOException, SQLException {
	    if(!Query.existsTable(conn, "INODE")) {
	    	String sSql = "CREATE TABLE INODE " +
                 "(ID               LONG      PRIMARY KEY    NOT NULL," +
                 " CTIME            REAL                     NOT NULL," + 
                 " SIZE             LONG                     NOT NULL," +
                 " EXTENSION        TEXT                     NOT NULL," +
                 " SHA256           VARCHAR(64)              NOT NULL," +
 //                " SHA256_UPDATED   REAL                     NOT NULL," +
                 " UPDATED          REAL                     NOT NULL," +
                 " BATCH_ID         INT                      NOT NULL)";
                 Query.perform(conn, q -> q.query(sSql).execute());
	    }
	    
	    final String sSql = "CREATE INDEX IF NOT EXISTS IDX_INODE_KEY ON INODE (SHA256, SIZE, EXTENSION)";
	    Query.perform(conn, q -> q.query(sSql).execute());
	    
	}

	@Override
	public boolean isValid() {
		return true;
	}


}
