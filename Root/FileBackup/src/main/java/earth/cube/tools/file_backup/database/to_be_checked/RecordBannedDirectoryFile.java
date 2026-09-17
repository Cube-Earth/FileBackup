package earth.cube.tools.file_backup.database.to_be_checked;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.Scope.ActionContext;
import earth.cube.tools.file_backup.commons.CloseableAction;
import earth.cube.tools.file_backup.commons.EscapeUtil;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.commons.StringUtil;
import earth.cube.tools.file_backup.commons.XattrFile;
import earth.cube.tools.file_backup.files.common.IContentKey;
import earth.cube.tools.file_backup.filesystem.CompoundFile;
import earth.cube.tools.file_backup.filesystem.features.IFileInformation;
import earth.cube.tools.file_backup.model.LinuxInode;
import earth.cube.tools.file_backup.model.RecordFile;
import lombok.Getter;
import lombok.Setter;

public class RecordBannedDirectoryFile implements IFileInformation {
	
	protected Logger _log = LogManager.getLogger(getClass());
	
	private Scope _scope;
	
	private BannedDirectory _parent;

	private CompoundFile _file;
	
	@Getter
	private long _nId;
	
	@Getter @Setter
	private boolean _bTrigger;

	@Getter
	private String _sSha256;
	
	@Getter @Setter
	private String _sRemark;

	@Getter @Setter
	private String _sRelativePath;

	@Getter
	private long _nSize;

	@Getter
	private String _sExtension;
	
	protected boolean _bNew = true;
	
	@Getter
	protected boolean _bValid = true;

	@Getter
	private String _sPath;

	@Getter
	private Date _dLastModified;




	public RecordBannedDirectoryFile(Scope scope, ResultSet rs) throws SQLException {
		_scope = scope;
		load(rs);
		
	}
	
	public RecordBannedDirectoryFile(Scope scope, BannedDirectory parent, CompoundFile file) throws SQLException, IOException {
		_scope = scope;
		_parent = parent;
		load(file);
		
	}


	public RecordBannedDirectoryFile load(CompoundFile file) throws IOException {
		_bValid = file.isValid();
		if(!_bValid)
			return this;
		
		_scope.getStatistics().incrementCheckedFiles();
		_file = file;
		_sSha256 = _file.getSha256();
		_sPath = _file.getFile().getAbsolutePath();
		_sRelativePath = FileUtil.getRelativePath(_parent.getBaseDirectory(), _file.getFile().getAbsoluteFile());
		_dLastModified = _file.getLinuxFile().getLastModified();
		_nSize = _file.getLinuxFile().getSize();
		_sExtension = file.getGenericFile().getExtension();
		_bTrigger = CloseableAction.execute(XattrFile.getSidecar(file.getFile()), x -> x.isValid() && x.hasLoweredTag("DeleteTrigger", "Delete_Trigger"), false);
		return this;
	}
	
	public void load(ResultSet rs) throws SQLException {
		if(_parent != null && rs.getLong("id") != _parent.getId())
			throw new IllegalStateException();
		
		_bNew = false;
		_nId = rs.getLong("id");
		_sPath = rs.getString("path");
		_sRelativePath = rs.getString("rel_path");
		_dLastModified = rs.getTimestamp("modified");
		_bTrigger = rs.getBoolean("trigger");
		_nSize = rs.getLong("size");
		_sSha256 = rs.getString("SHA256");
		_sExtension = rs.getString("extension");
		_sRemark = rs.getString("remark");
	}

	public String getKey() {
		return LinuxInode.getKey(_sSha256, _sExtension, _nSize);
	}
	
	
	public void save() throws IOException, SQLException {
		if(!_bNew || _file == null)
			throw new IllegalStateException();
		
		String sSql = "INSERT OR REPLACE INTO BANNED_DIRECTORY_FILE (ID, PATH, REL_PATH, TRIGGER, MODIFIED, SIZE, DELETED, UPDATED, EXTENSION, SHA256, REMARK, BATCH_ID) VALUES(%s, '%s', '%s', %s, %s, %s, %s, %s, '%s', '%s', '%s', %s)";
		Query.perform(_scope.getConnection(), q -> q.query(sSql,
				_parent.getId(),
				EscapeUtil.escapeSqlString(_sPath), 
				EscapeUtil.escapeSqlString(_sRelativePath), 
				_bTrigger,
				Query.getJulianDate(_dLastModified), 
				_nSize, 
				Query.getJulianDate(new Date()),
				Query.getJulianDate(new Date()),
				EscapeUtil.escapeSqlString(_sExtension),
				_sSha256, 
				EscapeUtil.escapeSqlString(_sRemark == null ? "" : _sRemark),
				_scope.getBatchId()).execute());
	}
	
	public RecordFile getFile() throws IOException, SQLException {
		if(_file == null) {
			_file = new CompoundFile(_scope.fi , new File(_parent.getBaseDirectory(), _sRelativePath));
		}
		return _file;
	}
	

