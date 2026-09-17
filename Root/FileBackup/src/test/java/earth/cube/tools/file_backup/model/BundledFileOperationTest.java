package earth.cube.tools.file_backup.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import earth.cube.tools.file_backup.actions.AbstractActionTest;
import earth.cube.tools.file_backup.model.test_files.CopiedTestFile;
import earth.cube.tools.file_backup.model.test_files.FileSystem;

public class BundledFileOperationTest extends AbstractActionTest {
	
	@Test
	public void test_delete_1() throws Exception {
		_fs.add("test/._t1.txt", FileSystem.F1);   // will not be deleted as it is has no valid xattr content
		_fs.add("test/t1.txt", FileSystem.F2);
		_fs.add("test/t1.Xmp", FileSystem.F3);
		_fs.add("test/t2.txt", FileSystem.F4);
		_fs.create();
		
		_fs.softDelete("test/t1.txt");
		_fs.softDelete("test/t1.Xmp");
		
		getRecordFile("test/t1.txt").bundle().delete();

		
		_fs.checkAgainstPhysicalFiles();
	
	}

	@Test
	public void test_delete_2() throws Exception {
		_fs.add("test/._t1.txt", new CopiedTestFile(new File("test-data/conf.xattr")));
		_fs.add("test/t1.txt", FileSystem.F2);
		_fs.add("test/t1.Xmp", FileSystem.F3);   // will not be deleted as it XMP is not unique
		_fs.add("test/T1.Xmp", FileSystem.F3);   // will not be deleted as it XMP is not unique
		_fs.add("test/t2.txt", FileSystem.F4);
		_fs.create();
		
		_fs.softDelete("test/._t1.txt");
		_fs.softDelete("test/t1.txt");
		
		getRecordFile("test/t1.txt").bundle().delete();

		
		_fs.checkAgainstPhysicalFiles();
	
	}

	@Test
	public void test_delete_3() throws Exception {
		_fs.add("test/._t1.txt", new CopiedTestFile(new File("test-data/conf.xattr")));
		_fs.add("test/t1.txt", FileSystem.F2);
		_fs.add("test/t1.Xmp", FileSystem.F3);   // will not be deleted as it XMP is not unique
		_fs.add("test/t1.log", FileSystem.F3);  
		_fs.add("test/t2.txt", FileSystem.F4);
		_fs.create();
		
		_fs.softDelete("test/._t1.txt");
		_fs.softDelete("test/t1.txt");
		
		getRecordFile("test/t1.txt").bundle().delete();

		_fs.checkAgainstPhysicalFiles();

		_fs.softDelete("test/t1.log");
		_fs.softDelete("test/t1.Xmp");
		
		getRecordFile("test/t1.log").bundle().delete();
		
		_fs.checkAgainstPhysicalFiles();
	}

	@Test
	public void test_delete_4() throws Exception {
		_fs.add("test/._t1.txt", new CopiedTestFile(new File("test-data/conf.xattr")));
		_fs.add("test/t1.txt", FileSystem.F2);
		_fs.add("test/t1.Xmp", FileSystem.F3);
		_fs.add("test/t2.txt", FileSystem.F4);
		_fs.add("test/._t3.txt", new CopiedTestFile(new File("test-data/conf.xattr")));
		_fs.create();
		
		_fs.softDelete("test/._t1.txt");
		_fs.softDelete("test/t1.txt");
		_fs.softDelete("test/t1.Xmp");
		_fs.softDelete("test/t2.txt");
		
		getRecordFile("test/t1.txt").bundle().delete();
		getRecordFile("test/t2.txt").bundle().delete();
		
		_fs.checkAgainstPhysicalFiles();

		assertTrue(new File(_scope.getRootDirectory(), "test").exists());
		
		_fs.softDelete("test/._t3.txt");
		
		_scope.cleanUp();
		
		_fs.checkAgainstPhysicalFiles();
		
		assertFalse(new File(_scope.getRootDirectory(), "test").exists());
	}

	@Test
	public void test_delete_5() throws Exception {
		_fs.add("test/._t1.txt", new CopiedTestFile(new File("test-data/conf.xattr")));
		_fs.add("test/t1.txt", FileSystem.F2);
		_fs.add("test/t1.Xmp", FileSystem.F3);
		_fs.add("test/t2.txt", FileSystem.F4);
		_fs.add("test/._t3.txt", new CopiedTestFile(new File("test-data/conf.xattr")));
		_fs.add("test/t4.xmp", FileSystem.F5);
		_fs.create();
		
		_fs.softDelete("test/._t1.txt");
		_fs.softDelete("test/t1.txt");
		_fs.softDelete("test/t1.Xmp");
		_fs.softDelete("test/t2.txt");
		
		getRecordFile("test/t1.txt").bundle().delete();
		getRecordFile("test/t2.txt").bundle().delete();
		
		_fs.checkAgainstPhysicalFiles();

		assertTrue(new File(_scope.getRootDirectory(), "test").exists());
		
		_fs.softDelete("test/._t3.txt");
		_fs.softDelete("test/t4.xmp");
		
		_scope.cleanUp();
		
		_fs.checkAgainstPhysicalFiles();
		
		assertFalse(new File(_scope.getRootDirectory(), "test").exists());
	}
}
