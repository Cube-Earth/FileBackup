package earth.cube.tools.file_backup.model;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.Scope.ActionContext;
import earth.cube.tools.file_backup.commons.EscapeUtil;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.commons.ValidationException;
import earth.cube.tools.file_backup.database.RecordInode;
import earth.cube.tools.file_backup.database.to_be_checked.RecordBannedDirectoryFile;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RecordFile extends LinuxFile {
	
	private static final String DELETE_TOKEN = "/@Delete/";

	private static final String REFRESH_ACTION = "RecordFile:Refresh";
	
	private Date _updatedTime = new Date();
	private String _sNeutralPath;
	
	@Getter
	protected Scope _scope;
	
	private boolean _bNew;

	private boolean _bHasDatabaseEntry;
	

	public RecordFile(Scope scope, File file) throws IOException, SQLException {
		super();
		_log.debug("<init>: unregistered file");
		_log.debug("<init>: file = " + file.getAbsolutePath());
		_file = file;
		_scope = scope;
		_sNeutralPath = scope.neutralize(file);
		_bNew = true;
		_log.debug("<init>: path = " + _sNeutralPath);
		update();
	}

	public RecordFile(Scope scope, ResultSet rs) throws IOException, SQLException {
		super();
		_log.debug("<init>: checking registered file");
		_scope = scope;
		load(rs);
		update();
	}
	
	protected void load(ResultSet rs) throws SQLException {
		_dLastModified = new Date();
		if(_sNeutralPath == null) {
			_sNeutralPath = rs.getString("PATH");
			_log.debug("load: loaded path = " + _sNeutralPath);
		}
		else
			_log.debug("load: path = " + _sNeutralPath);
		_nInodeId = rs.getLong("INODE");
		_dLastModified = rs.getTimestamp("MODIFIED");
		_bValid = rs.getBoolean("VALID");
		_updatedTime = rs.getTimestamp("UPDATED");
		_bHasDatabaseEntry = true;
		_snapshot.record();
	}
		
	protected void updateByRecord() throws IOException, SQLException {
		Query.perform(_scope.getConnection(), q -> q.query("SELECT PATH, INODE, MODIFIED, VALID, UPDATED FROM FILE WHERE PATH='%s'", EscapeUtil.escapeSqlString(_sNeutralPath)).execute().process ( rs -> {
			load(rs);
		}));	
	}

	
	protected void update() throws IOException, SQLException {
		_dRefreshDate = new Date();
		
		boolean bTriggeredByDatabase = _file == null;
		if(!bTriggeredByDatabase) {
			updateByRecord();
		}
		else {
			_file = new File(_scope.getRootDirectory(), _sNeutralPath);
		}
		
		updateByFile();
		if(isValid()) {
			_inode = _scope.getInodeCache().get(_nInodeId, k -> {
				try {
					return new RecordInode(_scope, this);
				} catch (IOException | SQLException e) {
					throw new RuntimeException(e);
				}
			});
			
			BannedDirectory bannedDir = null;
			
			boolean bCheck = false;
			
			int j = _sNeutralPath.indexOf(BannedDirectory.DIR_TOKEN);
			if(!_scope.isRunningAction(BannedDirectory.REGISTER_ACTION, BannedDirectory.DELETE_ACTION, REFRESH_ACTION)) {
				if(!RecordBannedDirectoryFile.deleteBannedDirectories(_scope, this)) {
					if(j == -1 && ((RecordInode) _inode).isBanned()) {
						String sSha256 = getSha256(null);
						if(sSha256.equals(_inode.getSha256())) {
							bundle().delete();
						}
					}
					else {
						int i = _sNeutralPath.indexOf(DELETE_TOKEN);
						if(i != -1 && j != -1) {
							throw new IllegalStateException("Huh?");
						}
						if (i != -1) {
							String[] saPortions = _sNeutralPath.substring(i + DELETE_TOKEN.length()).split("/", 2);
							chain().ban(saPortions.length == 2 ? null : saPortions[0]);
						}
						else
							if(j != -1) {
								if(!_file.exists()) { // TODO remove
									System.out.println("~~");
								}
								try(ActionContext ctx = _scope.createActionContext(BannedDirectory.REGISTER_ACTION)) {
									bannedDir = new BannedDirectory(_scope, this);
									bannedDir.register();
									bannedDir.delete();
									bCheck = true;
								}
								catch(ValidationException e) {
									_log.error("<init>: Exception occurred -->", e);
								}
							}
					}
				}
				else
					bCheck = true;
			}
			
			// a new banned directory has been just registered.
			// now, find deletion candidates.
			if(bannedDir != null) {
				chain().banDirectory(bannedDir);
			}
			
			if(bCheck && !_file.exists()) {
				// has been deleted by bannedDir actions from above lines.
				// database has already been updated
				_bValid = false;
				_nInodeId = 0;
				_inode = null;
				_snapshot.record();  // skip database update
			}
		}
		else {
			_nInodeId = 0;
			_inode = null;
		}
		
		_scope.getStatistics().incrementCheckedFiles();
		if(_snapshot.hasChanged()) {
			if(!_bValid) {
				_log.info("update: path '{}' is deleted", _sNeutralPath);
				_scope.getStatistics().incrementUpdatedFiles();
			}
			else
				if(_bNew) {
					_log.info("update: path '{}' is new", _sNeutralPath);
					_scope.getStatistics().incrementNewFiles();
				}
				else {
					_log.info("update: path '{}' is changed", _sNeutralPath);
					_scope.getStatistics().incrementUpdatedFiles();
				}
			save();
		}
		else {
			_log.info("update: path '{}' is unchanged", _sNeutralPath);
			_scope.getStatistics().incrementUnchangedFiles();
		}
	}	
	
	public void save() throws IOException, SQLException {
		_log.debug("save: entered");
		
		if(!_bHasDatabaseEntry && !_bValid) {
			_log.debug("save: skipping creation of database entries for invalid files ...");
			return;
		}

		_bNew = false;
		_updatedTime = _snapshot.getRefreshDate();
//		Query.perform(_scope.getConnection(), q -> q.query("INSERT OR REPLACE INTO FILE (PATH, INODE, MODIFIED, VALID, UPDATED, BATCH_ID) VALUES('%s', %s, %s, %s, %s, %s)", EscapeUtil.escapeSqlString(_sNeutralPath), _bValid ? _inode.getId() : 0, Query.getJulianDate(_dLastModified), _bValid ? 1 : 0, Query.getJulianDate(_updatedTime), _scope.getBatchId()).execute());
		
		RecordFileSaver.create(_scope.getConnection()).path(_sNeutralPath).inodeId(_bValid ? _nInodeId : 0).valid(_bValid).lastModified(_dLastModified).updatedTime(_updatedTime).save();
		
		_bHasDatabaseEntry = true;
		_snapshot.record();
		
		if(_inode != null)
			((RecordInode) _inode).save();
	}
	
	
	public String getNeutralPath() {
		return _sNeutralPath;
	}
	
	
	public File getTargetFile(BaseFileOperationType op) {
		File baseDir;				
		switch(op) {

			case DELETE:
				baseDir = _scope.getDeletionsDirectory();
				break;
				
			case ARCHIVE:
				baseDir = _scope.getArchiveDirectory();
				break;

			default:
				throw new IllegalArgumentException("Unsupported operation '" + op + "'!");
		}

		File targetFile = new File(baseDir, getNeutralPath());
		return targetFile;
	}
	
	
	protected boolean moveOut(String sActionLabel, File targetFile) throws IOException, SQLException {
		if(_scope.isProbing()) {
			log.debug("archive: PROBING " + sActionLabel + " file '" + _file.getAbsolutePath() + "' ...");
			return false;
		}
		else {
			log.debug("moveOut: " + sActionLabel + " file '" + _file.getAbsolutePath() + "' ...");
			log.debug("moveOut: moving " + sActionLabel + " file to '" + targetFile.getAbsolutePath() + "' ...");
			FileUtil.renameTo(_file, targetFile);
			
			// in case a file is not registered yet and will immediately be deleted (archived or soft deleted)
			// no database entry is written back. Enforce that a database entry is always written back
			// to document that an formerly existing file has been deleted. This behavior provides consistency
			// in manual database analysis and JUnit test case executions.
			_bHasDatabaseEntry = true; // for consistency purposes, enforce that an database entry is created for this file which will potential become invalid

			return true;
		}
	}

	public void archive(File targetFile) throws IOException, SQLException {
		moveOut("archived", targetFile == null ? getTargetFile(BaseFileOperationType.ARCHIVE) : targetFile);
	}

	
	public void archive(File targetFile, boolean bDelete) throws IOException, SQLException {
		if(moveOut("archived", targetFile == null ? getTargetFile(BaseFileOperationType.ARCHIVE) : targetFile) && bDelete) {
			_scope.getPruneEmptyDirectories().add(_file.getParentFile());
			String sPath = _sNeutralPath;
			long nInodeId = _nInodeId;
			_bValid = false;
			_nInodeId = 0;
			_inode = null;
			save();
			refreshInode(nInodeId, sPath);
		}
	}

	public void delete(File targetFile) throws IOException, SQLException {
		if(moveOut("deletions", targetFile == null ? getTargetFile(BaseFileOperationType.DELETE) : targetFile)) {
			_scope.getPruneEmptyDirectories().add(_file.getParentFile());
			String sPath = _sNeutralPath;
			long nInodeId = _nInodeId;
			_bValid = false;
			_nInodeId = 0;
			_inode = null;
			save();
			refreshInode(nInodeId, sPath);
		}
	}
	
	
	public ChainedFileOperation chain() {
		return new ChainedFileOperation(this);
	}
	
	public BundledFileOperation bundle() throws IOException, SQLException {
		return new BundledFileOperation(this);
	}
	
	public ChainedFileOperation chain(Scope destinationScope) {
		return new ChainedFileOperation(destinationScope, this);
	}

	public void refreshInode(long nInodeId, String sPath) throws IOException, SQLException {
		System.out.println("x: " + sPath + ", " + nInodeId);
		if(sPath.equals("test/t2.txt")) // TODO remove
			System.out.println("~~");
		try(ActionContext ctx = _scope.createActionContext(REFRESH_ACTION)) {
			_scope.getInodeCache().remove(nInodeId);
			Query.perform(_scope.getConnection(), q -> q.query("select * from file where inode=%s and valid=1 and path<>'%s'", nInodeId, EscapeUtil.escapeSqlString(sPath)).execute().cache().processUntil( rs -> {
				RecordFile file = new RecordFile(_scope, rs);
				System.out.println("y: " + file.getNeutralPath() + " - " + file.isValid());
				return !file.isValid();
			}));
			
		}
	}
	
	
	@Override
	public String toString() {
		return _file.getAbsolutePath();
	}

	public static void ensureTable(Connection conn) throws IOException, SQLException {
	    if(!Query.existsTable(conn, "FILE")) {
	    	String sSql = "CREATE TABLE FILE " +
	                 "(PATH         TEXT    PRIMARY KEY    NOT NULL," +
	                 " INODE        LONG                   NOT NULL," +
	                 " MODIFIED     REAL                   NOT NULL," +
	                 " VALID        TINYINT                NOT NULL," +
	    			 " UPDATED      REAL                   NOT NULL," +
	                 " BATCH_ID     INT                    NOT NULL)";
		    Query.perform(conn, q -> q.query(sSql).execute());
	    }
	    
	    final String sSql = "CREATE INDEX IF NOT EXISTS IDX_FILE_INODE ON FILE (INODE, VALID)";
	    Query.perform(conn, q -> q.query(sSql).execute());
	}
	

	
	public static List<RecordFile> findFiles(Scope scope, long nInodeId) throws IOException, SQLException {
		List<RecordFile> files = new ArrayList<>();
		Query.perform(scope.getConnection(), q -> q.query("select * from file where inode=%s and valid=1", nInodeId).execute().process( rs -> {
			RecordFile file = new RecordFile(scope, rs);
			if(file.isValid() && file.getNodeId() == nInodeId)
				files.add(file);
		}));

		return files;
	}

	
	/** Find files with same sha256 but other inode id.
	 * 
	 * @param scope
	 * @param searchFile
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static List<RecordFile> findOtherFilesWithSameSha256(Scope scope, LinuxFile searchFile) throws IOException, SQLException {
		List<RecordFile> files = new ArrayList<>();
		
		if(!searchFile.isValid())
			return files;
		
		Set<Long> inodes = new HashSet<>();
		
		LinuxInode searchInode = searchFile.getInode();
		String sSha256 = searchInode.getSha256();
		long nSize = searchInode.getSize();
		String sExtension = searchInode.getExtension();

		Query.perform(scope.getConnection(), q -> q.query("select f.* from file f, inode i where f.inode=i.id and f.valid=1 and i.sha256='%s' and i.size=%s and i.extension='%s'", sSha256, nSize, sExtension.replace("'", "''")).execute().cache().process( rs -> {
			RecordFile file = new RecordFile(scope, rs);
			if(file.isValid()) {
				LinuxInode inode = file.getInode();
				if(inode.getSize() == nSize 
						&& inode.getExtension().equals(sExtension) 
						&& inode.getSha256().equals(sSha256)) {
//						&& inode.getSha256Source() == DataSource.CALCULATED)
					files.add(file);
					inodes.add(inode.getId());
				}
			}
		}));
		
		if(inodes.size() <= 1) {
			return Collections.emptyList();
		}
		
		files.sort(new Comparator<LinuxFile>() {

			@Override
			public int compare(LinuxFile o1, LinuxFile o2) {
				int n = -Integer.compare(o1.getLinkCount(), o1.getLinkCount());
				if(n == 0)
					n = o1.getFile().compareTo(o2.getFile());
				return n;
			}
			
		});
		
		return files;
	}

	
	public static List<RecordFile> findFilesWithSameSha256(Scope scope, LinuxFile searchFile) throws IOException, SQLException {
		List<RecordFile> files = new ArrayList<>();
		
		if(!searchFile.isValid())
			return files;
		
		LinuxInode searchInode = searchFile.getInode();
		String sSha256 = searchInode.getSha256();
		long nSize = searchInode.getSize();
		String sExtension = searchInode.getExtension();

		Query.perform(scope.getConnection(), q -> q.query("select f.* from file f, inode i where f.inode=i.id and f.valid=1 and i.sha256='%s' and i.size=%s and i.extension='%s'", sSha256, nSize, sExtension.replace("'", "''")).execute().cache().process( rs -> {
			RecordFile file = new RecordFile(scope, rs);
			if(file.isValid()) {
				LinuxInode inode = file.getInode();
				if(inode.getSize() == nSize 
						&& inode.getExtension().equals(sExtension) 
						&& inode.getSha256().equals(sSha256))
//						&& inode.getSha256Source() == DataSource.CALCULATED)
					files.add(file);
			}
		}));
		
		files.sort(new Comparator<LinuxFile>() {

			@Override
			public int compare(LinuxFile o1, LinuxFile o2) {
				int n = -Integer.compare(o1.getLinkCount(), o1.getLinkCount());
				if(n == 0)
					n = o1.getFile().compareTo(o2.getFile());
				return n;
			}
			
		});
		
		return files;
	}

	public static RecordFile remove(List<RecordFile> list, RecordFile fileToRemove) {
		for(RecordFile file : new ArrayList<>(list)) {
			if(file.getFile().equals(fileToRemove.getFile())) {
				list.remove(file);
				return file;
			}
		}
		return null;
	}

	/** "safe" because sha256 is checked.
	 * 
	 * @param scope
	 * @param searchFile
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static RecordFile findSafeClone(Scope scope, RecordFile searchFile) throws IOException, SQLException {
		if(!searchFile.isValid())
			return null;

		String sClonePath = Query.perform(scope.getConnection(), q -> q.query("select path from file f where inode=%s and valid=1 and path<>'%s'", searchFile.getInode(), searchFile.getNeutralPath()).execute().processUntil( rs -> {
			RecordFile file = new RecordFile(scope, rs);
			if(file.isValid() && searchFile.getCalculatedSha256FromInode().equals(file.getCalculatedSha256FromInode())) {
				return false;
			}
			return true;
		}).asString("path"));
		
		RecordFile clone = sClonePath == null ? null : new RecordFile(scope, new File(scope.getRootDirectory(), sClonePath));
		
		return clone;
	}

	public static void updateInodes(Scope scope, Set<Long> inodes) throws IOException, SQLException {
		for(long inode : inodes) {
			scope.getInodeCache().remove(inode);

			Query.perform(scope.getConnection(), q -> q.query("select PATH, INODE, MODIFIED, VALID, UPDATED from file f where inode=%s and valid=1", inode).execute().processUntil( rs -> {
				RecordFile file = new RecordFile(scope, rs);
				if(file.isValid()) {
					return false;
				}
				return true;
			}));
		}
	}

	
}
