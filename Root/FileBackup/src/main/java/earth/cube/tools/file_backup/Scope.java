package earth.cube.tools.file_backup;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Stack;

import earth.cube.tools.file_backup.commons.Cache;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.PruneEmptyDirectories;
import earth.cube.tools.file_backup.database.FileDatabase;
import earth.cube.tools.file_backup.database.RecordInode;
import earth.cube.tools.file_backup.database.RecordSequence;
import earth.cube.tools.file_backup.model.LinuxFile;
import earth.cube.tools.file_backup.model.RecordStatistics;
import lombok.Getter;
import lombok.Setter;

public class Scope implements Closeable {
	
	private Object _deviceId;
	private File _databaseFile;
	private FileDatabase _database;
	private File _rootDir;
	private File _housekeeperDir;
	private Connection _connection;
	private Cache<Long,RecordInode> _inodeCache = new Cache<>();
	private int _nBatchId;
	private RecordStatistics _stats;
	private File _propsFile;
	private Properties _props;
	private String _sHostName;
	
	@Getter
	private ScopedActions _currentActions = new ScopedActions();
	private Stack<ScopedActions> _actions = new Stack<>();

	@Getter
	private File _archiveDirectory;
	
	@Getter
	private File _deletionsDirectory;
	
	@Getter
	protected PruneEmptyDirectories _pruneEmptyDirectories = new PruneEmptyDirectories(this);

	@Getter @Setter
	protected boolean _bProbing;
	
	
	public class ActionContext implements Closeable {
		
		public ActionContext(String... saActions) {
			_actions.push(_currentActions);
			_currentActions = new ScopedActions(_currentActions, saActions);
		}

		@Override
		public void close() throws IOException {
			_currentActions = _actions.pop();
		}
		
	}
	
	
	public static File findRootDirectory(File dir) throws IOException, SQLException {
		return dir == null ? null : new File(dir, Globals.HOUSEKEEPER_DIR_NAME + "/" + Globals.DATABASE_NAME).exists() ? dir : findRootDirectory(dir.getParentFile());
	}
	
	
	public Scope(File baseDir) throws IOException, SQLException {
		LinuxFile d = new LinuxFile(baseDir);
		_deviceId = d.getDeviceId();
		findDatabase(d);
		_database = new FileDatabase(_databaseFile);
		_connection = _database.getConnection();
		_nBatchId = new RecordSequence(this, "batch_id").acquireNext();
		_stats = new RecordStatistics(this);
		_sHostName = System.getenv("HOSTNAME");
		if(_sHostName == null || _sHostName.length() == 0)
			_sHostName = System.getenv("COMPUTERNAME");
		if(_sHostName == null || _sHostName.length() == 0)
			_sHostName = InetAddress.getLocalHost().getHostName();
		if(_sHostName == null || _sHostName.length() == 0)
			throw new IllegalStateException("Host name need to be set properly!");
		_archiveDirectory = FileUtil.mkdirs(new File(_housekeeperDir, Globals.ARCHIVE_DIRECTORY));
		_deletionsDirectory = FileUtil.mkdirs(new File(_housekeeperDir, Globals.DELETED_DIRECTORY));
	}

	private void findDatabase(LinuxFile dir) throws IOException, SQLException {
		if(!_deviceId.equals(dir.getDeviceId()))
			throw new IllegalStateException("No database found!");

		File db = new File(dir.getFile(), Globals.HOUSEKEEPER_DIR_NAME + "/" + Globals.DATABASE_NAME);
		if(db.exists()) {
			_databaseFile = db;
			_housekeeperDir = _databaseFile.getParentFile();
			_rootDir = _housekeeperDir.getParentFile();
			_propsFile = new File(_housekeeperDir, Globals.PROPERTIES_FILE_NAME);
			_props = new Properties();
			if(_propsFile.exists()) {
				try(InputStream is = new FileInputStream(_propsFile)) {
					_props.load(is);
				}
			}
			return;
		}
		
		File parent = dir.getFile().getParentFile();
		if(parent == null)
			throw new IllegalStateException("No database found!");
		
		findDatabase(new LinuxFile(parent));
	}
	
	public FileDatabase getDatabase() {
		return _database;
	}
	
	public File getHousekeeperDir() {
		return _housekeeperDir;
	}
	
	public File getRootDirectory() {
		return _rootDir;
	}

	public static String neutralize(File rootDir, File file) {
		String sRelPath = FileUtil.getRelativePath(rootDir, file);
		if(sRelPath == null)
			throw new IllegalStateException(String.format("'%s' is not underneath '%s'!", file.getAbsolutePath(), rootDir.getAbsolutePath()));
		return sRelPath;
	}
	
	public String neutralize(File file) {
		return neutralize(_rootDir, file);
	}
	
	public Connection getConnection() {
		return _connection;
	}

	public File getDatabaseFile() {
		return _databaseFile;
	}
	
	public int getBatchId() {
		return _nBatchId;
	}
	
	public Cache<Long, RecordInode> getInodeCache() {
		return _inodeCache;
	}
	
	public RecordStatistics getStatistics() {
		return _stats;
	}
	
	public void saveProperties() {
		try(OutputStream os = new FileOutputStream(_propsFile)) {
			_props.store(os, null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private String getPropertyName(Object obj, String sKey) {
		return _sHostName + '.' + obj.getClass().getCanonicalName().replaceAll("\\.", "__") + '.' + sKey;
	}

	public void setProperty(Object obj, String sKey, String sValue) {
		_props.setProperty(getPropertyName(obj, sKey), sValue);
	}

	public void removeProperty(Object obj, String sKey) {
		_props.remove(getPropertyName(obj, sKey));
	}

	public File getPropertyAsFile(Object obj, String sKey) {
		String s = _props.getProperty(getPropertyName(obj, sKey));
		return s == null || s.length() == 0 ? null : new File(s);
	}

	
	public void resetCache() {
		_inodeCache.reset();
	}
	
	public ActionContext createActionContext(String... saActions) {
		return new ActionContext(saActions);
	}
	
	public boolean isRunningAction(String... saAction) {
		return _currentActions.isRunningAction(saAction);
	}

	public void cleanUp() {
		_pruneEmptyDirectories.cleanUp();
	}

	@Override
	public void close() throws IOException {
		cleanUp();
		if(_database != null) {
			_database.close();
			_database = null;
		}
	}



}