	public boolean isMatching() throws IOException, SQLException {
		if(_bNew) {
			// self-fulfilling prophecy
			// if _bNew is true, the file properties would be compared against itself and not against a database entry
			throw new IllegalStateException();
		}
		
		RecordFile file = getFile();
		if(!file.isValid())
			return false;
		
		LinuxInode inode = _file.getLinuxFile().getInode();
		boolean bMatches = _sSha256.equals(file.getCalculatedSha256FromInode());
		bMatches &= _nSize == inode.getSize();
		bMatches &= _sExtension.equals(inode.getExtension());
		return bMatches;
	}
	
	
	public static void ensureTable(Connection conn) throws IOException, SQLException {
	    if(!Query.existsTable(conn, "BANNED_DIRECTORY_FILE")) {
	    	String sSql = "CREATE TABLE BANNED_DIRECTORY_FILE " +
	                 "(ID           LONG                   NOT NULL," +
	                 " PATH         TEXT                   NOT NULL," +
	                 " REL_PATH     TEXT                   NOT NULL," +
	                 " TRIGGER      TINYINT                NOT NULL," +
	                 " MODIFIED     REAL                   NOT NULL," +
	                 " SIZE         LONG                   NOT NULL," +
	                 " DELETED      REAL                   NOT NULL," +
	    			 " UPDATED      REAL                   NOT NULL," +
	                 " EXTENSION    TEXT                   NOT NULL," +
	                 " SHA256       VARCHAR(64)            NOT NULL," +
	                 " REMARK       TEXT                   NOT NULL," +
	                 " BATCH_ID     INT                    NOT NULL," +
	                 " PRIMARY KEY (ID, REL_PATH))";
		    Query.perform(conn, q -> q.query(sSql).execute());
	    }	
	    
	    final String sSql1 = "CREATE INDEX IF NOT EXISTS IDX_BANNED_DIRECTORY_FILE_KEY ON BANNED_DIRECTORY_FILE (SHA256, SIZE, EXTENSION, TRIGGER)";
	    Query.perform(conn, q -> q.query(sSql1).execute());

	    final String sSql2 = "CREATE INDEX IF NOT EXISTS IDX_BANNED_DIRECTORY_FILE_ID ON BANNED_DIRECTORY_FILE (ID)";
	    Query.perform(conn, q -> q.query(sSql2).execute());
	}
	

	public static Collection<BannedDirectory> getBannedDirectories(Scope scope, IContentKey info) throws IOException, SQLException {
		Map<Long,BannedDirectory> dirs = new HashMap<>();
		
		Query.perform(scope.getConnection(), query -> query.query("select * from banned_directory_file where sha256='%s' and size=%s and extension='%s' and trigger=1", 
				EscapeUtil.escapeSqlString(info.getSha256()),
				info.getSize(),
				EscapeUtil.escapeSqlString(info.getExtension())).execute().process( rs -> {
					RecordBannedDirectoryFile file = new RecordBannedDirectoryFile(scope, rs);
					String sPath = rFile.getNeutralPath();
					if(sPath.endsWith("/" + file.getRelativePath())) {
						File baseDir = new File(scope.getRootDirectory(), sPath.substring(0, sPath.length()-file.getRelativePath().length()-1));
						long nId = file.getId();
						
						BannedDirectory dir = dirs.get(nId);
						if(dir == null) {
							dir = new BannedDirectory(scope, nId, baseDir);
							dir.setRemark(file.getRemark());
							dirs.put(nId, dir);
						}
						
						dir.add(file);
					}

				}));
		
		if(dirs.size() == 0) {
			return null;
		}
		
		if(dirs.size() > 0) {
			Query.perform(scope.getConnection(), query -> query.query("select * from banned_directory_file where id in (%s)",
					String.join(", ", StringUtil.join(dirs.keySet(), ","))).execute().process( rs -> {
						RecordBannedDirectoryFile file = new RecordBannedDirectoryFile(scope, rs);
						BannedDirectory dir = dirs.get(file.getId());
						file = dir.add(file);
						file._parent = dir;
					}));
			
			for(BannedDirectory dir : new ArrayList<>(dirs.values())) {
				if(!dir.isMatching()) {
					dirs.remove(dir.getId());
				}
			}
			
		}
		
		return dirs.values();
	}
	
	public static boolean deleteBannedDirectories(Scope scope, RecordFile file) throws IOException, SQLException {

		try(ActionContext ctx = scope.createActionContext(BannedDirectory.DELETE_ACTION)) {
			Collection<BannedDirectory> dirs = getBannedDirectories(scope, file);
			if(dirs == null) {
				return false;
			}
			
			for(BannedDirectory dir : dirs) {
				dir.delete();
			}
		}
		return true;
	}

}
