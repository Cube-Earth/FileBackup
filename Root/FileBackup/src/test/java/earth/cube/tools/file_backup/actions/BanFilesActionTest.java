package earth.cube.tools.file_backup.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import earth.cube.tools.file_backup.commons.DatabaseNumberChecker;
import earth.cube.tools.file_backup.commons.DebugScope;
import earth.cube.tools.file_backup.model.test_files.FileSystem;

public class BanFilesActionTest extends AbstractActionTest {
	
	
	
	
	@Test
	public void test_1() throws Exception {
		banFiles();
	}
	

	@Test
	public void test_2() throws Exception {
		banFiles();

		// all descendants of /test
		_fs.softDelete("test/t1.txt");
		_fs.softDelete("test/t2.txt");
		_fs.softDelete("test/t3.TXT");
		_fs.softDelete("test/t4.log");
		
		// all with same content
		_fs.softDelete("t1.txt");
		_fs.softDelete("test1/t2.txt");
		_fs.softDelete("test1/folder1/t1.txt");
		_fs.softDelete("test2/a/1/t6.txt");
 		_fs.softDelete("test2/a/t7.txt");
 		
 		assertEquals(2, _fs.getFileCount()); // only for paranoia purposes
				
		executeBanFilesAction("test");
		
		_scope.resetCache();
		
		executeRegisterFilesAction();
		
		_fs.checkAll(_scope.getConnection(), 13, 13);

		// banned files count is 4 and not 3 as F2 has two extensions, txt and log
		DatabaseNumberChecker.from(_scope).fileSystem(_fs).sharedInodes(0).banned(4).check();
		
		assertFalse(new File(_scope.getRootDirectory(), "test").exists());
		assertFalse(new File(_scope.getRootDirectory(), "test1/folder1").exists());
		assertFalse(new File(_scope.getRootDirectory(), "test2/a/1").exists());
		
	}

	@Test
	public void test_3() throws Exception {
		banFiles();

		_fs.add("test1/@Delete/1/t1.txt", FileSystem.F1);
		_fs.add("test1/@Delete/t7.txt", FileSystem.F2);
		_fs.add("test1/@Delete/1/t2.txt", FileSystem.F5);
		_fs.create();

		// all descendants of /test1/@Delete
		_fs.softDelete("test1/@Delete/1/t1.txt");
		_fs.softDelete("test1/@Delete/t7.txt");
		_fs.softDelete("test1/@Delete/1/t2.txt");
		
		// all with same content
		_fs.softDelete("test/t1.txt");
		_fs.softDelete("test/t2.txt");
		_fs.softDelete("test/t3.TXT");
		_fs.softDelete("t1.txt");
		_fs.softDelete("test1/t2.txt");
		_fs.softDelete("test1/folder1/t1.txt");
		_fs.softDelete("test2/a/1/t6.txt");
 		_fs.softDelete("test2/a/t7.txt");
 		
 		assertEquals(3, _fs.getFileCount()); // only for paranoia purposes

		executeRegisterFilesAction();
		
		_fs.checkAll(_scope.getConnection(), 15, 15);

		// banned files count is 4 and not 3 as F2 has two extensions, txt and log
		DatabaseNumberChecker.from(_scope).fileSystem(_fs).sharedInodes(0).banned(3).check();
		
		assertTrue(new File(_scope.getRootDirectory(), "test").exists());
		assertFalse(new File(_scope.getRootDirectory(), "test1/folder1").exists());
		assertFalse(new File(_scope.getRootDirectory(), "test2/a/1").exists());
		assertFalse(new File(_scope.getRootDirectory(), "test1/@Delete").exists());

	}	
	

	
}
