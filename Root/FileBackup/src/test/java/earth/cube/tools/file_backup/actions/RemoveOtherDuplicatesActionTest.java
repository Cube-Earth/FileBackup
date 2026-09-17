package earth.cube.tools.file_backup.actions;

import org.junit.jupiter.api.Test;

import earth.cube.tools.file_backup.commons.DatabaseDumper;
import earth.cube.tools.file_backup.commons.DatabaseNumberChecker;

public class RemoveOtherDuplicatesActionTest extends AbstractActionTest {
	
/*		
	_fs.add("/t1.txt", FileSystem.F1);
	_fs.add("/test/t1.txt", FileSystem.F1);
	_fs.add("/test/t2.txt", FileSystem.F2);
	_fs.add("/test/t3.TXT", FileSystem.F2);
	_fs.add("/test/t4.log", FileSystem.F2);
	_fs.add("/test1/t2.txt", FileSystem.F1);
	_fs.add("/test1/t3.txt", FileSystem.F4);
	_fs.add("/test1/folder1/t1.txt", FileSystem.F1);
	_fs.add("/test2/a/1/t6.txt", FileSystem.F1);
		_fs.add("/test2/a/t7.txt", FileSystem.F2);       // will be created later
	_fs.add("/test2/a/t8.TXT", FileSystem.F3);
*/		
	
	
	@Test
	public void test_1() throws Exception {
		createBaseTestData();

		_fs.checkAll(_scope.getConnection(), 4, 9);
		
		_fs.archive("t1.txt");
		_fs.archive("test1/t2.txt");
		_fs.archive("test1/folder1/t1.txt");
		_fs.archive("test2/a/1/t6.txt");
		_fs.archive("test2/a/t7.txt");
		
//		_scope.resetCache();
		
		DatabaseDumper.from(_scope).dumpCompoundFiles();
		
		executeRemoveOtherFilesAction("test");

		DatabaseDumper.from(_scope).dumpCompoundFiles();
		
		// There is one more orphaned inode as /test/t2.txt and /test/test3 share the same inode but will be deleted as /test2/a/t7.txt has the same sha256 but different inode 
		_fs.checkAll(_scope.getConnection(), 9, 10);

		DatabaseNumberChecker.from(_scope).fileSystem(_fs).sharedInodes(1).banned(1).check();
		
	}
	
	
}
