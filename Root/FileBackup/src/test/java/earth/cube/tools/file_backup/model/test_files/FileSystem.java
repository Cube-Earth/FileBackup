package earth.cube.tools.file_backup.model.test_files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import earth.cube.tools.file_backup.Globals;
import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.commons.Assertion;
import earth.cube.tools.file_backup.commons.FileCounter;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.Sha256Util;
import earth.cube.tools.file_backup.commons.StoredObject;
import earth.cube.tools.file_backup.commons.StringUtil;
import earth.cube.tools.file_backup.commons.ValidationError;
import earth.cube.tools.file_backup.commons.TestCaseValidationException;
import earth.cube.tools.file_backup.model.AbstractLinuxTestFile;
import earth.cube.tools.file_backup.model.BannedDirectory;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileSystem {
	
	public static AbstractLinuxTestFile F1 = new LinuxTestFile1();
	public static AbstractLinuxTestFile F2 = new LinuxTestFile2();
	public static AbstractLinuxTestFile F3 = new LinuxTestFile3();
	public static AbstractLinuxTestFile F4 = new LinuxTestFile4();
	public static AbstractLinuxTestFile F5 = new LinuxTestFile5();
	public static AbstractLinuxTestFile F6 = new LinuxTestFile6();

	private File _rootDir;
	private File _houseKeeperDir;
	private Map<String,AbstractLinuxTestFile> _files = new HashMap<>();
	private Map<String, CompoundFile> _compoundFiles;
	private File _backupDir;
	private Map<String,AbstractLinuxTestFile> _backupFiles = new HashMap<>();
	private File _archivedDir;
	private Map<String, AbstractLinuxTestFile> _archivedFiles = new HashMap<>();
	private File _deletedDir;
	private Map<String, AbstractLinuxTestFile> _deletedFiles = new HashMap<>();
	private int _nHardDeleted;
	private Set<Long> _inodeHistory = new HashSet<>();
	
	@Getter
	private int _nArchiveRemovalCount;
	
	
	public FileSystem(File rootDir) {
		_rootDir = rootDir;
		_houseKeeperDir = new File(rootDir, Globals.HOUSEKEEPER_DIR_NAME);
		if(!_houseKeeperDir.exists())
			throw new IllegalStateException("huh?");
		_archivedDir = new File(_houseKeeperDir, Globals.ARCHIVE_DIRECTORY);
		_archivedDir.mkdirs();
		_deletedDir = new File(_houseKeeperDir, Globals.DELETED_DIRECTORY);
		_deletedDir.mkdirs();
		_backupDir = new File(_houseKeeperDir, "backup");
		_backupDir.mkdirs();
	}

	public void add(String sPath, AbstractLinuxTestFile file) {
		_files.put(sPath, file.copy());
	}
	
	public void create() throws IOException, SQLException {
		for(Entry<String, AbstractLinuxTestFile> e : _files.entrySet()) {
			File f = new File(_rootDir, e.getKey());
			if(e.getValue().getFile() == null) {
				e.getValue().create(f);
				if(!e.getValue().isGenerated())
					_inodeHistory.add(e.getValue().getLinuxFile().getNodeId());
			}
		}
	}
	
	public AbstractLinuxTestFile get(String sPath) {
		return _files.get(sPath);
	}
	
	public void delete(String sPath) {
		if(!_files.containsKey(sPath))
			throw new IllegalStateException("Missing entry for File '" + sPath + "'!");
		
		File file = new File(_rootDir, sPath);
		if(!file.exists())
			throw new IllegalStateException("File '" + sPath + "' not found!");
		if(!file.isFile())
			throw new IllegalStateException("'" + sPath + "' is not a file!");
		if(!file.delete())
			throw new IllegalStateException("Could not delete file '" + sPath + "'!");
		
		_files.remove(sPath);
		_nHardDeleted++;
	}
	
	protected String findUniqueName(Map<String,AbstractLinuxTestFile> files, String sPreferredName) {
		if(!files.containsKey(sPreferredName))
			return sPreferredName;
		String sDir = FileUtil.getParentDirectory(sPreferredName);
		String sName = FileUtil.getNameWithoutExtension(sPreferredName);
		String sExt = FileUtil.getRawExtension(sPreferredName);
		int i = 0;
		String s;
		do {
			i++;
			s = sDir + '/' + sName + " (" + i + ")" + sExt;
		} while (files.containsKey(s));
		return s;
	}
	
	public void archive(String sPath) {
		if(!_files.containsKey(sPath))
			throw new IllegalStateException("Missing entry for File '" + sPath + "'!");
		
		File file = new File(_rootDir, sPath);
		if(!file.exists())
			throw new IllegalStateException("File '" + sPath + "' not found!");
		if(!file.isFile())
			throw new IllegalStateException("'" + sPath + "' is not a file!");
		
		AbstractLinuxTestFile testFile = _files.remove(sPath);
		sPath = findUniqueName(_archivedFiles, sPath);
		if(_archivedFiles.put(sPath, testFile) != null) {
			throw new IllegalStateException(sPath + " already archived!");
		}
		_nArchiveRemovalCount++;
	}
	
	public void archiveAndKeep(String sPath) {
		if(!_files.containsKey(sPath))
			throw new IllegalStateException("Missing entry for File '" + sPath + "'!");
		
		File file = new File(_rootDir, sPath);
		if(!file.exists())
			throw new IllegalStateException("File '" + sPath + "' not found!");
		if(!file.isFile())
			throw new IllegalStateException("'" + sPath + "' is not a file!");
		
		AbstractLinuxTestFile testFile = _files.get(sPath);
		sPath = findUniqueName(_archivedFiles, sPath);
		if(_archivedFiles.put(sPath, testFile.copy(true)) != null) {
			throw new IllegalStateException(sPath + " already archived!");
		}
	}


	public void softDelete(String sPath, String sNewPath) {
		if(!_files.containsKey(sPath))
			throw new IllegalStateException("Missing entry for File '" + sPath + "'!");
		
		File file = new File(_rootDir, sPath);
		if(!file.exists())
			throw new IllegalStateException("File '" + sPath + "' not found!");
		if(!file.isFile())
			throw new IllegalStateException("'" + sPath + "' is not a file!");
		
		AbstractLinuxTestFile testFile = _files.remove(sPath);
		sPath = sNewPath == null ? findUniqueName(_deletedFiles, sPath) : sNewPath;
		if(_deletedFiles.put(sPath, testFile) != null) {
			throw new IllegalStateException(sPath + " already deleted!");
		}

		_files.remove(sPath);
	}	

	public void softDelete(String sPath) {
		softDelete(sPath, null);
	}	
	
	
	
	/** moveOut and moveIn to preserve inode id in order to support specific test cases.
	 *
	 **/
	public void moveOut(String sPath) {
		if(!_files.containsKey(sPath))
			throw new IllegalStateException("Missing entry for File '" + sPath + "'!");
		
		File file = new File(_rootDir, sPath);
		if(!file.exists())
			throw new IllegalStateException("File '" + sPath + "' not found!");
		if(!file.isFile())
			throw new IllegalStateException("'" + sPath + "' is not a file!");
		
		File backupFile = new File(_backupDir, sPath);
		backupFile.getParentFile().mkdirs();
		if(!file.renameTo(backupFile))
			throw new IllegalStateException("Could not move out file '" + sPath + "'!");
		
		_backupFiles.put(sPath, _files.remove(sPath));
	}

	public void moveIn(String sPath) {
		if(_files.containsKey(sPath))
			throw new IllegalStateException("Unexpected entry for File '" + sPath + "'!");
		
		File file = new File(_rootDir, sPath);
		if(file.exists())
			throw new IllegalStateException("File '" + sPath + "' already exists!");
		
		File backupFile = new File(_backupDir, sPath);
		if(!backupFile.renameTo(file))
			throw new IllegalStateException("Could not move in file '" + sPath + "'!");
		
		_files.put(sPath, _backupFiles.remove(sPath));
	}
	
	
	public void create(String sPath, AbstractLinuxTestFile file) throws IOException, SQLException {
		if(_files.containsKey(sPath))
			throw new IllegalStateException("Unexpected entry for File '" + sPath + "'!");

		file = file.copy();
		_files.put(sPath, file);
		File f = new File(_rootDir, sPath);
		if(f.exists())
			throw new IllegalStateException("File '" + sPath + "' already exists!");
		file.create(f);
		if(!file.isGenerated())
			_inodeHistory.add(file.getLinuxFile().getNodeId());
	}

	public void update(String sPath, AbstractLinuxTestFile file, boolean bKeepFileDate) throws IOException, SQLException {
		AbstractLinuxTestFile existing = _files.get(sPath);
		if(existing == null)
			throw new IllegalStateException("Missing entry for File '" + sPath + "'!");

		file = file.copy();
		_files.put(sPath, file);
		File f = new File(_rootDir, sPath);
		if(!f.exists())
			throw new IllegalStateException("File '" + sPath + "' not found!");
		if(!f.isFile())
			throw new IllegalStateException("'" + sPath + "' is not a file!");
		if(bKeepFileDate)
			file.setDate(existing.getDate());
		file.create(f);
		if(file.isGenerated())
			throw new IllegalStateException("Huh?"); // generated files are either not yet existing or if (subsequent run of a register action), not any more in "generated" state because then they are registered.
		_inodeHistory.add(file.getLinuxFile().getNodeId());
	}

	public int getTotalInodeCount() {
		return _inodeHistory.size();
	}
	
	public int getFileCount() {
		return _files.size();
	}
	
	public int getHardDeletionCount() {
		return _nHardDeleted;
	}
	
	public int getSoftDeletionCount() {
		return _deletedFiles.size();
	}
	
	public int getArchiveCount() {
		return _archivedFiles.size();
	}	
	
	private Set<Long> getActiveInodes() throws IOException, SQLException {
		Set<Long> inodes = new HashSet<>();
		for(AbstractLinuxTestFile f : _files.values()) {
			if(!f.isGenerated())
				inodes.add(f.getLinuxFile().getNodeId());
		}
		return inodes;
	}

	public int getActiveInodeCount() throws IOException, SQLException {
		return getActiveInodes().size();
	}
	
	public int getOrphanedInodeCount() throws IOException, SQLException {
		Set<Long> orphaned = new HashSet<>(_inodeHistory);
		orphaned.removeAll(getActiveInodes());
		return orphaned.size();
	}
	
	public int getGeneratedFileCount() {
		return (int) _files.values().stream().filter( f -> f.isGenerated() ).count();
	}
	
	public void readCompoundFiles(Connection conn) throws Exception {
		_compoundFiles = CompoundFile.getCompoundFiles(conn);
	}

		
	public void checkDatabaseAgainstPhysicalFiles(Connection conn, int nInvalidFiles, int nOrphanedInodes) throws Exception {
		readCompoundFiles(conn);
		
		Set<Long> inodes = new HashSet<>();
		Set<String> expectedFiles = new HashSet<>(_files.keySet());
		_compoundFiles.values().stream().forEach( f -> {
			try {
				f.checkAgainstPyhsicalFile(_rootDir);
				if(f._bValid) {
					AbstractLinuxTestFile g = _files.get(f._sPath);
					if(g == null)
						throw new IllegalStateException("Missing file '" + f._sPath + "'!");
					assertEquals(g.getSha256(), f._sSha256);
					expectedFiles.remove(f._sPath);
					inodes.add(f._nInode);
				}
				else {
					if(_files.containsKey(f._sPath))
						throw new IllegalStateException("Unexpected file '" + f._sPath + "'!");
				}
			} catch (IOException | SQLException e) {
				throw new IllegalStateException(e);
			}
		});
		
		Set<String> generatedFiles = expectedFiles.stream().filter( f -> _files.get(f).isGenerated() ).collect(Collectors.toCollection(HashSet::new));

		// generated files (like purged.txt) are not yet registered in the database.
		expectedFiles.removeAll(generatedFiles);
		
		if(expectedFiles.size() > 0)
			throw new TestCaseValidationException(ValidationError.MISSING_DB_FILE_ENTRY, "Missing files: '" + String.join("', '", expectedFiles) + "'");

		// generated files (like purged.txt) are not yet registered in the database.
		assertEquals(getActiveInodeCount(), inodes.size());
		
		assertEquals(nInvalidFiles, CompoundFile._nInvalidFiles);
		assertEquals(nOrphanedInodes, CompoundFile._nOprhanedInodes);
	}
	
	
	protected void getPhysicalFilePaths(Set<String> filePaths, File dir) {
		for(File file : dir.listFiles()) {
			String sRelPath = Scope.neutralize(_rootDir, file);
			if(file.isDirectory()) {
				if(!sRelPath.equals('/' + Globals.HOUSEKEEPER_DIR_NAME))
					getPhysicalFilePaths(filePaths, file);
			}
			else
				if(!file.getName().equals(".DS_Store"))
					filePaths.add(sRelPath);
		}
	}
	
	protected void checkSectionFiles(String sLabel, Map<String, AbstractLinuxTestFile> expected, Set<String> actual, File baseDir) throws IOException {
		Set<String> mismatch = new HashSet<>(expected.keySet());
		mismatch.removeAll(actual);
		if(mismatch.size() > 0) {
			fail(sLabel + ": missing files '" + String.join("', '", mismatch) + "'");
		}
		
		mismatch = new HashSet<>(actual);
		mismatch.removeAll(expected.keySet());
		if(mismatch.size() > 0) {
			fail(sLabel + ": unexpected files '" + String.join("', '", mismatch) + "'");
		}

		for(Entry<String, AbstractLinuxTestFile> e : expected.entrySet()) {
			String sPath = e.getKey();
			File file = new File(baseDir, sPath);
			AbstractLinuxTestFile tf = e.getValue();

			if(!tf.isGenerated()) {
				assertEquals(tf.getSha256(), Sha256Util.getChecksum(file));
				Assertion.create().message(sPath).expected(tf.getDate()).actual(new Date(file.lastModified())).truncateMillis().checkEquals();
			}
			else {
				if(tf.getContent() != null) {
					assertEquals(tf.getSha256(), Sha256Util.getChecksum(file));
				} 
				else {
					assertTrue(file.exists());
					assertTrue(file.isFile());					
					assertTrue(file.length() > 0);					
				}
				
				if(tf.getDate() != null) {
					Assertion.create().message(sPath).expected(tf.getDate()).actual(new Date(file.lastModified())).truncateMillis().checkEquals();
				}
			}
		}

		assertEquals(expected.size(), actual.size());	// double check
	}

	public void checkAgainstPhysicalFiles() throws IOException {
		FileCounter cnt = new FileCounter(_rootDir);
		
		checkSectionFiles("Main files", _files, cnt.getMainFiles(), _rootDir);
		checkSectionFiles("Archived files", _archivedFiles, cnt.getArchivedFiles(), _archivedDir);
		checkSectionFiles("Deleted files", _deletedFiles, cnt.getDeletedFiles(), _deletedDir);

	}


	public void checkAll(Connection conn, int nInvalidFiles, int nOrphanedInodes) throws Exception {
		checkAgainstPhysicalFiles();
		checkDatabaseAgainstPhysicalFiles(conn, nInvalidFiles, nOrphanedInodes);
	}
	
	
	public CompoundFile getCompoundFile(String sPath) {
		return _compoundFiles.get(sPath);
	}
	
	
	
	public String getDump() {
		StringBuilder sb = new StringBuilder();
		
		int nWidth = _files.keySet().stream().map(String::length).max(Integer::compare).orElse(0);
		StoredObject<String> last = new StoredObject<>();
		
		_files.entrySet().stream().sorted(new Comparator<Entry<String, AbstractLinuxTestFile>>() {

			@Override
			public int compare(Entry<String, AbstractLinuxTestFile> e1, Entry<String, AbstractLinuxTestFile> e2) {
				File f1 = new File(e1.getKey());
				File f2 = new File(e2.getKey());
				int n = 0;
				
				// @DeleteDirectories should come first
				if(n == 0)
					n = e1.getKey().contains(BannedDirectory.DIR_TOKEN) ? (e2.getKey().contains(BannedDirectory.DIR_TOKEN) ? 0 : -1) : e2.getKey().contains(BannedDirectory.DIR_TOKEN) ? +1 : 0;

				// Files not in the same directory follow the natural sorting rules
				if(n == 0) {
					File p1 = f1.getParentFile();
					File p2 = f2.getParentFile();
					n = p1 == p2 ? 0 : p1 == null ? -1 : p2 == null ? +1 : p1.compareTo(p2);
				}
				
				// Direct children should come before other descendants (for the same directory)
				if(n == 0)
					n = Integer.compare(StringUtil.count(e1.getKey(), '/'), StringUtil.count(e2.getKey(), '/'));

				if(n == 0)
					n = e1.getKey().compareTo(e2.getKey());
				return n;
			}}).forEach( e -> {
				String sRoot = FileUtil.getRootDirectoryName(e.getKey());
				if(last.getObject() != null && !last.getObject().equals(sRoot)) {
					sb.append("\n");
				}
				last.setObject(sRoot);
				sb.append(String.format("%-" + nWidth + "s  %s\n", e.getKey(), e.getValue().getDump()));
			});
		
		return sb.toString();		
	}

	
}
