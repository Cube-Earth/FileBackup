package earth.cube.tools.file_backup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import earth.cube.tools.file_backup.commons.AssertUtils;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.Query;
import earth.cube.tools.file_backup.commons.TestDataDirectory;
import earth.cube.tools.file_backup.commons.TestDataDirectoryBuilder;
import earth.cube.tools.file_backup.commons.ValidationError;
import earth.cube.tools.file_backup.model.test_files.FileSystem;

public class Application_RegisterFilesInDatabase {
	
	protected TestDataDirectory _testDataDir;
	private File _rootDir;
	private Scope _scope;
	
	@BeforeEach
	public void setUp(TestInfo info) throws IOException, SQLException {
		_testDataDir = TestDataDirectoryBuilder.instance(info).build();
		_rootDir = _testDataDir.getDirectory();
		assertEquals(1, FileUtil.countFiles(_rootDir)); // database file
		assertEquals(0, _testDataDir.getDatabaseFile().length());
		_scope = new Scope(_rootDir);
		System.out.println("=== " + info.getDisplayName() + " ==============================");
	}
	
	
	protected void checkCounts(FileSystem fs, int nFileCount, int nInodeCount) throws IOException, SQLException {
		System.out.println("--------------------------------------");
		assertEquals(nFileCount, fs.getFileCount());
		
		int n = fs.getFileCount();
		n++;   // VolumeFiles.db
		if(new File(_scope.getHousekeeperDir(), "VolumeFiles.properties").exists()) {
			n++;   // VolumeFiles.properties
		}
		assertEquals(n, FileUtil.countFiles(_rootDir));

		assertEquals(nInodeCount, fs.getActiveInodeCount());
	}
	
	protected void executeAction() throws Exception {
		String[] saArgs = {
		    "-s", _rootDir.getAbsolutePath(),
		    "-a", ActionType.REGISTER_FILES.toString()
		};
		
		Application.main(saArgs);
	}
	
	
	protected boolean existsStatistics(int nBatchId) throws IOException, SQLException {
		int n = Query.perform(_scope.getConnection(), q -> q.query("SELECT COUNT(*) AS cnt FROM STATISTICS WHERE BATCH_ID=%s", nBatchId).execute().asInt("cnt"));	
		return n > 0;
	}
	
	
	@Test
	public void test_1() throws Exception {
		
		assertFalse(existsStatistics(_scope.getBatchId()));
		assertFalse(existsStatistics(_scope.getBatchId()+1));
		
		FileSystem fs = new FileSystem(_rootDir);
		fs.add("test/t1.txt", FileSystem.F1);
		fs.add("test/t2.txt", FileSystem.F2);
		fs.add("test1/t1.txt", FileSystem.F3);
		fs.add("test1/t2.txt", FileSystem.F1);
		fs.create();
		
		checkCounts(fs, 4, 4);

		fs.checkAgainstPhysicalFiles();
		
		// database is still empty
		AssertUtils.assertValidationThrows(ValidationError.MISSING_DB_FILE_ENTRY, () -> fs.checkDatabaseAgainstPhysicalFiles(_scope.getConnection(), 0, 0));
		
		executeAction();

		assertFalse(existsStatistics(_scope.getBatchId()));
		assertTrue(existsStatistics(_scope.getBatchId()+1));
		
		checkCounts(fs, 4, 4);

		fs.checkAgainstPhysicalFiles();
		fs.checkDatabaseAgainstPhysicalFiles(_scope.getConnection(), 0, 0);
		fs.checkAll(_scope.getConnection(), 0, 0);
		
		Query.perform(_scope.getConnection(), q -> q.query("delete from file where path='test/t1.txt'").execute());
		
		AssertUtils.assertValidationThrows(ValidationError.MISSING_DB_FILE_ENTRY, () -> fs.checkDatabaseAgainstPhysicalFiles(_scope.getConnection(), 0, 0));
		fs.checkAgainstPhysicalFiles();
		AssertUtils.assertValidationThrows(ValidationError.MISSING_DB_FILE_ENTRY, () -> fs.checkAll(_scope.getConnection(), 0, 0));
		
	}

	
}
