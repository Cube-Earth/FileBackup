package earth.cube.tools.file_backup.model.test_files;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import earth.cube.tools.file_backup.commons.FileUtil;
import earth.cube.tools.file_backup.commons.TestDataDirectory;
import earth.cube.tools.file_backup.commons.TestDataDirectoryBuilder;

public class FileSystemTest {
	
	protected TestDataDirectory _testDataDir;
	private File _rootDir;
	
	@BeforeEach
	public void setUp(TestInfo info) throws IOException {
		_testDataDir = TestDataDirectoryBuilder.instance(info).build();
		_rootDir = _testDataDir.getDirectory();
		assertEquals(1, FileUtil.countFiles(_rootDir)); // database file
		assertEquals(0, _testDataDir.getDatabaseFile().length());
	}
	
	@Test
	public void test_1() throws IOException, SQLException {
		FileSystem fs = new FileSystem(_testDataDir.getDirectory());
		fs.add("/test/t1.txt", FileSystem.F1);
		fs.add("/test/t2.txt", FileSystem.F2);
		fs.add("/test1/t1.txt", FileSystem.F3);
		fs.add("/test1/t2.txt", FileSystem.F1);
		fs.create();
		assertEquals(4, fs.getFileCount());
		assertEquals(fs.getFileCount() + 1, FileUtil.countFiles(_rootDir));
	}

}
