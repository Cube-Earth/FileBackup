package earth.cube.tools.file_backup.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import earth.cube.tools.file_backup.Scope;
import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.TestDataDirectory;
import earth.cube.tools.file_backup.commons.TestDataDirectoryBuilder;
import earth.cube.tools.file_backup.database.RecordSequence;

public class RecordSequenceTest {
	
	protected final static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	
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
		


	private int getCounter(String sName) throws SQLException, IOException {
		return new RecordSequence(_scope, sName).acquireNext();
	}
	

	
	@Test
	public void test_1() throws IOException, SQLException {
		assertEquals(1, getCounter("batch"));
		assertEquals(2, getCounter("batch"));
		assertEquals(3, getCounter("batch"));

		assertEquals(1, getCounter("run"));
		assertEquals(4, getCounter("batch"));

		assertEquals(2, getCounter("run"));
		assertEquals(5, getCounter("batch"));
		assertEquals(3, getCounter("run"));
		assertEquals(4, getCounter("run"));
	
	}
	
	@Test
	public void test_2() throws IOException, SQLException {
		assertEquals(1, _scope.getBatchId());
		_scope.close();
		_scope = new Scope(_rootDir);
		assertEquals(2, _scope.getBatchId());
	}
	

}
